package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;

import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
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
class LikeServiceIntegrationTest {

  @Autowired private ILikeService likeService;

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
  void likeAndUnlikePost_UpdateAuthorsProfileLikeCount() {
    UserDO author = insertUser("post-author");
    UserDO actor = insertUser("post-actor");
    PostDO post = insertPost(author.getId(), "Post title");

    likeService.like("post", post.getId(), actor.getId());

    PostDO persistedPost = postMapper.selectById(post.getId());
    UserStatsDO authorStats = userStatsMapper.selectById(author.getId());
    UserProfileResponse profile = userService.getProfile(author.getId());

    assertEquals(1, likeMapper.selectCount(null));
    assertEquals(Integer.valueOf(1), persistedPost.getLikeCount());
    assertNotNull(authorStats);
    assertEquals(Integer.valueOf(1), authorStats.getLikeCount());
    assertEquals(Integer.valueOf(1), profile.getLikeCount());

    likeService.unlike("post", post.getId(), actor.getId());

    PostDO updatedPost = postMapper.selectById(post.getId());
    UserStatsDO updatedAuthorStats = userStatsMapper.selectById(author.getId());
    UserProfileResponse updatedProfile = userService.getProfile(author.getId());

    assertEquals(0, likeMapper.selectCount(null));
    assertEquals(Integer.valueOf(0), updatedPost.getLikeCount());
    assertEquals(Integer.valueOf(0), updatedAuthorStats.getLikeCount());
    assertEquals(Integer.valueOf(0), updatedProfile.getLikeCount());
  }

  @Test
  void likeAndUnlikeReply_UpdateAuthorsProfileLikeCount() {
    UserDO author = insertUser("reply-author");
    UserDO actor = insertUser("reply-actor");
    PostDO post = insertPost(author.getId(), "Reply parent");
    ReplyDO reply = insertReply(post.getId(), author.getId(), "Reply content");

    likeService.like("reply", reply.getId(), actor.getId());

    ReplyDO persistedReply = replyMapper.selectById(reply.getId());
    UserStatsDO authorStats = userStatsMapper.selectById(author.getId());
    UserProfileResponse profile = userService.getProfile(author.getId());

    assertEquals(1, likeMapper.selectCount(null));
    assertEquals(Integer.valueOf(1), persistedReply.getLikeCount());
    assertNotNull(authorStats);
    assertEquals(Integer.valueOf(1), authorStats.getLikeCount());
    assertEquals(Integer.valueOf(1), profile.getLikeCount());

    likeService.unlike("reply", reply.getId(), actor.getId());

    ReplyDO updatedReply = replyMapper.selectById(reply.getId());
    UserStatsDO updatedAuthorStats = userStatsMapper.selectById(author.getId());
    UserProfileResponse updatedProfile = userService.getProfile(author.getId());

    assertEquals(0, likeMapper.selectCount(null));
    assertEquals(Integer.valueOf(0), updatedReply.getLikeCount());
    assertEquals(Integer.valueOf(0), updatedAuthorStats.getLikeCount());
    assertEquals(Integer.valueOf(0), updatedProfile.getLikeCount());
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

  private ReplyDO insertReply(Long postId, Long userId, String content) {
    ReplyDO reply = new ReplyDO();
    reply.setPostId(postId);
    reply.setUserId(userId);
    reply.setContent(content);
    reply.setFloorNumber(1);
    reply.setLikeCount(0);
    reply.setStatus(0);
    reply.setCreateTime(LocalDateTime.now());
    reply.setUpdateTime(LocalDateTime.now());
    replyMapper.insert(reply);
    return reply;
  }
}
