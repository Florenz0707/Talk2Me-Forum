package com.example.springboot_backend.talk2me.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.vo.CreatePostRequest;
import com.example.springboot_backend.talk2me.model.vo.UpdatePostRequest;

public interface IPostService {
  PostDO createPost(CreatePostRequest request, Long userId);

  PostDO getPost(Long id, Long currentUserId);

  PostDO updatePost(Long id, UpdatePostRequest request, Long userId);

  void deletePost(Long id, Long userId);

  Page<PostDO> listPosts(Long sectionId, Integer page, Integer size, Long currentUserId);
}
