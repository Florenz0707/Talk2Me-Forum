package com.example.springboot_backend.talk2me.service;

import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

  UserProfileResponse getProfile(Long userId);

  UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

  String uploadAvatar(Long userId, MultipartFile file);
}
