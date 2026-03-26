// WebSocket 通知服务
class NotificationWebSocket {
  constructor() {
    this.ws = null;
    this.listeners = [];
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
  }

  connect() {
    const token = localStorage.getItem("auth_token");
    if (!token) return;

    const wsUrl = `ws://localhost:8099/talk2me/ws/notifications?token=${token}`;
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log("WebSocket 已连接");
      this.reconnectAttempts = 0;
    };

    this.ws.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data);
        this.listeners.forEach((listener) => listener(notification));
      } catch (error) {
        console.error("解析通知消息失败:", error);
      }
    };

    this.ws.onerror = (error) => {
      console.error("WebSocket 错误:", error);
    };

    this.ws.onclose = () => {
      console.log("WebSocket 已断开");
      this.attemptReconnect();
    };
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => this.connect(), this.reconnectDelay);
    }
  }

  onNotification(callback) {
    this.listeners.push(callback);
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.listeners = [];
  }
}

export const notificationWS = new NotificationWebSocket();
