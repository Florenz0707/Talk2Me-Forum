package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.LikeDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.INotificationService;
import com.example.springboot_backend.talk2me.service.IReplyService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplyService implements IReplyService {
  private final ReplyMapper replyMapper;
  private final PostMapper postMapper;
  private final LikeMapper likeMapper;
  private final UserStatsMapper userStatsMapper;
  private final INotificationService notificationService;

  public ReplyService(
      ReplyMapper replyMapper,
      PostMapper postMapper,
      LikeMapper likeMapper,
      UserStatsMapper userStatsMapper,
      INotificationService notificationService) {
    this.replyMapper = replyMapper;
    this.postMapper = postMapper;
    this.likeMapper = likeMapper;
    this.userStatsMapper = userStatsMapper;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public ReplyDO createReply(Long postId, CreateReplyRequest request, Long userId) {
    PostDO post = postMapper.selectById(postId);
    if (post == null) {
      throw new RuntimeException("Post not found");
    }

    LambdaQueryWrapper<ReplyDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ReplyDO::getPostId, postId).orderByDesc(ReplyDO::getFloorNumber).last("LIMIT 1");
    ReplyDO lastReply = replyMapper.selectOne(wrapper);
    int floorNumber = lastReply == null ? 1 : lastReply.getFloorNumber() + 1;

    ReplyDO reply = new ReplyDO();
    reply.setPostId(postId);
    reply.setUserId(userId);
    reply.setContent(request.getContent());
    reply.setFloorNumber(floorNumber);
    reply.setLikeCount(0);
    reply.setStatus(0);
    replyMapper.insert(reply);

    post.setReplyCount(post.getReplyCount() + 1);
    postMapper.updateById(post);

    notificationService.createNotification(
        post.getUserId(), userId, "REPLY_POST", "REPLY", reply.getId(), "你的帖子有了新回复");

    return reply;
  }

  @Override
  public Page<ReplyDO> listReplies(Long postId, Integer page, Integer size, Long currentUserId) {
    Page<ReplyDO> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<ReplyDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(ReplyDO::getPostId, postId)
        .eq(ReplyDO::getStatus, 0)
        .orderByAsc(ReplyDO::getFloorNumber);
    Page<ReplyDO> result = replyMapper.selectPage(pageParam, wrapper);
    fillReplyLikedState(result.getRecords(), currentUserId);
    return result;
  }

  @Override
  @Transactional
  public void deleteReply(Long id, Long userId) {
    ReplyDO reply = replyMapper.selectById(id);
    if (reply == null || !reply.getUserId().equals(userId)) {
      throw new RuntimeException("Reply not found or not authorized");
    }

    if (!isActiveStatus(reply.getStatus())) {
      return;
    }

    PostDO post = postMapper.selectById(reply.getPostId());
    if (post != null) {
      post.setReplyCount(Math.max(0, defaultCount(post.getReplyCount()) - 1));
      postMapper.updateById(post);
      notificationService.revokeNotification(
          post.getUserId(), userId, "REPLY_POST", "REPLY", reply.getId());
    }

    int removedLikeCount = defaultCount(reply.getLikeCount());
    if (removedLikeCount > 0) {
      UserStatsDO authorStats = getOrCreateStats(reply.getUserId());
      authorStats.setLikeCount(
          Math.max(0, defaultCount(authorStats.getLikeCount()) - removedLikeCount));
      userStatsMapper.updateById(authorStats);
      notificationService.revokeNotificationsByTarget(
          reply.getUserId(), "LIKE_REPLY", "REPLY", reply.getId());
    }

    LambdaQueryWrapper<LikeDO> likeWrapper = new LambdaQueryWrapper<>();
    likeWrapper.eq(LikeDO::getTargetType, "REPLY").eq(LikeDO::getTargetId, reply.getId());
    likeMapper.delete(likeWrapper);

    reply.setStatus(1);
    reply.setLikeCount(0);
    replyMapper.updateById(reply);
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

  private boolean isActiveStatus(Integer status) {
    return status == null || status == 0;
  }

  private void fillReplyLikedState(java.util.List<ReplyDO> replies, Long currentUserId) {
    if (replies == null || replies.isEmpty()) {
      return;
    }

    Set<Long> likedReplyIds =
        getLikedReplyIds(currentUserId, replies.stream().map(ReplyDO::getId).toList());
    replies.forEach(reply -> reply.setIsLiked(likedReplyIds.contains(reply.getId())));
  }

  private Set<Long> getLikedReplyIds(Long currentUserId, java.util.List<Long> replyIds) {
    if (currentUserId == null || replyIds == null || replyIds.isEmpty()) {
      return Collections.emptySet();
    }

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, currentUserId)
        .eq(LikeDO::getTargetType, "REPLY")
        .in(LikeDO::getTargetId, replyIds);
    return likeMapper.selectList(wrapper).stream()
        .map(LikeDO::getTargetId)
        .collect(Collectors.toSet());
  }
}
