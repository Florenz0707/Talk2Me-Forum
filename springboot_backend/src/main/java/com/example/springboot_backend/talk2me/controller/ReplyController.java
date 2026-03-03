package com.example.springboot_backend.talk2me.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
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
        UserDetailsServiceImpl.UserPrincipal principal = (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
        return principal.getId();
    }

    @PostMapping("/posts/{postId}/replies")
    public Result<ReplyDO> createReply(@PathVariable Long postId, @Valid @RequestBody CreateReplyRequest request) {
        return Result.success(replyService.createReply(postId, request, getCurrentUserId()));
    }

    @GetMapping("/posts/{postId}/replies")
    public Result<Page<ReplyDO>> listReplies(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(replyService.listReplies(postId, page, size));
    }

    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id, getCurrentUserId());
        return Result.success(null);
    }
}
