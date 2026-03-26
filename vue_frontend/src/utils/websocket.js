// WebSocket 通知服务（STOMP over WebSocket）
class NotificationWebSocket {
  constructor() {
    this.ws = null;
    this.listeners = [];
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;

    this.connected = false;
    this.connecting = false;
    this.shouldReconnect = true;
    this.heartbeatTimer = null;
    this.subscriptionId = "notif-sub-0";
  }

  connect() {
    if (this.connecting || this.connected) return;

    const token = this.getToken();
    if (!token) return;

    this.connecting = true;
    this.shouldReconnect = true;

    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const backendHost = window.location.hostname || "localhost";
    const backendPort = "8099";
    const wsUrl = `${protocol}://${backendHost}:${backendPort}/talk2me/ws`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.sendFrame(
        [
          "CONNECT",
          "accept-version:1.2",
          "heart-beat:10000,10000",
          `Authorization:Bearer ${token}`,
          "",
        ].join("\n"),
      );
    };

    this.ws.onmessage = (event) => {
      this.handleStompFrame(event.data);
    };

    this.ws.onerror = (error) => {
      console.error("WebSocket/STOMP 错误:", error);
    };

    this.ws.onclose = () => {
      this.cleanupHeartbeat();
      this.connected = false;
      this.connecting = false;
      console.log("WebSocket 已断开");
      this.attemptReconnect();
    };
  }

  getToken() {
    return (
      localStorage.getItem("auth_token") || localStorage.getItem("accessToken")
    );
  }

  sendFrame(frame) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    this.ws.send(`${frame}\u0000`);
  }

  handleStompFrame(rawData) {
    // 心跳帧
    if (rawData === "\n") return;

    const data = String(rawData || "").replaceAll("\u0000", "");
    const [headerPart, body = ""] = data.split("\n\n");
    const headerLines = headerPart.split("\n");
    const command = headerLines[0];

    if (command === "CONNECTED") {
      this.connected = true;
      this.connecting = false;
      this.reconnectAttempts = 0;
      console.log("WebSocket(STOMP) 已连接");

      this.sendFrame(
        [
          "SUBSCRIBE",
          `id:${this.subscriptionId}`,
          "destination:/user/queue/notifications",
          "",
        ].join("\n"),
      );

      this.startHeartbeat();
      return;
    }

    if (command === "MESSAGE") {
      try {
        const notification = JSON.parse(body);
        this.listeners.forEach((listener) => listener(notification));
      } catch (error) {
        console.error("解析通知消息失败:", error, body);
      }
      return;
    }

    if (command === "ERROR") {
      console.error("STOMP ERROR:", data);
      return;
    }
  }

  startHeartbeat() {
    this.cleanupHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send("\n");
      }
    }, 10000);
  }

  cleanupHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  attemptReconnect() {
    if (!this.shouldReconnect) return;
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return;

    this.reconnectAttempts++;
    setTimeout(() => this.connect(), this.reconnectDelay);
  }

  onNotification(callback) {
    this.listeners.push(callback);
  }

  disconnect() {
    this.shouldReconnect = false;
    this.cleanupHeartbeat();

    if (this.connected) {
      this.sendFrame(["DISCONNECT", ""].join("\n"));
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.connected = false;
    this.connecting = false;
    this.listeners = [];
  }
}

export const notificationWS = new NotificationWebSocket();
