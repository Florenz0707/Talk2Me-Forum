package com.example.springboot_backend.talk2me.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.springboot_backend.core.model.BaseEntity;

@TableName("user_follows")
public class UserFollowDO extends BaseEntity {
  private Long followerId;
  private Long followeeId;

  public Long getFollowerId() {
    return followerId;
  }

  public void setFollowerId(Long followerId) {
    this.followerId = followerId;
  }

  public Long getFolloweeId() {
    return followeeId;
  }

  public void setFolloweeId(Long followeeId) {
    this.followeeId = followeeId;
  }
}
