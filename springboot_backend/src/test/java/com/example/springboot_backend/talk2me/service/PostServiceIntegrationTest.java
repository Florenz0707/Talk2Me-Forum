package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserFollowDO;
import com.example.springboot_backend.talk2me.model.vo.CreatePostRequest;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.NotificationMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.PostViewMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.repository.UserFollowMapper;
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

  @Autowired private UserFollowMapper userFollowMapper;

  @Autowired private INotificationService notificationService;

  @MockBean private NotificationRealtimeService notificationRealtimeService;

  @BeforeEach
  void setUp() {
    likeMapper.delete(null);
    notificationMapper.delete(null);
    postViewMapper.delete(null);
    replyMapper.delete(null);
    postMapper.delete(null);
    userFollowMapper.delete(null);
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

  @Test
  void createPost_NotifiesFollowersAboutNewPostOnly() {
    UserDO author = insertUser("notify-author");
    UserDO follower = insertUser("notify-follower");
    UserDO nonFollower = insertUser("notify-other");
    insertFollow(follower.getId(), author.getId());

    CreatePostRequest request = new CreatePostRequest();
    request.setSectionId(1L);
    request.setTitle("followee new post");
    request.setContent("content");

    PostDO created = postService.createPost(request, author.getId());
    assertNotNull(created.getId());

    assertEquals(1L, notificationService.countUnread(follower.getId()));
    assertEquals(0L, notificationService.countUnread(nonFollower.getId()));
    assertEquals(0L, notificationService.countUnread(author.getId()));

    NotificationDO followerNotification =
        notificationService.listNotifications(follower.getId(), 1, 20).getRecords().get(0);
    assertEquals("FOLLOWEE_POST", followerNotification.getType());
    assertEquals("POST", followerNotification.getTargetType());
    assertEquals(created.getId(), followerNotification.getTargetId());
    assertEquals(author.getId(), followerNotification.getActorId());
  }

  @Test
  void createPost_NoFollowersDoesNotCreateNotification() {
    UserDO author = insertUser("notify-no-follower-author");

    CreatePostRequest request = new CreatePostRequest();
    request.setSectionId(1L);
    request.setTitle("no follower post");
    request.setContent("content");

    PostDO created = postService.createPost(request, author.getId());
    assertNotNull(created.getId());
    assertEquals(0L, notificationService.countUnread(author.getId()));
    assertTrue(notificationService.listNotifications(author.getId(), 1, 20).getRecords().isEmpty());
  }

  @Test
  void createPost_NotifiesAllFollowers() {
    UserDO author = insertUser("notify-all-author");
    UserDO followerA = insertUser("notify-all-follower-a");
    UserDO followerB = insertUser("notify-all-follower-b");
    insertFollow(followerA.getId(), author.getId());
    insertFollow(followerB.getId(), author.getId());

    CreatePostRequest request = new CreatePostRequest();
    request.setSectionId(1L);
    request.setTitle("followers post");
    request.setContent("content");

    PostDO created = postService.createPost(request, author.getId());
    assertNotNull(created.getId());

    assertEquals(1L, notificationService.countUnread(followerA.getId()));
    assertEquals(1L, notificationService.countUnread(followerB.getId()));

    NotificationDO followerANotification =
        notificationService.listNotifications(followerA.getId(), 1, 20).getRecords().get(0);
    NotificationDO followerBNotification =
        notificationService.listNotifications(followerB.getId(), 1, 20).getRecords().get(0);

    assertEquals("FOLLOWEE_POST", followerANotification.getType());
    assertEquals("FOLLOWEE_POST", followerBNotification.getType());
    assertEquals(created.getId(), followerANotification.getTargetId());
    assertEquals(created.getId(), followerBNotification.getTargetId());
    assertEquals(author.getId(), followerANotification.getActorId());
    assertEquals(author.getId(), followerBNotification.getActorId());
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

  private void insertFollow(Long followerId, Long followeeId) {
    UserFollowDO follow = new UserFollowDO();
    follow.setFollowerId(followerId);
    follow.setFolloweeId(followeeId);

    LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserFollowDO::getFollowerId, followerId).eq(UserFollowDO::getFolloweeId, followeeId);
    if (userFollowMapper.selectCount(wrapper) == 0) {
      userFollowMapper.insert(follow);
    }
  }
}
