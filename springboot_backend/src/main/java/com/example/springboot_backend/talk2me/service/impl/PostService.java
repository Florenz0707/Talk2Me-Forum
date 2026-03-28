package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.LikeDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.PostViewDO;
import com.example.springboot_backend.talk2me.model.vo.CreatePostRequest;
import com.example.springboot_backend.talk2me.model.vo.UpdatePostRequest;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.PostViewMapper;
import com.example.springboot_backend.talk2me.service.IPostService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService implements IPostService {
  private final PostMapper postMapper;
  private final LikeMapper likeMapper;
  private final PostViewMapper postViewMapper;

  public PostService(PostMapper postMapper, LikeMapper likeMapper, PostViewMapper postViewMapper) {
    this.postMapper = postMapper;
    this.likeMapper = likeMapper;
    this.postViewMapper = postViewMapper;
  }

  @Override
  @Transactional
  public PostDO createPost(CreatePostRequest request, Long userId) {
    PostDO post = new PostDO();
    post.setSectionId(request.getSectionId());
    post.setUserId(userId);
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    post.setViewCount(0);
    post.setLikeCount(0);
    post.setReplyCount(0);
    post.setStatus(0);
    postMapper.insert(post);
    return post;
  }

  @Override
  @Transactional
  public PostDO getPost(Long id, Long currentUserId) {
    PostDO post = postMapper.selectById(id);
    if (post != null && isActiveStatus(post.getStatus())) {
      refreshPostView(post.getId(), currentUserId);
      post = postMapper.selectById(id);
      post.setIsLiked(isPostLikedByUser(post.getId(), currentUserId));
    }
    return post;
  }

  @Override
  @Transactional
  public PostDO updatePost(Long id, UpdatePostRequest request, Long userId) {
    PostDO post = postMapper.selectById(id);
    if (post == null || !post.getUserId().equals(userId)) {
      throw new RuntimeException("Post not found or not authorized");
    }
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    postMapper.updateById(post);
    return post;
  }

  @Override
  @Transactional
  public void deletePost(Long id, Long userId) {
    PostDO post = postMapper.selectById(id);
    if (post == null || !post.getUserId().equals(userId)) {
      throw new RuntimeException("Post not found or not authorized");
    }
    post.setStatus(1);
    postMapper.updateById(post);
  }

  @Override
  public Page<PostDO> listPosts(Long sectionId, Integer page, Integer size, Long currentUserId) {
    Page<PostDO> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<PostDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostDO::getStatus, 0);
    if (sectionId != null) {
      wrapper.eq(PostDO::getSectionId, sectionId);
    }
    wrapper.orderByDesc(PostDO::getCreateTime);
    Page<PostDO> result = postMapper.selectPage(pageParam, wrapper);
    fillPostLikedState(result.getRecords(), currentUserId);
    return result;
  }

  @Override
  @Transactional
  public void refreshPostView(Long postId, Long currentUserId) {
    if (currentUserId == null) {
      return;
    }

    PostViewDO existingPostView = findPostView(postId, currentUserId);
    if (existingPostView != null) {
      touchPostView(existingPostView);
      return;
    }

    PostViewDO postView = new PostViewDO();
    postView.setPostId(postId);
    postView.setUserId(currentUserId);
    postView.setHistoryDeleted(0);
    try {
      postViewMapper.insert(postView);
    } catch (DuplicateKeyException ignored) {
      PostViewDO duplicatedPostView = findPostView(postId, currentUserId);
      if (duplicatedPostView != null) {
        touchPostView(duplicatedPostView);
      }
      return;
    }

    PostDO post = postMapper.selectById(postId);
    if (post == null) {
      return;
    }
    post.setViewCount(defaultCount(post.getViewCount()) + 1);
    postMapper.updateById(post);
  }

  private PostViewDO findPostView(Long postId, Long currentUserId) {
    LambdaQueryWrapper<PostViewDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostViewDO::getPostId, postId).eq(PostViewDO::getUserId, currentUserId);
    return postViewMapper.selectOne(wrapper);
  }

  private void touchPostView(PostViewDO postView) {
    postView.setHistoryDeleted(0);
    postView.setUpdateTime(LocalDateTime.now());
    postViewMapper.updateById(postView);
  }

  private void fillPostLikedState(List<PostDO> posts, Long currentUserId) {
    if (posts == null || posts.isEmpty()) {
      return;
    }

    Set<Long> likedPostIds =
        getLikedTargetIds("POST", currentUserId, posts.stream().map(PostDO::getId).toList());
    posts.forEach(post -> post.setIsLiked(likedPostIds.contains(post.getId())));
  }

  private boolean isPostLikedByUser(Long postId, Long currentUserId) {
    if (currentUserId == null || postId == null) {
      return false;
    }

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, currentUserId)
        .eq(LikeDO::getTargetType, "POST")
        .eq(LikeDO::getTargetId, postId);
    return likeMapper.selectCount(wrapper) > 0;
  }

  private Set<Long> getLikedTargetIds(String targetType, Long currentUserId, List<Long> targetIds) {
    if (currentUserId == null || targetIds == null || targetIds.isEmpty()) {
      return Collections.emptySet();
    }

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, currentUserId)
        .eq(LikeDO::getTargetType, targetType)
        .in(LikeDO::getTargetId, targetIds);
    return likeMapper.selectList(wrapper).stream()
        .map(LikeDO::getTargetId)
        .collect(Collectors.toSet());
  }

  private int defaultCount(Integer count) {
    return count == null ? 0 : count;
  }

  private boolean isActiveStatus(Integer status) {
    return status == null || status == 0;
  }
}
