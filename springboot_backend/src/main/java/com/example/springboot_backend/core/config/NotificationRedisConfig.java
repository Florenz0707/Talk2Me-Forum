package com.example.springboot_backend.core.config;

import com.example.springboot_backend.talk2me.service.impl.NotificationRedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(name = "notification.redis.enabled", havingValue = "true")
public class NotificationRedisConfig {

  @Bean
  public ChannelTopic notificationTopic(@Value("${notification.redis.topic}") String topicName) {
    return new ChannelTopic(topicName);
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory redisConnectionFactory,
      NotificationRedisSubscriber notificationRedisSubscriber,
      ChannelTopic notificationTopic) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(notificationRedisSubscriber, notificationTopic);
    return container;
  }
}
