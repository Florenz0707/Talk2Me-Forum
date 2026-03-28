package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.PostViewMapper;
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
class PostServiceIntegrationTest {

  @Autowired private IPostService postService;

  @Autowired private ILikeService likeService;

  @Autowired private UserMapper userMapper;

  @Autowired private UserStatsMapper userStatsMapper;

  @Autowired private PostMapper postMapper;

  @Autowired private ReplyMapper replyMapper;

  @Autowired private LikeMapper likeMapper;

  @Autowired private PostViewMapper postViewMapper;

  @Autowired private NotificationMapper notificationMapper;

  @MockBean private NotificationRealtimeService notificationRealtimeService;

  @BeforeEach
  void setUp() {
    likeMapper.delete(null);
    notificationMapper.delete(null);
    postViewMapper.delete(null);
    replyMapper.delete(null);
    postMapper.delete(null);
    userStatsMapper.delete(null);
    userMapper.delete(null);
    reset(notificationRealtimeService);
  }

  @Test
  void getPost_AnonymousUserDoesNotIncreaseViewCount() {
    UserDO author = insertUser("anonymous-author");
    PostDO post = insertPost(author.getId(), "Anonymous view post");

    PostDO fetched = postService.getPost(post.getId(), null);

    assertEquals(Integer.valueOf(0), fetched.getViewCount());
    assertFalse(Boolean.TRUE.equals(fetched.getIsLiked()));
    assertEquals(Integer.valueOf(0), postMapper.selectById(post.getId()).getViewCount());
    assertEquals(0, postViewMapper.selectCount(null));
  }

  @Test
  void getPost_LoggedInUserCountsViewOnlyOnceAndReturnsIsLiked() {
    UserDO author = insertUser("view-author");
    UserDO viewer = insertUser("viewer-user");
    PostDO post = insertPost(author.getId(), "Viewed post");
    likeService.like("post", post.getId(), viewer.getId());

    PostDO firstFetch = postService.getPost(post.getId(), viewer.getId());
    PostDO secondFetch = postService.getPost(post.getId(), viewer.getId());

    assertEquals(Integer.valueOf(1), firstFetch.getViewCount());
    assertTrue(Boolean.TRUE.equals(firstFetch.getIsLiked()));
    assertEquals(Integer.valueOf(1), secondFetch.getViewCount());
    assertTrue(Boolean.TRUE.equals(secondFetch.getIsLiked()));
    assertEquals(Integer.valueOf(1), postMapper.selectById(post.getId()).getViewCount());
    assertEquals(1, postViewMapper.selectCount(null));
  }

  @Test
  void listPosts_LoggedInUserReceivesIsLikedState() {
    UserDO author = insertUser("list-author");
    UserDO viewer = insertUser("list-viewer");
    PostDO likedPost = insertPost(author.getId(), "Liked post");
    PostDO unlikedPost = insertPost(author.getId(), "Unliked post");
    likeService.like("post", likedPost.getId(), viewer.getId());

    var page = postService.listPosts(null, 1, 20, viewer.getId());

    PostDO fetchedLikedPost =
        page.getRecords().stream()
            .filter(post -> post.getId().equals(likedPost.getId()))
            .findFirst()
            .orElseThrow();
    PostDO fetchedUnlikedPost =
        page.getRecords().stream()
            .filter(post -> post.getId().equals(unlikedPost.getId()))
            .findFirst()
            .orElseThrow();

    assertTrue(Boolean.TRUE.equals(fetchedLikedPost.getIsLiked()));
    assertFalse(Boolean.TRUE.equals(fetchedUnlikedPost.getIsLiked()));
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
