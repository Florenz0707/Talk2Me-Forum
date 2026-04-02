package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.model.vo.ReplyDetailResponse;
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
    assertTrue(
        replyService.listReplies(post.getId(), 1, 20, replyAuthor.getId()).getRecords().isEmpty());
  }

  @Test
  void listReplies_LoggedInUserReceivesIsLikedState() {
    UserDO postOwner = insertUser("reply-post-owner");
    UserDO replyAuthor = insertUser("reply-liked-author");
    UserDO liker = insertUser("reply-liked-viewer");
    PostDO post = insertPost(postOwner.getId(), "Reply liked target");

    CreateReplyRequest request = new CreateReplyRequest();
    request.setContent("reply liked content");
    ReplyDO reply = replyService.createReply(post.getId(), request, replyAuthor.getId());
    likeService.like("reply", reply.getId(), liker.getId());

    var page = replyService.listReplies(post.getId(), 1, 20, liker.getId());
    var anonymousPage = replyService.listReplies(post.getId(), 1, 20, null);

    assertEquals(1, page.getRecords().size());
    assertTrue(Boolean.TRUE.equals(page.getRecords().getFirst().getIsLiked()));
    assertFalse(Boolean.TRUE.equals(anonymousPage.getRecords().getFirst().getIsLiked()));
  }

  @Test
  void createReply_StoresReplyContentInNotification() {
    UserDO postOwner = insertUser("reply-notification-owner");
    UserDO replyAuthor = insertUser("reply-notification-author");
    PostDO post = insertPost(postOwner.getId(), "Reply notification post");

    CreateReplyRequest request = new CreateReplyRequest();
    request.setContent("这是新的回复内容");
    replyService.createReply(post.getId(), request, replyAuthor.getId());

    var notifications =
        notificationService.listNotifications(postOwner.getId(), 1, 20).getRecords();
    assertEquals(1, notifications.size());
    assertEquals("REPLY_POST", notifications.getFirst().getType());
    assertEquals("这是新的回复内容", notifications.getFirst().getContent());
  }

  @Test
  void likeReply_StoresNullContentInNotification() {
    UserDO postOwner = insertUser("like-post-owner");
    UserDO replyAuthor = insertUser("like-reply-author");
    UserDO liker = insertUser("like-reply-liker");
    PostDO post = insertPost(postOwner.getId(), "Like reply target");

    CreateReplyRequest request = new CreateReplyRequest();
    request.setContent("reply for like notification");
    ReplyDO reply = replyService.createReply(post.getId(), request, replyAuthor.getId());

    likeService.like("reply", reply.getId(), liker.getId());

    var notifications =
        notificationService.listNotifications(replyAuthor.getId(), 1, 20).getRecords();
    assertEquals(1, notifications.size());
    assertEquals("LIKE_REPLY", notifications.getFirst().getType());
    assertNull(notifications.getFirst().getContent());
  }

  @Test
  void getReplyDetail_ReturnsReplyAuthorAndPostMetadata() {
    UserDO postOwner = insertUser("reply-detail-owner");
    UserDO replyAuthor = insertUser("reply-detail-author");
    UserDO viewer = insertUser("reply-detail-viewer");
    PostDO post = insertPost(postOwner.getId(), "Reply detail target");

    CreateReplyRequest request = new CreateReplyRequest();
    request.setContent("reply detail content");
    ReplyDO reply = replyService.createReply(post.getId(), request, replyAuthor.getId());
    likeService.like("reply", reply.getId(), viewer.getId());

    ReplyDetailResponse detail = replyService.getReplyDetail(reply.getId(), viewer.getId());

    assertEquals(reply.getId(), detail.getId());
    assertEquals(post.getId(), detail.getPostId());
    assertEquals(replyAuthor.getId(), detail.getUserId());
    assertEquals(replyAuthor.getUsername(), detail.getUsername());
    assertEquals("reply detail content", detail.getContent());
    assertEquals(post.getTitle(), detail.getPostTitle());
    assertEquals(post.getSectionId(), detail.getSectionId());
    assertEquals("技术讨论", detail.getSectionName());
    assertTrue(Boolean.TRUE.equals(detail.getIsLiked()));
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
