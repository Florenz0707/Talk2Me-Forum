package com.example.springboot_backend.talk2me.controller;

import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.vo.LikeRequest;
import com.example.springboot_backend.talk2me.service.ILikeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {
  private final ILikeService likeService;

  public LikeController(ILikeService likeService) {
    this.likeService = likeService;
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsServiceImpl.UserPrincipal principal =
        (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
    return principal.getId();
  }

  @PostMapping
  public Result<Void> like(@Valid @RequestBody LikeRequest request) {
    likeService.like(request.getTargetType(), request.getTargetId(), getCurrentUserId());
    return Result.success(null);
  }

  @DeleteMapping
  public Result<Void> unlike(@RequestParam String targetType, @RequestParam Long targetId) {
    likeService.unlike(targetType, targetId, getCurrentUserId());
    return Result.success(null);
  }
}
