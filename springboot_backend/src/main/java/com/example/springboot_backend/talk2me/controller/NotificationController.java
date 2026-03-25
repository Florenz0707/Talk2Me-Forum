package com.example.springboot_backend.talk2me.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.domain.NotificationDO;
import com.example.springboot_backend.talk2me.service.INotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  private final INotificationService notificationService;

  public NotificationController(INotificationService notificationService) {
    this.notificationService = notificationService;
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsServiceImpl.UserPrincipal principal =
        (UserDetailsServiceImpl.UserPrincipal) auth.getPrincipal();
    return principal.getId();
  }

  @GetMapping
  public Result<Page<NotificationDO>> listNotifications(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    return Result.success(notificationService.listNotifications(getCurrentUserId(), page, size));
  }

  @GetMapping("/unread-count")
  public Result<Long> countUnread() {
    return Result.success(notificationService.countUnread(getCurrentUserId()));
  }

  @PostMapping("/{notificationId}/read")
  public Result<Void> markRead(@PathVariable Long notificationId) {
    notificationService.markRead(getCurrentUserId(), notificationId);
    return Result.success(null);
  }

  @PostMapping("/read-all")
  public Result<Void> markAllRead() {
    notificationService.markAllRead(getCurrentUserId());
    return Result.success(null);
  }
}
