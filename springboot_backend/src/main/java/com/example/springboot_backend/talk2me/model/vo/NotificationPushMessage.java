package com.example.springboot_backend.talk2me.model.vo;

import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import java.time.LocalDateTime;

public class NotificationPushMessage {
  private Long id;
  private Long recipientId;
  private Long actorId;
  private String type;
  private String targetType;
  private Long targetId;
  private String content;
  private Boolean isRead;
  private LocalDateTime createTime;

  public static NotificationPushMessage from(NotificationDO notification) {
    NotificationPushMessage message = new NotificationPushMessage();
    message.setId(notification.getId());
    message.setRecipientId(notification.getRecipientId());
    message.setActorId(notification.getActorId());
    message.setType(notification.getType());
    message.setTargetType(notification.getTargetType());
    message.setTargetId(notification.getTargetId());
    message.setContent(notification.getContent());
    message.setIsRead(notification.getIsRead());
    message.setCreateTime(notification.getCreateTime());
    return message;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(Long recipientId) {
    this.recipientId = recipientId;
  }

  public Long getActorId() {
    return actorId;
  }

  public void setActorId(Long actorId) {
    this.actorId = actorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public Long getTargetId() {
    return targetId;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Boolean getIsRead() {
    return isRead;
  }

  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }
}
