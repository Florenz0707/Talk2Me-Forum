package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot_backend.talk2me.model.domain.LikeDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.ILikeService;
import com.example.springboot_backend.talk2me.service.INotificationService;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService implements ILikeService {
  private final LikeMapper likeMapper;
  private final PostMapper postMapper;
  private final ReplyMapper replyMapper;
  private final UserStatsMapper userStatsMapper;
  private final INotificationService notificationService;

  public LikeService(
      LikeMapper likeMapper,
      PostMapper postMapper,
      ReplyMapper replyMapper,
      UserStatsMapper userStatsMapper,
      INotificationService notificationService) {
    this.likeMapper = likeMapper;
    this.postMapper = postMapper;
    this.replyMapper = replyMapper;
    this.userStatsMapper = userStatsMapper;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public void like(String targetType, Long targetId, Long userId) {
    String normalizedTargetType = normalizeTargetType(targetType);
    ensureTargetExists(normalizedTargetType, targetId);

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, userId)
        .eq(LikeDO::getTargetType, normalizedTargetType)
        .eq(LikeDO::getTargetId, targetId);
    if (likeMapper.selectCount(wrapper) > 0) {
      throw new RuntimeException("Already liked");
    }

    LikeDO like = new LikeDO();
    like.setUserId(userId);
    like.setTargetType(normalizedTargetType);
    like.setTargetId(targetId);
    likeMapper.insert(like);

    updateLikeCount(normalizedTargetType, targetId, 1);
    createLikeNotification(normalizedTargetType, targetId, userId);
  }

  @Override
  @Transactional
  public void unlike(String targetType, Long targetId, Long userId) {
    String normalizedTargetType = normalizeTargetType(targetType);

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, userId)
        .eq(LikeDO::getTargetType, normalizedTargetType)
        .eq(LikeDO::getTargetId, targetId);
    int deleted = likeMapper.delete(wrapper);
    if (deleted <= 0) {
      return;
    }

    updateLikeCount(normalizedTargetType, targetId, -1);
    revokeLikeNotification(normalizedTargetType, targetId, userId);
  }

  private void updateLikeCount(String targetType, Long targetId, int delta) {
    if ("POST".equals(targetType)) {
      PostDO post = postMapper.selectById(targetId);
      if (post != null) {
        post.setLikeCount(Math.max(0, defaultCount(post.getLikeCount()) + delta));
        postMapper.updateById(post);
        updateReceivedLikeCount(post.getUserId(), delta);
      }
    } else if ("REPLY".equals(targetType)) {
      ReplyDO reply = replyMapper.selectById(targetId);
      if (reply != null) {
        reply.setLikeCount(Math.max(0, defaultCount(reply.getLikeCount()) + delta));
        replyMapper.updateById(reply);
        updateReceivedLikeCount(reply.getUserId(), delta);
      }
    }
  }

  private void updateReceivedLikeCount(Long userId, int delta) {
    UserStatsDO stats = getOrCreateStats(userId);
    stats.setLikeCount(Math.max(0, defaultCount(stats.getLikeCount()) + delta));
    userStatsMapper.updateById(stats);
  }

  private UserStatsDO getOrCreateStats(Long userId) {
    UserStatsDO stats = userStatsMapper.selectById(userId);
    if (stats != null) {
      return stats;
    }

    UserStatsDO newStats = new UserStatsDO();
    newStats.setUserId(userId);
    newStats.setLikeCount(0);
    newStats.setFollowerCount(0);
    newStats.setFollowingCount(0);
    newStats.setCreateTime(LocalDateTime.now());
    newStats.setUpdateTime(LocalDateTime.now());
    userStatsMapper.insert(newStats);
    return newStats;
  }

  private int defaultCount(Integer count) {
    return count == null ? 0 : count;
  }

  private String normalizeTargetType(String targetType) {
    if (targetType == null) {
      throw new IllegalArgumentException("targetType is required");
    }
    String normalized = targetType.toUpperCase(Locale.ROOT);
    if (!"POST".equals(normalized) && !"REPLY".equals(normalized)) {
      throw new IllegalArgumentException("Unsupported targetType: " + targetType);
    }
    return normalized;
  }

  private void ensureTargetExists(String targetType, Long targetId) {
    if ("POST".equals(targetType)) {
      PostDO post = postMapper.selectById(targetId);
      if (post == null || !isActiveStatus(post.getStatus())) {
        throw new RuntimeException("Post not found");
      }
      return;
    }

    ReplyDO reply = replyMapper.selectById(targetId);
    if (reply == null || !isActiveStatus(reply.getStatus())) {
      throw new RuntimeException("Reply not found");
    }
  }

  private void revokeLikeNotification(String targetType, Long targetId, Long actorId) {
    if ("POST".equals(targetType)) {
      PostDO post = postMapper.selectById(targetId);
      if (post == null) {
        return;
      }
      notificationService.revokeNotification(
          post.getUserId(), actorId, "LIKE_POST", "POST", targetId);
      return;
    }

    if ("REPLY".equals(targetType)) {
      ReplyDO reply = replyMapper.selectById(targetId);
      if (reply == null) {
        return;
      }
      notificationService.revokeNotification(
          reply.getUserId(), actorId, "LIKE_REPLY", "REPLY", targetId);
    }
  }

  private boolean isActiveStatus(Integer status) {
    return status == null || status == 0;
  }

  private void createLikeNotification(String targetType, Long targetId, Long actorId) {
    if ("POST".equals(targetType)) {
      PostDO post = postMapper.selectById(targetId);
      if (post == null) {
        return;
      }
      notificationService.createNotification(
          post.getUserId(), actorId, "LIKE_POST", "POST", targetId, "你的帖子收到了一个赞");
      return;
    }

    if ("REPLY".equals(targetType)) {
      ReplyDO reply = replyMapper.selectById(targetId);
      if (reply == null) {
        return;
      }
      notificationService.createNotification(
          reply.getUserId(), actorId, "LIKE_REPLY", "REPLY", targetId, "你的评论收到了一个赞");
    }
  }
}
