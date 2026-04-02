package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.service.impl.NotificationRealtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class NotificationServiceIntegrationTest {

  @Autowired private INotificationService notificationService;

  @Autowired private NotificationMapper notificationMapper;

  @MockBean private NotificationRealtimeService notificationRealtimeService;

  @BeforeEach
  void setUp() {
    notificationMapper.delete(null);
    reset(notificationRealtimeService);
  }

  @Test
  void createNotification_PersistsBeforeDispatch() {
    doAnswer(
            invocation -> {
              NotificationDO notification = invocation.getArgument(0);
              assertNotNull(notification.getId());

              NotificationDO persisted = notificationMapper.selectById(notification.getId());
              assertNotNull(persisted);
              assertEquals(notification.getRecipientId(), persisted.getRecipientId());
              assertFalse(Boolean.TRUE.equals(persisted.getIsRead()));
              return null;
            })
        .when(notificationRealtimeService)
        .dispatch(any(NotificationDO.class));

    NotificationDO created =
        notificationService.createNotification(100L, 200L, "LIKE_POST", "POST", 300L, "你的帖子收到了一个赞");

    assertNotNull(created);
    assertEquals(1L, notificationService.countUnread(100L));

    Page<NotificationDO> page = notificationService.listNotifications(100L, 1, 20);
    assertEquals(1, page.getRecords().size());
    assertEquals(created.getId(), page.getRecords().getFirst().getId());

    verify(notificationRealtimeService).dispatch(any(NotificationDO.class));
  }

  @Test
  void createNotification_SkipsSelfNotification() {
    NotificationDO created =
        notificationService.createNotification(100L, 100L, "FOLLOW_USER", "USER", 100L, "有新用户关注了你");

    assertNull(created);
    assertEquals(0L, notificationService.countUnread(100L));
    verify(notificationRealtimeService, never()).dispatch(any(NotificationDO.class));
  }

  @Test
  void markReadAndMarkAllRead_KeepUnreadCountConsistent() {
    NotificationDO first =
        notificationService.createNotification(100L, 201L, "LIKE_POST", "POST", 301L, "你的帖子收到了一个赞");
    NotificationDO second =
        notificationService.createNotification(100L, 202L, "REPLY_POST", "POST", 302L, "你的帖子有了新回复");
    NotificationDO otherUserNotification =
        notificationService.createNotification(101L, 203L, "FOLLOW_USER", "USER", 101L, "有新用户关注了你");

    assertNotNull(first);
    assertNotNull(second);
    assertNotNull(otherUserNotification);
    assertEquals(2L, notificationService.countUnread(100L));
    assertEquals(1L, notificationService.countUnread(101L));

    notificationService.markRead(100L, first.getId());

    NotificationDO updatedFirst = notificationMapper.selectById(first.getId());
    assertTrue(Boolean.TRUE.equals(updatedFirst.getIsRead()));
    assertEquals(1L, notificationService.countUnread(100L));

    notificationService.markAllRead(100L, "all");

    NotificationDO updatedSecond = notificationMapper.selectById(second.getId());
    NotificationDO untouchedOtherUserNotification =
        notificationMapper.selectById(otherUserNotification.getId());
    assertTrue(Boolean.TRUE.equals(updatedSecond.getIsRead()));
    assertFalse(Boolean.TRUE.equals(untouchedOtherUserNotification.getIsRead()));
    assertEquals(0L, notificationService.countUnread(100L));
    assertEquals(1L, notificationService.countUnread(101L));
  }

  @Test
  void markAllRead_ByTypeOnlyMarksMatchingNotifications() {
    NotificationDO likePost =
        notificationService.createNotification(100L, 201L, "LIKE_POST", "POST", 301L, null);
    NotificationDO likeReply =
        notificationService.createNotification(100L, 202L, "LIKE_REPLY", "REPLY", 302L, null);
    NotificationDO replyPost =
        notificationService.createNotification(
            100L, 203L, "REPLY_POST", "REPLY", 303L, "reply body");

    notificationService.markAllRead(100L, "like_reply");

    assertFalse(Boolean.TRUE.equals(notificationMapper.selectById(likePost.getId()).getIsRead()));
    assertTrue(Boolean.TRUE.equals(notificationMapper.selectById(likeReply.getId()).getIsRead()));
    assertFalse(Boolean.TRUE.equals(notificationMapper.selectById(replyPost.getId()).getIsRead()));
    assertEquals(2L, notificationService.countUnread(100L));
  }

  @Test
  void markAllRead_RejectsUnsupportedType() {
    assertThrows(
        IllegalArgumentException.class, () -> notificationService.markAllRead(100L, "UNKNOWN"));
  }

  @Test
  void markRead_RejectsOtherUsersNotification() {
    NotificationDO created =
        notificationService.createNotification(
            200L, 201L, "LIKE_REPLY", "REPLY", 301L, "你的评论收到了一个赞");

    assertNotNull(created);
    assertThrows(RuntimeException.class, () -> notificationService.markRead(100L, created.getId()));

    NotificationDO persisted = notificationMapper.selectById(created.getId());
    assertFalse(Boolean.TRUE.equals(persisted.getIsRead()));
  }

  @Test
  void revokeNotification_RemovesItFromUnreadAndList() {
    NotificationDO created =
        notificationService.createNotification(100L, 200L, "LIKE_POST", "POST", 300L, "你的帖子收到了一个赞");

    assertNotNull(created);
    assertEquals(1L, notificationService.countUnread(100L));

    notificationService.revokeNotification(100L, 200L, "LIKE_POST", "POST", 300L);

    assertEquals(0L, notificationService.countUnread(100L));
    assertTrue(notificationService.listNotifications(100L, 1, 20).getRecords().isEmpty());
    assertNull(notificationMapper.selectById(created.getId()));
    verify(notificationRealtimeService)
        .dispatchDeleted(
            argThat((NotificationDO notification) -> created.getId().equals(notification.getId())));
  }
}
