package com.example.springboot_backend.talk2me.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        UserDetailsServiceImpl.UserPrincipal principal = (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
        return principal.getId();
    }

    @PostMapping
    public Result<PostDO> createPost(@Valid @RequestBody CreatePostRequest request) {
        return Result.success(postService.createPost(request, getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public Result<PostDO> getPost(@PathVariable Long id) {
        return Result.success(postService.getPost(id));
    }

    @PutMapping("/{id}")
    public Result<PostDO> updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        return Result.success(postService.updatePost(id, request, getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id, getCurrentUserId());
        return Result.success(null);
    }

    @GetMapping
    public Result<Page<PostDO>> listPosts(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(postService.listPosts(sectionId, page, size));
    }
}
