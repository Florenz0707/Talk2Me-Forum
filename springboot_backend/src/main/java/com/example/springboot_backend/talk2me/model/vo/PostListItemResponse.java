package com.example.springboot_backend.talk2me.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PostListItemResponse {
  private Long id;
  private Long sectionId;
  private Long userId;
  private String title;
  private Integer viewCount;
  private Integer likeCount;
  private Integer replyCount;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Boolean isLiked;
  private LocalDateTime lastViewTime;

  @JsonProperty("section_name")
  private String sectionName;

  @JsonProperty("user_name")
  private String userName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSectionId() {
    return sectionId;
  }

  public void setSectionId(Long sectionId) {
    this.sectionId = sectionId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getViewCount() {
    return viewCount;
  }

  public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }

  public Integer getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }

  public Integer getReplyCount() {
    return replyCount;
  }

  public void setReplyCount(Integer replyCount) {
    this.replyCount = replyCount;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  public Boolean getIsLiked() {
    return isLiked;
  }

  public void setIsLiked(Boolean isLiked) {
    this.isLiked = isLiked;
  }

  public LocalDateTime getLastViewTime() {
    return lastViewTime;
  }

  public void setLastViewTime(LocalDateTime lastViewTime) {
    this.lastViewTime = lastViewTime;
  }

  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
