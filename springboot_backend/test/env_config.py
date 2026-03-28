from __future__ import annotations

from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent.parent
ENV_FILE = ROOT_DIR / ".env"


def _strip_quotes(value: str) -> str:
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
        return value[1:-1]
    return value


def load_dotenv(path: Path = ENV_FILE) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.exists():
        return values

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = _strip_quotes(value.strip())
    return values


def _normalize_context_path(value: str) -> str:
    if not value or value == "/":
        return ""
    return value if value.startswith("/") else f"/{value}"


def load_service_config() -> dict[str, str]:
    env_values = load_dotenv()

    host = env_values.get("TEST_SERVER_HOST", "127.0.0.1").strip() or "127.0.0.1"
    port = env_values.get("SERVER_PORT", "8099").strip() or "8099"
    context_path = _normalize_context_path(env_values.get("SERVER_CONTEXT_PATH", "/talk2me"))
    api_base_path = env_values.get("API_BASE_PATH", "/api/v1").strip() or "/api/v1"
    ws_path = env_values.get("WEBSOCKET_ENDPOINT", "/ws").strip() or "/ws"

    return {
        "host": host,
        "port": port,
        "context_path": context_path,
        "api_base_path": api_base_path if api_base_path.startswith("/") else f"/{api_base_path}",
        "ws_path": ws_path if ws_path.startswith("/") else f"/{ws_path}",
        "base_url": f"http://{host}:{port}",
    }
