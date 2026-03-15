package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.vo.CreatePostRequest;
import com.example.springboot_backend.talk2me.model.vo.UpdatePostRequest;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.service.IPostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService implements IPostService {
  private final PostMapper postMapper;

  public PostService(PostMapper postMapper) {
    this.postMapper = postMapper;
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
  public PostDO getPost(Long id) {
    PostDO post = postMapper.selectById(id);
    if (post != null && post.getStatus() == 0) {
      post.setViewCount(post.getViewCount() + 1);
      postMapper.updateById(post);
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
  public Page<PostDO> listPosts(Long sectionId, Integer page, Integer size) {
    Page<PostDO> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<PostDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostDO::getStatus, 0);
    if (sectionId != null) {
      wrapper.eq(PostDO::getSectionId, sectionId);
    }
    wrapper.orderByDesc(PostDO::getCreateTime);
    return postMapper.selectPage(pageParam, wrapper);
  }
}
