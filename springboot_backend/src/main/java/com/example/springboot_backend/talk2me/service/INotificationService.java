package com.example.springboot_backend.talk2me.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;

public interface INotificationService {
  NotificationDO createNotification(
      Long recipientId,
      Long actorId,
      String type,
      String targetType,
      Long targetId,
      String content);

  void revokeNotification(
      Long recipientId, Long actorId, String type, String targetType, Long targetId);

  void revokeNotificationsByTarget(Long recipientId, String type, String targetType, Long targetId);

  Page<NotificationDO> listNotifications(Long recipientId, Integer page, Integer size);

  Long countUnread(Long recipientId);

  void markRead(Long recipientId, Long notificationId);

  void markAllRead(Long recipientId);
}
