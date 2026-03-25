package com.example.springboot_backend.talk2me.service;

public interface IFollowService {
  void follow(Long followerId, Long followeeId);

  void unfollow(Long followerId, Long followeeId);
}
