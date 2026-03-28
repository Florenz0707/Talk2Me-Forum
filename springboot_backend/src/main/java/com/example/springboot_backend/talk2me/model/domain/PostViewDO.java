package com.example.springboot_backend.talk2me.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.springboot_backend.core.model.BaseEntity;

@TableName("post_views")
public class PostViewDO extends BaseEntity {
  private Long postId;
  private Long userId;

  @TableField("is_deleted")
  private Integer historyDeleted;

  public Long getPostId() {
    return postId;
  }

  public void setPostId(Long postId) {
    this.postId = postId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Integer getHistoryDeleted() {
    return historyDeleted;
  }

  public void setHistoryDeleted(Integer historyDeleted) {
    this.historyDeleted = historyDeleted;
  }
}
