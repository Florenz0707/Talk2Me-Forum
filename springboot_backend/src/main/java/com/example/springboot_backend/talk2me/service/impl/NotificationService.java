package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.service.INotificationService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService implements INotificationService {
  private static final String READ_ALL_TYPE = "ALL";
  private static final Set<String> SUPPORTED_TYPES =
      Set.of("LIKE_POST", "LIKE_REPLY", "REPLY_POST", "FOLLOW_USER", "FOLLOWEE_POST");

  private final NotificationMapper notificationMapper;
  private final NotificationRealtimeService notificationRealtimeService;

  public NotificationService(
      NotificationMapper notificationMapper,
      NotificationRealtimeService notificationRealtimeService) {
    this.notificationMapper = notificationMapper;
    this.notificationRealtimeService = notificationRealtimeService;
  }

  @Override
  @Transactional
  public NotificationDO createNotification(
      Long recipientId,
      Long actorId,
      String type,
      String targetType,
      Long targetId,
      String content) {
    if (recipientId == null || actorId == null) {
      throw new IllegalArgumentException("recipientId and actorId are required");
    }
    if (recipientId.equals(actorId)) {
      return null;
    }

    NotificationDO notification = new NotificationDO();
    notification.setRecipientId(recipientId);
    notification.setActorId(actorId);
    notification.setType(type);
    notification.setTargetType(targetType);
    notification.setTargetId(targetId);
    notification.setContent(content);
    notification.setIsRead(false);
    notificationMapper.insert(notification);
    notificationRealtimeService.dispatch(notification);
    return notification;
  }

  @Override
  @Transactional
  public void revokeNotification(
      Long recipientId, Long actorId, String type, String targetType, Long targetId) {
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(NotificationDO::getRecipientId, recipientId)
        .eq(NotificationDO::getActorId, actorId)
        .eq(NotificationDO::getType, type)
        .eq(NotificationDO::getTargetType, targetType)
        .eq(NotificationDO::getTargetId, targetId);
    revokeMatchingNotifications(wrapper);
  }

  @Override
  @Transactional
  public void revokeNotificationsByTarget(
      Long recipientId, String type, String targetType, Long targetId) {
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(NotificationDO::getRecipientId, recipientId)
        .eq(NotificationDO::getType, type)
        .eq(NotificationDO::getTargetType, targetType)
        .eq(NotificationDO::getTargetId, targetId);
    revokeMatchingNotifications(wrapper);
  }

  @Override
  public Page<NotificationDO> listNotifications(Long recipientId, Integer page, Integer size) {
    Page<NotificationDO> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(NotificationDO::getRecipientId, recipientId)
        .orderByDesc(NotificationDO::getCreateTime);
    return notificationMapper.selectPage(pageParam, wrapper);
  }

  @Override
  public Long countUnread(Long recipientId) {
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(NotificationDO::getRecipientId, recipientId).eq(NotificationDO::getIsRead, false);
    return notificationMapper.selectCount(wrapper);
  }

  @Override
  @Transactional
  public void markRead(Long recipientId, Long notificationId) {
    NotificationDO notification = notificationMapper.selectById(notificationId);
    if (notification == null || !notification.getRecipientId().equals(recipientId)) {
      throw new RuntimeException("Notification not found or not authorized");
    }
    notification.setIsRead(true);
    notificationMapper.updateById(notification);
  }

  @Override
  @Transactional
  public void markAllRead(Long recipientId, String type) {
    String normalizedType = normalizeReadType(type);
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(NotificationDO::getRecipientId, recipientId).eq(NotificationDO::getIsRead, false);
    if (!READ_ALL_TYPE.equals(normalizedType)) {
      wrapper.eq(NotificationDO::getType, normalizedType);
    }

    NotificationDO updateEntity = new NotificationDO();
    updateEntity.setIsRead(true);
    notificationMapper.update(updateEntity, wrapper);
  }

  private String normalizeReadType(String type) {
    if (type == null || type.isBlank()) {
      return READ_ALL_TYPE;
    }

    String normalizedType = type.trim().toUpperCase(Locale.ROOT);
    if (READ_ALL_TYPE.equals(normalizedType)) {
      return normalizedType;
    }
    if (!SUPPORTED_TYPES.contains(normalizedType)) {
      throw new IllegalArgumentException("Unsupported notification type: " + type);
    }
    return normalizedType;
  }

  private void revokeMatchingNotifications(LambdaQueryWrapper<NotificationDO> wrapper) {
    List<NotificationDO> notifications = notificationMapper.selectList(wrapper);
    notifications.forEach(
        notification -> {
          notificationMapper.deleteById(notification.getId());
          notificationRealtimeService.dispatchDeleted(notification);
        });
  }
}
