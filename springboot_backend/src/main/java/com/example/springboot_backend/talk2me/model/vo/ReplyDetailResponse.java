package com.example.springboot_backend.talk2me.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ReplyDetailResponse {
  private Long id;

  @JsonProperty("post_id")
  private Long postId;

  @JsonProperty("user_id")
  private Long userId;

  private String username;
  private String content;

  @JsonProperty("floor_number")
  private Integer floorNumber;

  @JsonProperty("like_count")
  private Integer likeCount;

  @JsonProperty("is_liked")
  private Boolean isLiked;

  @JsonProperty("post_title")
  private String postTitle;

  @JsonProperty("section_id")
  private Long sectionId;

  @JsonProperty("section_name")
  private String sectionName;

  @JsonProperty("create_time")
  private LocalDateTime createTime;

  @JsonProperty("update_time")
  private LocalDateTime updateTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Integer getFloorNumber() {
    return floorNumber;
  }

  public void setFloorNumber(Integer floorNumber) {
    this.floorNumber = floorNumber;
  }

  public Integer getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }

  public Boolean getIsLiked() {
    return isLiked;
  }

  public void setIsLiked(Boolean isLiked) {
    this.isLiked = isLiked;
  }

  public String getPostTitle() {
    return postTitle;
  }

  public void setPostTitle(String postTitle) {
    this.postTitle = postTitle;
  }

  public Long getSectionId() {
    return sectionId;
  }

  public void setSectionId(Long sectionId) {
    this.sectionId = sectionId;
  }

  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
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
}
