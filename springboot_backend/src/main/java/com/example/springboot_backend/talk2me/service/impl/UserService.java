package com.example.springboot_backend.talk2me.service.impl;

import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.IUserService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements IUserService {

  private final UserMapper userMapper;
  private final UserStatsMapper userStatsMapper;

  @Value("${upload.avatar.path:uploads/avatars}")
  private String avatarUploadPath;

  public UserService(UserMapper userMapper, UserStatsMapper userStatsMapper) {
    this.userMapper = userMapper;
    this.userStatsMapper = userStatsMapper;
  }

  @Override
  public UserProfileResponse getProfile(Long userId) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    UserStatsDO stats = userStatsMapper.selectById(userId);
    if (stats == null) {
      stats = initUserStats(userId);
    }

    return buildProfileResponse(user, stats);
  }

  @Override
  @Transactional
  public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    user.setBio(request.getBio());
    user.setBirthday(request.getBirthday());
    user.setGender(request.getGender());
    user.setOccupation(request.getOccupation());
    user.setUpdateTime(LocalDateTime.now());

    userMapper.updateById(user);

    UserStatsDO stats = userStatsMapper.selectById(userId);
    if (stats == null) {
      stats = initUserStats(userId);
    }

    return buildProfileResponse(user, stats);
  }

  @Override
  @Transactional
  public String uploadAvatar(Long userId, MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("文件不能为空");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    String filename = UUID.randomUUID().toString() + extension;

    try {
      Path uploadDir = Paths.get(avatarUploadPath).toAbsolutePath().normalize();
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      Path filePath = uploadDir.resolve(filename).normalize();
      file.transferTo(filePath);

      String avatarUrl = "/avatars/" + filename;

      UserDO user = userMapper.selectById(userId);
      if (user == null) {
        throw new RuntimeException("用户不存在");
      }
      user.setAvatar(avatarUrl);
      user.setUpdateTime(LocalDateTime.now());
      userMapper.updateById(user);

      return avatarUrl;
    } catch (IOException e) {
      throw new RuntimeException("文件上传失败", e);
    }
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new IllegalArgumentException("文件名不能为空");
    }
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
      throw new IllegalArgumentException("文件必须包含有效后缀名");
    }
    return originalFilename.substring(dotIndex);
  }

  private UserStatsDO initUserStats(Long userId) {
    UserStatsDO stats = new UserStatsDO();
    stats.setUserId(userId);
    stats.setLikeCount(0);
    stats.setFollowerCount(0);
    stats.setFollowingCount(0);
    stats.setCreateTime(LocalDateTime.now());
    stats.setUpdateTime(LocalDateTime.now());
    userStatsMapper.insert(stats);
    return stats;
  }

  private UserProfileResponse buildProfileResponse(UserDO user, UserStatsDO stats) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setBio(user.getBio());
    response.setAvatar(user.getAvatar());
    response.setBirthday(user.getBirthday());
    response.setGender(user.getGender());
    response.setOccupation(user.getOccupation());
    response.setLikeCount(stats.getLikeCount());
    response.setFollowerCount(stats.getFollowerCount());
    response.setFollowingCount(stats.getFollowingCount());
    return response;
  }
}
