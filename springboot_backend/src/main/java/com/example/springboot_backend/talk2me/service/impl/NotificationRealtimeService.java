package com.example.springboot_backend.talk2me.service.impl;

import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.model.vo.NotificationPushMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationRealtimeService {
  private static final Logger log = LoggerFactory.getLogger(NotificationRealtimeService.class);

  private final SimpMessagingTemplate messagingTemplate;
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;
  private final boolean redisEnabled;
  private final String redisTopic;

  public NotificationRealtimeService(
      SimpMessagingTemplate messagingTemplate,
      StringRedisTemplate stringRedisTemplate,
      ObjectMapper objectMapper,
      @Value("${notification.redis.enabled:false}") boolean redisEnabled,
      @Value("${notification.redis.topic:talk2me:notification:events}") String redisTopic) {
    this.messagingTemplate = messagingTemplate;
    this.stringRedisTemplate = stringRedisTemplate;
    this.objectMapper = objectMapper;
    this.redisEnabled = redisEnabled;
    this.redisTopic = redisTopic;
  }

  public void dispatch(NotificationDO notification) {
    if (notification == null) {
      return;
    }
    NotificationPushMessage pushMessage = NotificationPushMessage.from(notification);
    if (!redisEnabled) {
      sendToWebSocket(pushMessage);
      return;
    }
    publishToRedis(pushMessage);
  }

  public void onRedisMessage(String messagePayload) {
    try {
      NotificationPushMessage pushMessage =
          objectMapper.readValue(messagePayload, NotificationPushMessage.class);
      sendToWebSocket(pushMessage);
    } catch (JsonProcessingException ex) {
      log.error("Failed to parse notification redis message", ex);
    }
  }

  private void publishToRedis(NotificationPushMessage pushMessage) {
    try {
      String payload = objectMapper.writeValueAsString(pushMessage);
      stringRedisTemplate.convertAndSend(redisTopic, payload);
    } catch (Exception ex) {
      // Redis不可用时，回退到本机直推，避免影响业务成功响应。
      log.warn("Failed to publish notification to redis, fallback to local websocket push");
      sendToWebSocket(pushMessage);
    }
  }

  private void sendToWebSocket(NotificationPushMessage pushMessage) {
    messagingTemplate.convertAndSendToUser(
        String.valueOf(pushMessage.getRecipientId()), "/queue/notifications", pushMessage);
  }
}
