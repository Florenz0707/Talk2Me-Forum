package com.example.springboot_backend.talk2me.service.impl;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "notification.redis.enabled", havingValue = "true")
public class NotificationRedisSubscriber implements MessageListener {

  private final NotificationRealtimeService notificationRealtimeService;

  public NotificationRedisSubscriber(NotificationRealtimeService notificationRealtimeService) {
    this.notificationRealtimeService = notificationRealtimeService;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String payload = new String(message.getBody(), StandardCharsets.UTF_8);
    notificationRealtimeService.onRedisMessage(payload);
  }
}
