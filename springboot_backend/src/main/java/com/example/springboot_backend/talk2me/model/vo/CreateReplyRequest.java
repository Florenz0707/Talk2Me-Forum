package com.example.springboot_backend.talk2me.model.vo;

import jakarta.validation.constraints.NotBlank;

public class CreateReplyRequest {
  @NotBlank private String content;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
