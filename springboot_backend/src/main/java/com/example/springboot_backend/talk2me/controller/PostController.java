package com.example.springboot_backend.talk2me.controller;

import com.example.springboot_backend.core.model.PageResult;
import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.vo.CreatePostRequest;
import com.example.springboot_backend.talk2me.model.vo.UpdatePostRequest;
import com.example.springboot_backend.talk2me.service.IPostService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private final IPostService postService;

  public PostController(IPostService postService) {
    this.postService = postService;
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

  @PostMapping
  public Result<PostDO> createPost(@Valid @RequestBody CreatePostRequest request) {
    return Result.success(postService.createPost(request, getCurrentUserId()));
  }

  @GetMapping("/{id}")
  public Result<PostDO> getPost(@PathVariable Long id, Authentication auth) {
    return Result.success(postService.getPost(id, getOptionalCurrentUserId(auth)));
  }

  @PutMapping("/{id}")
  public Result<PostDO> updatePost(
      @PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
    return Result.success(postService.updatePost(id, request, getCurrentUserId()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> deletePost(@PathVariable Long id) {
    postService.deletePost(id, getCurrentUserId());
    return Result.success(null);
  }

  @GetMapping
  public Result<PageResult<PostDO>> listPosts(
      @RequestParam(required = false) Long sectionId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      Authentication auth) {
    return Result.success(
        PageResult.of(
            postService.listPosts(sectionId, page, size, getOptionalCurrentUserId(auth))));
  }
}
