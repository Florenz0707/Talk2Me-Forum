package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserFollowDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.repository.UserFollowMapper;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.IFollowService;
import com.example.springboot_backend.talk2me.service.INotificationService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService implements IFollowService {
  private final UserFollowMapper userFollowMapper;
  private final UserMapper userMapper;
  private final UserStatsMapper userStatsMapper;
  private final INotificationService notificationService;

  public FollowService(
      UserFollowMapper userFollowMapper,
      UserMapper userMapper,
      UserStatsMapper userStatsMapper,
      INotificationService notificationService) {
    this.userFollowMapper = userFollowMapper;
    this.userMapper = userMapper;
    this.userStatsMapper = userStatsMapper;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public void follow(Long followerId, Long followeeId) {
    if (followerId.equals(followeeId)) {
      throw new RuntimeException("不能关注自己");
    }

    UserDO followee = userMapper.selectById(followeeId);
    if (followee == null) {
      throw new RuntimeException("目标用户不存在");
    }

    LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserFollowDO::getFollowerId, followerId).eq(UserFollowDO::getFolloweeId, followeeId);
    if (userFollowMapper.selectCount(wrapper) > 0) {
      throw new RuntimeException("Already followed");
    }

    UserFollowDO follow = new UserFollowDO();
    follow.setFollowerId(followerId);
    follow.setFolloweeId(followeeId);
    userFollowMapper.insert(follow);

    UserStatsDO followerStats = getOrCreateStats(followerId);
    followerStats.setFollowingCount(followerStats.getFollowingCount() + 1);
    userStatsMapper.updateById(followerStats);

    UserStatsDO followeeStats = getOrCreateStats(followeeId);
    followeeStats.setFollowerCount(followeeStats.getFollowerCount() + 1);
    userStatsMapper.updateById(followeeStats);

    notificationService.createNotification(
        followeeId, followerId, "FOLLOW_USER", "USER", followeeId, "有新用户关注了你");
  }

  @Override
  @Transactional
  public void unfollow(Long followerId, Long followeeId) {
    LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserFollowDO::getFollowerId, followerId).eq(UserFollowDO::getFolloweeId, followeeId);
    if (userFollowMapper.delete(wrapper) <= 0) {
      return;
    }

    UserStatsDO followerStats = getOrCreateStats(followerId);
    followerStats.setFollowingCount(Math.max(0, followerStats.getFollowingCount() - 1));
    userStatsMapper.updateById(followerStats);

    UserStatsDO followeeStats = getOrCreateStats(followeeId);
    followeeStats.setFollowerCount(Math.max(0, followeeStats.getFollowerCount() - 1));
    userStatsMapper.updateById(followeeStats);
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
}
