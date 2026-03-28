#!/usr/bin/env python3
import json
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

from env_config import load_service_config


SERVICE_CONFIG = load_service_config()
DEFAULT_BASE_URL = SERVICE_CONFIG["base_url"]
TIMEOUT_SECONDS = 8
DEFAULT_API_BASE_PATH = SERVICE_CONFIG["api_base_path"]


def http_json(base_url, method, path, body=None, token=None):
    url = f"{base_url}{path}"
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


def detect_base_root(base_url, api_base_path):
    configured_context_path = SERVICE_CONFIG["context_path"]
    candidates = []
    for candidate in [configured_context_path, "", "/talk2me", "/api", "/backend"]:
        normalized = candidate.rstrip("/")
        if normalized not in candidates:
            candidates.append(normalized)
    for prefix in candidates:
        status, _ = http_json(
            f"{base_url}{prefix}",
            "POST",
            f"{api_base_path}/auth/verification",
        )
        if status != 404:
            return f"{base_url}{prefix}"
    return base_url


def assert_ok(condition, message):
    if not condition:
        print(f"[FAIL] {message}")
        sys.exit(1)
    print(f"[OK] {message}")


def register_or_login(base_root, api_base_path, username, password):
    status, _ = http_json(
        base_root,
        "POST",
        f"{api_base_path}/auth/register",
        {"username": username, "password": password},
    )
    assert_ok(status in (200, 400), f"register {username} status in (200,400), got {status}")

    status, login_payload = http_json(
        base_root,
        "POST",
        f"{api_base_path}/auth/login",
        {"username": username, "password": password},
    )
    assert_ok(status == 200, f"login {username} returns 200")
    token = login_payload.get("access_token")
    assert_ok(bool(token), f"login {username} returns access_token")
    return token


def get_user_id(base_root, api_base_path, token):
    status, payload = http_json(base_root, "GET", f"{api_base_path}/users/profile", token=token)
    assert_ok(status == 200, "GET /api/v1/users/profile returns 200")
    user_id = payload.get("data", {}).get("id")
    assert_ok(isinstance(user_id, int), "profile data contains user id")
    return user_id


def get_unread_count(base_root, api_base_path, token):
    status, payload = http_json(
        base_root, "GET", f"{api_base_path}/notifications/unread-count", token=token
    )
    assert_ok(status == 200, "GET /api/v1/notifications/unread-count returns 200")
    count = payload.get("data")
    assert_ok(isinstance(count, int), "unread-count data is int")
    return count


def main():
    base_url = DEFAULT_BASE_URL
    api_base_path = DEFAULT_API_BASE_PATH
    if len(sys.argv) > 1:
        base_url = sys.argv[1].rstrip("/")
    if len(sys.argv) > 2:
        api_base_path = sys.argv[2].rstrip("/")

    suffix = int(time.time()) % 100000
    pwd = "Passw0rd!"
    user_a = f"notify_a_{suffix}"
    user_b = f"notify_b_{suffix}"

    print(f"[INFO] base url: {base_url}")
    base_root = detect_base_root(base_url, api_base_path)
    print(f"[INFO] detected root: {base_root}")
    print(f"[INFO] api base path: {api_base_path}")
    print(f"[INFO] loaded service config: {SERVICE_CONFIG}")
    token_a = register_or_login(base_root, api_base_path, user_a, pwd)
    token_b = register_or_login(base_root, api_base_path, user_b, pwd)

    id_a = get_user_id(base_root, api_base_path, token_a)
    _ = get_user_id(base_root, api_base_path, token_b)

    before_unread = get_unread_count(base_root, api_base_path, token_a)
    print(f"[INFO] userA unread before actions: {before_unread}")

    status, post_payload = http_json(
        base_root,
        "POST",
        f"{api_base_path}/posts",
        {"sectionId": 1, "title": f"Smoke post {random.randint(1000, 9999)}", "content": "smoke"},
        token=token_a,
    )
    assert_ok(status == 200, "POST /api/v1/posts returns 200")
    post_id = post_payload.get("data", {}).get("id")
    assert_ok(isinstance(post_id, int), "create post returns post id")

    status, _ = http_json(
        base_root,
        "POST",
        f"{api_base_path}/posts/{post_id}/replies",
        {"content": "reply by userA"},
        token=token_a,
    )
    assert_ok(status == 200, "POST /api/v1/posts/{id}/replies by userA returns 200")

    status, _ = http_json(
        base_root,
        "POST",
        f"{api_base_path}/likes",
        {"targetType": "POST", "targetId": post_id},
        token=token_b,
    )
    assert_ok(status == 200, "POST /api/v1/likes (like post) returns 200")

    status, replies_payload = http_json(
        base_root,
        "GET",
        f"{api_base_path}/posts/{post_id}/replies?page=1&size=10",
        token=token_a,
    )
    assert_ok(status == 200, "GET /api/v1/posts/{id}/replies returns 200")
    reply_records = replies_payload.get("data", {}).get("records", [])
    assert_ok(isinstance(reply_records, list) and len(reply_records) >= 1, "reply list has records")
    reply_id = reply_records[0].get("id")
    assert_ok(isinstance(reply_id, int), "reply record contains id")

    status, _ = http_json(
        base_root,
        "POST",
        f"{api_base_path}/likes",
        {"targetType": "REPLY", "targetId": reply_id},
        token=token_b,
    )
    assert_ok(status == 200, "POST /api/v1/likes (like reply) returns 200")

    status, _ = http_json(base_root, "POST", f"{api_base_path}/follows/{id_a}", token=token_b)
    assert_ok(status == 200, "POST /api/v1/follows/{id} returns 200")

    after_unread = get_unread_count(base_root, api_base_path, token_a)
    assert_ok(
        after_unread >= before_unread + 3,
        f"userA unread count increases by >=3 (before={before_unread}, after={after_unread})",
    )

    status, list_payload = http_json(
        base_root, "GET", f"{api_base_path}/notifications?page=1&size=20", token=token_a
    )
    assert_ok(status == 200, "GET /api/v1/notifications returns 200")
    records = list_payload.get("data", {}).get("records", [])
    assert_ok(isinstance(records, list) and len(records) >= 1, "notification list has records")
    first_nid = records[0].get("id")
    assert_ok(isinstance(first_nid, int), "notification record contains id")

    status, _ = http_json(
        base_root, "POST", f"{api_base_path}/notifications/{first_nid}/read", token=token_a
    )
    assert_ok(status == 200, "POST /api/v1/notifications/{id}/read returns 200")

    status, _ = http_json(base_root, "POST", f"{api_base_path}/notifications/read-all", token=token_a)
    assert_ok(status == 200, "POST /api/v1/notifications/read-all returns 200")

    final_unread = get_unread_count(base_root, api_base_path, token_a)
    assert_ok(final_unread == 0, f"userA unread count is 0 after read-all, got {final_unread}")

    print("[PASS] smoke check completed")


if __name__ == "__main__":
    main()
