package com.example.springboot_backend.talk2me.service;

public interface ILikeService {
  void like(String targetType, Long targetId, Long userId);

  void unlike(String targetType, Long targetId, Long userId);
}
