package com.example.springboot_backend.talk2me.controller;

import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import com.example.springboot_backend.talk2me.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理")
public class UserController {

  private final IUserService userService;

  public UserController(IUserService userService) {
    this.userService = userService;
  }

  private Long getCurrentUserId(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new AccessDeniedException("未认证用户");
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetailsServiceImpl.UserPrincipal userPrincipal) {
      return userPrincipal.getId();
    }

    try {
      return Long.parseLong(auth.getName());
    } catch (NumberFormatException ex) {
      throw new IllegalStateException("无法从认证信息中解析用户ID", ex);
    }
  }

  @GetMapping("/profile")
  @Operation(summary = "获取当前用户资料")
  public Result<UserProfileResponse> getCurrentProfile(Authentication auth) {
    return Result.success(userService.getProfile(getCurrentUserId(auth)));
  }

  @GetMapping("/{userId}/profile")
  @Operation(summary = "获取指定用户资料")
  public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
    return Result.success(userService.getProfile(userId));
  }

  @PutMapping("/profile")
  @Operation(summary = "更新用户资料")
  public Result<UserProfileResponse> updateProfile(
      Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
    return Result.success(userService.updateProfile(getCurrentUserId(auth), request));
  }

  @PostMapping("/avatar")
  @Operation(summary = "上传头像")
  public Result<String> uploadAvatar(
      Authentication auth, @RequestParam("file") MultipartFile file) {
    return Result.success(userService.uploadAvatar(getCurrentUserId(auth), file));
  }
}
