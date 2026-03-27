#!/usr/bin/env -S uv run
# /// script
# dependencies = [
#   "websocket-client>=1.8.0",
# ]
# ///

import argparse
import json
import random
import ssl
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from typing import Dict, Optional, Tuple

import websocket

from env_config import load_service_config


SERVICE_CONFIG = load_service_config()
TIMEOUT_SECONDS = 8
DEFAULT_BASE_URL = SERVICE_CONFIG["base_url"]
DEFAULT_API_BASE_PATH = SERVICE_CONFIG["api_base_path"]
DEFAULT_WS_PATH = SERVICE_CONFIG["ws_path"]


def assert_ok(condition: bool, message: str) -> None:
    if not condition:
        print(f"[FAIL] {message}")
        sys.exit(1)
    print(f"[OK] {message}")


def http_json(
    base_root: str, method: str, path: str, body: Optional[dict] = None, token: Optional[str] = None
) -> Tuple[int, dict]:
    url = f"{base_root}{path}"
    data = None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if body is not None:
        data = json.dumps(body).encode("utf-8")

    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as ex:
        raw = ex.read().decode("utf-8")
        payload = {}
        if raw:
            try:
                payload = json.loads(raw)
            except json.JSONDecodeError:
                payload = {"raw": raw}
        return ex.code, payload
    except urllib.error.URLError as ex:
        print(f"[FATAL] cannot connect to {url}: {ex}")
        sys.exit(2)


def detect_base_root(base_url: str, api_base_path: str) -> str:
    configured_context_path = SERVICE_CONFIG["context_path"]
    candidates = []
    for candidate in [configured_context_path, "", "/talk2me", "/api", "/backend"]:
        normalized = candidate.rstrip("/")
        if normalized not in candidates:
            candidates.append(normalized)
    for prefix in candidates:
        status, _ = http_json(
            f"{base_url.rstrip('/')}{prefix}",
            "POST",
            f"{api_base_path}/auth/verification",
        )
        if status != 404:
            return f"{base_url.rstrip('/')}{prefix}"
    return base_url.rstrip("/")


def register_or_login(base_root: str, api_base_path: str, username: str, password: str) -> str:
    status, _ = http_json(
        base_root,
        "POST",
        f"{api_base_path}/auth/register",
        {"username": username, "password": password},
    )
    assert_ok(status in (200, 400), f"register {username} status in (200,400), got {status}")

    status, payload = http_json(
        base_root,
        "POST",
        f"{api_base_path}/auth/login",
        {"username": username, "password": password},
    )
    assert_ok(status == 200, f"login {username} returns 200")
    token = payload.get("access_token")
    assert_ok(bool(token), f"login {username} returns access_token")
    return token


def get_user_id(base_root: str, api_base_path: str, token: str) -> int:
    status, payload = http_json(base_root, "GET", f"{api_base_path}/users/profile", token=token)
    assert_ok(status == 200, "GET /users/profile returns 200")
    user_id = payload.get("data", {}).get("id")
    assert_ok(isinstance(user_id, int), "profile contains numeric id")
    return user_id


def get_unread_count(base_root: str, api_base_path: str, token: str) -> int:
    status, payload = http_json(
        base_root, "GET", f"{api_base_path}/notifications/unread-count", token=token
    )
    assert_ok(status == 200, "GET /notifications/unread-count returns 200")
    count = payload.get("data")
    assert_ok(isinstance(count, int), "unread count is int")
    return count


def to_ws_url(base_root: str, ws_path: str) -> str:
    parsed = urllib.parse.urlsplit(base_root)
    scheme = "wss" if parsed.scheme == "https" else "ws"
    path = parsed.path.rstrip("/") + ws_path
    return urllib.parse.urlunsplit((scheme, parsed.netloc, path, "", ""))


def build_ws_candidates(base_root: str, ws_path: str):
    base = ws_path.rstrip("/")
    return [to_ws_url(base_root, base), to_ws_url(base_root, f"{base}/websocket")]


class StompWebSocketClient:
    def __init__(self, ws_url: str, token: str):
        self.ws_url = ws_url
        self.token = token
        self.ws = None

    def connect(self) -> None:
        kwargs = {}
        if self.ws_url.startswith("wss://"):
            kwargs["sslopt"] = {"cert_reqs": ssl.CERT_NONE}
        kwargs["header"] = [f"Authorization: Bearer {self.token}"]
        self.ws = websocket.create_connection(self.ws_url, timeout=TIMEOUT_SECONDS, **kwargs)
        self._send_frame(
            "CONNECT",
            {
                "accept-version": "1.2",
                "host": "localhost",
                "Authorization": f"Bearer {self.token}",
            },
        )
        frame = self._recv_frame(timeout=TIMEOUT_SECONDS)
        assert_ok(frame is not None and frame["command"] == "CONNECTED", "STOMP CONNECTED received")

    def subscribe(self, destination: str, sub_id: str = "sub-0") -> None:
        self._send_frame(
            "SUBSCRIBE",
            {
                "id": sub_id,
                "destination": destination,
                "ack": "auto",
            },
        )

    def wait_message(self, timeout_seconds: int) -> Optional[Dict]:
        end_at = time.time() + timeout_seconds
        while time.time() < end_at:
            frame = self._recv_frame(timeout=min(2, end_at - time.time()))
            if frame is None:
                continue
            if frame["command"] == "MESSAGE":
                return frame
        return None

    def close(self) -> None:
        if self.ws is None:
            return
        try:
            self._send_frame("DISCONNECT", {"receipt": "bye"})
        except Exception:
            pass
        try:
            self.ws.close()
        except Exception:
            pass

    def _send_frame(self, command: str, headers: Dict[str, str], body: str = "") -> None:
        lines = [command] + [f"{k}:{v}" for k, v in headers.items()] + ["", body]
        payload = "\n".join(lines) + "\x00"
        self.ws.send(payload)

    def _recv_frame(self, timeout: float) -> Optional[Dict]:
        if timeout <= 0:
            return None
        self.ws.settimeout(timeout)
        try:
            raw = self.ws.recv()
        except websocket.WebSocketTimeoutException:
            return None
        if isinstance(raw, bytes):
            raw = raw.decode("utf-8", errors="ignore")
        raw = raw.strip()
        if not raw:
            return None
        if "\x00" in raw:
            raw = raw.split("\x00", 1)[0]
        lines = raw.split("\n")
        command = lines[0].strip()
        idx = 1
        headers = {}
        while idx < len(lines) and lines[idx].strip():
            line = lines[idx]
            if ":" in line:
                k, v = line.split(":", 1)
                headers[k] = v
            idx += 1
        body = "\n".join(lines[idx + 1 :]) if idx + 1 < len(lines) else ""
        return {"command": command, "headers": headers, "body": body}


def main() -> None:
    parser = argparse.ArgumentParser(description="Test WS + Redis notification path")
    parser.add_argument("base_url", nargs="?", default=DEFAULT_BASE_URL)
    parser.add_argument("api_base_path", nargs="?", default=DEFAULT_API_BASE_PATH)
    parser.add_argument("--ws-path", default=DEFAULT_WS_PATH)
    parser.add_argument("--timeout", type=int, default=12, help="seconds to wait for WS messages")
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")
    api_base_path = args.api_base_path.rstrip("/")
    ws_path = args.ws_path.rstrip("/")

    suffix = int(time.time()) % 100000
    password = "Passw0rd!"
    user_a = f"ws_notify_a_{suffix}"
    user_b = f"ws_notify_b_{suffix}"

    print(f"[INFO] base url: {base_url}")
    base_root = detect_base_root(base_url, api_base_path)
    print(f"[INFO] detected root: {base_root}")
    print(f"[INFO] api base path: {api_base_path}")
    print(f"[INFO] ws path: {ws_path}")
    print(f"[INFO] loaded service config: {SERVICE_CONFIG}")

    token_a = register_or_login(base_root, api_base_path, user_a, password)
    token_b = register_or_login(base_root, api_base_path, user_b, password)
    id_a = get_user_id(base_root, api_base_path, token_a)

    ws_candidates = build_ws_candidates(base_root, ws_path)
    client = None
    for ws_url in ws_candidates:
        print(f"[INFO] trying ws url: {ws_url}")
        try:
            client = StompWebSocketClient(ws_url, token_a)
            client.connect()
            print(f"[OK] ws connected: {ws_url}")
            break
        except Exception as ex:
            print(f"[WARN] ws connect failed for {ws_url}: {ex}")
            if client is not None:
                client.close()
                client = None
    assert_ok(client is not None, "websocket connected on one candidate endpoint")
    client.subscribe("/user/queue/notifications")
    print("[OK] subscribed /user/queue/notifications")

    try:
        status, post_payload = http_json(
            base_root,
            "POST",
            f"{api_base_path}/posts",
            {
                "sectionId": 1,
                "title": f"ws redis notify post {random.randint(1000, 9999)}",
                "content": "ws redis notification test",
            },
            token=token_a,
        )
        assert_ok(status == 200, "create post returns 200")
        post_id = post_payload.get("data", {}).get("id")
        assert_ok(isinstance(post_id, int), "create post returns id")

        status, _ = http_json(
            base_root,
            "POST",
            f"{api_base_path}/likes",
            {"targetType": "POST", "targetId": post_id},
            token=token_b,
        )
        assert_ok(status == 200, "userB like post returns 200")

        status, _ = http_json(
            base_root,
            "POST",
            f"{api_base_path}/posts/{post_id}/replies",
            {"content": "reply from userB for ws test"},
            token=token_b,
        )
        assert_ok(status == 200, "userB reply post returns 200")

        status, _ = http_json(base_root, "POST", f"{api_base_path}/follows/{id_a}", token=token_b)
        assert_ok(status == 200, "userB follow userA returns 200")

        received_types = []
        expected = {"LIKE_POST", "FOLLOW_USER", "REPLY_POST"}
        deadline = time.time() + args.timeout
        while time.time() < deadline and expected.difference(received_types):
            frame = client.wait_message(timeout_seconds=2)
            if frame is None:
                continue
            body = frame.get("body", "")
            try:
                payload = json.loads(body)
            except json.JSONDecodeError:
                print(f"[WARN] non-json ws message: {body}")
                continue
            n_type = payload.get("type")
            if n_type:
                received_types.append(n_type)
                print(f"[INFO] received ws notification type={n_type}")

        missing = expected.difference(received_types)
        assert_ok(
            not missing,
            f"received expected ws notification types {sorted(expected)}; missing={sorted(missing)}",
        )

        unread_before_unfollow = get_unread_count(base_root, api_base_path, token_a)
        status, _ = http_json(base_root, "DELETE", f"{api_base_path}/follows/{id_a}", token=token_b)
        assert_ok(status == 200, "userB unfollow userA returns 200")

        # 取关当前不产生通知，等待短时间确认未出现该类型
        frame = client.wait_message(timeout_seconds=2)
        if frame is not None:
            body = frame.get("body", "")
            try:
                payload = json.loads(body)
                n_type = payload.get("type")
                assert_ok(n_type != "UNFOLLOW_USER", "unfollow does not emit UNFOLLOW_USER notification")
            except json.JSONDecodeError:
                pass
        unread_after_unfollow = get_unread_count(base_root, api_base_path, token_a)
        assert_ok(
            unread_after_unfollow == unread_before_unfollow,
            f"unread count unchanged after unfollow (before={unread_before_unfollow}, after={unread_after_unfollow})",
        )
    finally:
        client.close()

    print("[PASS] ws+redis notification check completed")


if __name__ == "__main__":
    main()
