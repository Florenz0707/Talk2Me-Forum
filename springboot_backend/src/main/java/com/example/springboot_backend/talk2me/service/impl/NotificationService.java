package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService implements INotificationService {
  private final NotificationMapper notificationMapper;

  public NotificationService(NotificationMapper notificationMapper) {
    this.notificationMapper = notificationMapper;
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
    return notification;
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
  public void markAllRead(Long recipientId) {
    LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(NotificationDO::getRecipientId, recipientId).eq(NotificationDO::getIsRead, false);

    NotificationDO updateEntity = new NotificationDO();
    updateEntity.setIsRead(true);
    notificationMapper.update(updateEntity, wrapper);
  }
}
