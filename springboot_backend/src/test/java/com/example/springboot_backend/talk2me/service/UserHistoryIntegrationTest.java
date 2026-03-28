package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.PostViewDO;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class UserHistoryIntegrationTest {

  @Autowired private IUserService userService;

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
  void listViewedPosts_SortsByLatestViewTimeAndExcludesDeletedRecords() {
    UserDO author = insertUser("history-author");
    UserDO viewer = insertUser("history-viewer");
    PostDO earlierPost = insertPost(author.getId(), "Earlier post");
    PostDO laterPost = insertPost(author.getId(), "Later post");

    postService.getPost(earlierPost.getId(), viewer.getId());
    postService.getPost(laterPost.getId(), viewer.getId());
    likeService.like("post", laterPost.getId(), viewer.getId());

    updatePostViewTime(earlierPost.getId(), viewer.getId(), LocalDateTime.now().minusHours(2));
    updatePostViewTime(laterPost.getId(), viewer.getId(), LocalDateTime.now().minusHours(1));

    Page<PostDO> descPage = userService.listViewedPosts(viewer.getId(), 1, 20, "desc");
    Page<PostDO> ascPage = userService.listViewedPosts(viewer.getId(), 1, 20, "asc");

    assertEquals(List.of(laterPost.getId(), earlierPost.getId()), extractPostIds(descPage));
    assertEquals(List.of(earlierPost.getId(), laterPost.getId()), extractPostIds(ascPage));
    assertTrue(Boolean.TRUE.equals(descPage.getRecords().getFirst().getIsLiked()));
    assertFalse(Boolean.TRUE.equals(descPage.getRecords().getLast().getIsLiked()));
    assertTrue(descPage.getRecords().getFirst().getLastViewTime() != null);

    userService.deleteViewedPost(viewer.getId(), laterPost.getId());

    Page<PostDO> pageAfterDelete = userService.listViewedPosts(viewer.getId(), 1, 20, "desc");
    PostViewDO deletedView = findPostView(laterPost.getId(), viewer.getId());

    assertEquals(List.of(earlierPost.getId()), extractPostIds(pageAfterDelete));
    assertEquals(Integer.valueOf(1), deletedView.getHistoryDeleted());
    assertEquals(Integer.valueOf(1), postMapper.selectById(laterPost.getId()).getViewCount());
  }

  @Test
  void getPost_RevisitingDeletedHistoryRestoresRecordWithoutIncreasingViewCount() {
    UserDO author = insertUser("restore-author");
    UserDO viewer = insertUser("restore-viewer");
    PostDO post = insertPost(author.getId(), "Restored history post");

    postService.getPost(post.getId(), viewer.getId());
    assertEquals(Integer.valueOf(1), postMapper.selectById(post.getId()).getViewCount());

    userService.deleteViewedPost(viewer.getId(), post.getId());
    PostViewDO deletedView = findPostView(post.getId(), viewer.getId());
    LocalDateTime deletedAt = deletedView.getUpdateTime();
    assertEquals(Integer.valueOf(1), deletedView.getHistoryDeleted());

    postService.getPost(post.getId(), viewer.getId());

    PostViewDO restoredView = findPostView(post.getId(), viewer.getId());
    Page<PostDO> historyPage = userService.listViewedPosts(viewer.getId(), 1, 20, "desc");

    assertEquals(Integer.valueOf(0), restoredView.getHistoryDeleted());
    assertTrue(
        restoredView.getUpdateTime().isAfter(deletedAt)
            || restoredView.getUpdateTime().isEqual(deletedAt));
    assertEquals(Integer.valueOf(1), postMapper.selectById(post.getId()).getViewCount());
    assertEquals(List.of(post.getId()), extractPostIds(historyPage));
  }

  private List<Long> extractPostIds(Page<PostDO> page) {
    return page.getRecords().stream().map(PostDO::getId).toList();
  }

  private void updatePostViewTime(Long postId, Long userId, LocalDateTime updateTime) {
    PostViewDO postView = findPostView(postId, userId);
    postView.setUpdateTime(updateTime);
    postViewMapper.updateById(postView);
  }

  private PostViewDO findPostView(Long postId, Long userId) {
    LambdaQueryWrapper<PostViewDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostViewDO::getPostId, postId).eq(PostViewDO::getUserId, userId);
    return postViewMapper.selectOne(wrapper);
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
