import { getAuthToken } from "./authStorage";

class NotificationWebSocket {
  constructor() {
    this.ws = null;
    this.listeners = new Set();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 8;
    this.reconnectDelay = 2000;
    this.reconnectTimer = null;
    this.manualDisconnect = false;
    this.connected = false;
    this.connecting = false;
    this.subscribed = false;
    this.currentToken = "";
    this.buffer = "";
  }

  connect() {
    const token = getAuthToken();
    if (!token) {
      return;
    }

    if ((this.connected || this.connecting) && this.currentToken === token) {
      return;
    }

    this.manualDisconnect = false;
    this.currentToken = token;
    this._clearReconnectTimer();
    this._createSocket(token);
  }

  disconnect(options = {}) {
    const { clearListeners = false } = options;
    this.manualDisconnect = true;
    this._clearReconnectTimer();
    this.reconnectAttempts = 0;
    this._resetConnectionFlags();

    if (this.ws) {
      try {
        this.ws.close();
      } catch (error) {
        console.warn("Failed to close notification socket:", error);
      }
      this.ws = null;
    }

    if (clearListeners) {
      this.listeners.clear();
    }
  }

  onNotification(callback) {
    if (typeof callback !== "function") {
      return () => {};
    }
    this.listeners.add(callback);
    return () => this.offNotification(callback);
  }

  offNotification(callback) {
    this.listeners.delete(callback);
  }

  _createSocket(token) {
    this._resetSocketOnly();
    this.connecting = true;

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = `${protocol}//${window.location.host}/talk2me/ws`;
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this._sendFrame("CONNECT", {
        "accept-version": "1.2",
        host: window.location.host,
        "heart-beat": "0,0",
        Authorization: `Bearer ${token}`,
      });
    };

    this.ws.onmessage = (event) => {
      if (typeof event.data !== "string") {
        return;
      }
      this._consumeFrames(event.data);
    };

    this.ws.onerror = (error) => {
      console.error("Notification WebSocket error:", error);
    };

    this.ws.onclose = () => {
      this._resetConnectionFlags();
      if (!this.manualDisconnect) {
        this._scheduleReconnect();
      }
    };
  }

  _consumeFrames(chunk) {
    this.buffer += chunk;
    let frameEnd = this.buffer.indexOf("\u0000");

    while (frameEnd !== -1) {
      const rawFrame = this.buffer.slice(0, frameEnd);
      this.buffer = this.buffer.slice(frameEnd + 1);
      this._handleFrame(rawFrame);
      frameEnd = this.buffer.indexOf("\u0000");
    }
  }

  _handleFrame(rawFrame) {
    const normalized = rawFrame.replace(/^\n+/, "");
    if (!normalized.trim()) {
      return;
    }

    const lines = normalized.split("\n");
    const command = lines[0]?.trim();
    if (!command) {
      return;
    }

    const headers = {};
    let index = 1;
    while (index < lines.length && lines[index].trim() !== "") {
      const line = lines[index];
      const separator = line.indexOf(":");
      if (separator !== -1) {
        const key = line.slice(0, separator).trim();
        const value = line.slice(separator + 1).trim();
        headers[key] = value;
      }
      index += 1;
    }

    const body =
      index + 1 < lines.length ? lines.slice(index + 1).join("\n") : "";

    if (command === "CONNECTED") {
      this.connected = true;
      this.connecting = false;
      this.reconnectAttempts = 0;
      if (!this.subscribed) {
        this._sendFrame("SUBSCRIBE", {
          id: "notifications-sub-0",
          destination: "/user/queue/notifications",
          ack: "auto",
        });
        this.subscribed = true;
      }
      return;
    }

    if (command === "MESSAGE") {
      this._handleNotificationBody(body);
      return;
    }

    if (command === "ERROR") {
      console.error("Notification STOMP error frame:", headers, body);
    }
  }

  _handleNotificationBody(body) {
    if (!body) {
      return;
    }
    try {
      const notification = JSON.parse(body);
      this.listeners.forEach((listener) => {
        try {
          listener(notification);
        } catch (error) {
          console.error("Notification listener error:", error);
        }
      });
    } catch (error) {
      console.error("Failed to parse notification body:", error, body);
    }
  }

  _sendFrame(command, headers = {}, body = "") {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }
    const headerLines = Object.entries(headers).map(
      ([key, value]) => `${key}:${value}`,
    );
    const frame = [command, ...headerLines, "", body].join("\n") + "\u0000";
    this.ws.send(frame);
  }

  _scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      return;
    }
    this.reconnectAttempts += 1;
    this._clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      this.connect();
    }, this.reconnectDelay);
  }

  _clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  _resetSocketOnly() {
    if (this.ws) {
      try {
        this.ws.close();
      } catch (error) {
        console.warn("Failed to close stale notification socket:", error);
      }
      this.ws = null;
    }
    this.buffer = "";
    this._resetConnectionFlags();
  }

  _resetConnectionFlags() {
    this.connected = false;
    this.connecting = false;
    this.subscribed = false;
  }
}

export const notificationWS = new NotificationWebSocket();
