package com.example.springboot_backend.talk2me.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.model.vo.ReplyDetailResponse;
import com.example.springboot_backend.talk2me.service.IReplyService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReplyController {
  private final IReplyService replyService;

  public ReplyController(IReplyService replyService) {
    this.replyService = replyService;
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsServiceImpl.UserPrincipal principal =
        (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
    return principal.getId();
  }

  private Long getOptionalCurrentUserId(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetailsServiceImpl.UserPrincipal userPrincipal) {
      return userPrincipal.getId();
    }

    try {
      return Long.parseLong(auth.getName());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  @PostMapping("/posts/{postId}/replies")
  public Result<ReplyDO> createReply(
      @PathVariable Long postId, @Valid @RequestBody CreateReplyRequest request) {
    return Result.success(replyService.createReply(postId, request, getCurrentUserId()));
  }

  @GetMapping("/posts/{postId}/replies")
  public Result<Page<ReplyDO>> listReplies(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      Authentication auth) {
    return Result.success(
        replyService.listReplies(postId, page, size, getOptionalCurrentUserId(auth)));
  }

  @GetMapping("/replies/{id}")
  public Result<ReplyDetailResponse> getReplyDetail(@PathVariable Long id, Authentication auth) {
    return Result.success(replyService.getReplyDetail(id, getOptionalCurrentUserId(auth)));
  }

  @DeleteMapping("/replies/{id}")
  public Result<Void> deleteReply(@PathVariable Long id) {
    replyService.deleteReply(id, getCurrentUserId());
    return Result.success(null);
  }
}
