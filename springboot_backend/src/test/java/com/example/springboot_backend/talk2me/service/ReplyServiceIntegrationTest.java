package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.impl.NotificationRealtimeService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ReplyServiceIntegrationTest {

  @Autowired private IReplyService replyService;

  @Autowired private ILikeService likeService;

  @Autowired private INotificationService notificationService;

  @Autowired private IUserService userService;

  @Autowired private UserMapper userMapper;

  @Autowired private UserStatsMapper userStatsMapper;

  @Autowired private PostMapper postMapper;

  @Autowired private ReplyMapper replyMapper;

  @Autowired private LikeMapper likeMapper;

  @Autowired private NotificationMapper notificationMapper;

  @MockBean private NotificationRealtimeService notificationRealtimeService;

  @BeforeEach
  void setUp() {
    likeMapper.delete(null);
    notificationMapper.delete(null);
    replyMapper.delete(null);
    postMapper.delete(null);
    userStatsMapper.delete(null);
    userMapper.delete(null);
    reset(notificationRealtimeService);
  }

  @Test
  void deleteReply_RemovesRelatedNotificationsAndCounters() {
    UserDO postOwner = insertUser("post-owner");
    UserDO replyAuthor = insertUser("reply-author");
    UserDO liker = insertUser("reply-liker");
    PostDO post = insertPost(postOwner.getId(), "Reply target post");

    CreateReplyRequest request = new CreateReplyRequest();
    request.setContent("reply content");
    ReplyDO reply = replyService.createReply(post.getId(), request, replyAuthor.getId());
    likeService.like("reply", reply.getId(), liker.getId());

    assertEquals(1L, notificationService.countUnread(postOwner.getId()));
    assertEquals(1L, notificationService.countUnread(replyAuthor.getId()));
    assertEquals(Integer.valueOf(1), userService.getProfile(replyAuthor.getId()).getLikeCount());

    replyService.deleteReply(reply.getId(), replyAuthor.getId());

    ReplyDO persistedReply = replyMapper.selectById(reply.getId());
    PostDO persistedPost = postMapper.selectById(post.getId());

    assertEquals(Integer.valueOf(1), persistedReply.getStatus());
    assertEquals(Integer.valueOf(0), persistedReply.getLikeCount());
    assertEquals(Integer.valueOf(0), persistedPost.getReplyCount());
    assertEquals(0, likeMapper.selectCount(null));
    assertEquals(0L, notificationService.countUnread(postOwner.getId()));
    assertEquals(0L, notificationService.countUnread(replyAuthor.getId()));
    assertTrue(
        notificationService.listNotifications(postOwner.getId(), 1, 20).getRecords().isEmpty());
    assertTrue(
        notificationService.listNotifications(replyAuthor.getId(), 1, 20).getRecords().isEmpty());
    assertEquals(Integer.valueOf(0), userService.getProfile(replyAuthor.getId()).getLikeCount());
    assertTrue(replyService.listReplies(post.getId(), 1, 20).getRecords().isEmpty());
  }

  private UserDO insertUser(String username) {
    UserDO user = new UserDO();
    user.setUsername(username);
    user.setPassword("password123");
    user.setEnabled(true);
    user.setBio("bio");
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());
    userMapper.insert(user);
    return user;
  }

  private PostDO insertPost(Long userId, String title) {
    PostDO post = new PostDO();
    post.setSectionId(1L);
    post.setUserId(userId);
    post.setTitle(title);
    post.setContent("content");
    post.setViewCount(0);
    post.setLikeCount(0);
    post.setReplyCount(0);
    post.setStatus(0);
    post.setCreateTime(LocalDateTime.now());
    post.setUpdateTime(LocalDateTime.now());
    postMapper.insert(post);
    return post;
  }
}
