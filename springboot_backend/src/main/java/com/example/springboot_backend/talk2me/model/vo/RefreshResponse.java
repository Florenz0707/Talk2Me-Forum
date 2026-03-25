package com.example.springboot_backend.talk2me.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshResponse {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  private String message;

  public RefreshResponse(String accessToken, String refreshToken, String message) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.message = message;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
