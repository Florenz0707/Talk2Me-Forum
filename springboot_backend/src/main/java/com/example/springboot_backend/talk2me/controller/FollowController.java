package com.example.springboot_backend.talk2me.controller;

import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.service.IFollowService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {
  private final IFollowService followService;

  public FollowController(IFollowService followService) {
    this.followService = followService;
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsServiceImpl.UserPrincipal principal =
        (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
    return principal.getId();
  }

  @PostMapping("/{followeeId}")
  public Result<Void> follow(@PathVariable Long followeeId) {
    followService.follow(getCurrentUserId(), followeeId);
    return Result.success(null);
  }

  @DeleteMapping("/{followeeId}")
  public Result<Void> unfollow(@PathVariable Long followeeId) {
    followService.unfollow(getCurrentUserId(), followeeId);
    return Result.success(null);
  }
}
