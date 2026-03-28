package com.example.springboot_backend.talk2me.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

  UserProfileResponse getProfile(Long userId);

  Page<PostDO> listViewedPosts(Long userId, Integer page, Integer size, String order);

  void deleteViewedPost(Long userId, Long postId);

  UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

  String uploadAvatar(Long userId, MultipartFile file);
}
