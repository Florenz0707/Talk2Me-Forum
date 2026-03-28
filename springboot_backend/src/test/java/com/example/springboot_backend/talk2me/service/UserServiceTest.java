package com.example.springboot_backend.talk2me.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostViewMapper;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.impl.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserMapper userMapper;

  @Mock private UserStatsMapper userStatsMapper;

  @Mock private PostViewMapper postViewMapper;

  @Mock private LikeMapper likeMapper;

  @InjectMocks private UserService userService;

  private UserDO testUser;
  private UserStatsDO testStats;

  @BeforeEach
  void setUp() {
    testUser = new UserDO();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setBio("Test bio");
    testUser.setGender("男");
    testUser.setOccupation("开发者");

    testStats = new UserStatsDO();
    testStats.setUserId(1L);
    testStats.setLikeCount(10);
    testStats.setFollowerCount(5);
    testStats.setFollowingCount(3);
  }

  @Test
  void getProfile_Success() {
    when(userMapper.selectById(1L)).thenReturn(testUser);
    when(userStatsMapper.selectById(1L)).thenReturn(testStats);

    UserProfileResponse response = userService.getProfile(1L);

    assertNotNull(response);
    assertEquals("testuser", response.getUsername());
    assertEquals("Test bio", response.getBio());
    assertEquals(10, response.getLikeCount());
    assertEquals(5, response.getFollowerCount());
    assertEquals(3, response.getFollowingCount());
  }

  @Test
  void getProfile_UserNotFound() {
    when(userMapper.selectById(1L)).thenReturn(null);

    assertThrows(RuntimeException.class, () -> userService.getProfile(1L));
  }

  @Test
  void updateProfile_Success() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setBio("Updated bio");
    request.setGender("女");
    request.setOccupation("工程师");
    request.setBirthday(LocalDate.of(1990, 1, 1));

    when(userMapper.selectById(1L)).thenReturn(testUser);
    when(userStatsMapper.selectById(1L)).thenReturn(testStats);
    when(userMapper.updateById(any(UserDO.class))).thenReturn(1);

    UserProfileResponse response = userService.updateProfile(1L, request);

    assertNotNull(response);
    verify(userMapper).updateById(any(UserDO.class));
  }
}
