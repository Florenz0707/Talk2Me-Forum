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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserMapper userMapper;

  @Mock private UserStatsMapper userStatsMapper;

  @Mock private PostViewMapper postViewMapper;

  @Mock private LikeMapper likeMapper;

  private UserService userService;

  private UserDO testUser;
  private UserStatsDO testStats;

  @BeforeEach
  void setUp() {
    userService =
        new UserService(
            userMapper, userStatsMapper, postViewMapper, likeMapper, new ObjectMapper());
    setField(userService, "maxPreferencesLength", 8192);

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

  @Test
  void getPreferences_ReturnsDefaultsWhenEmpty() {
    when(userMapper.selectById(1L)).thenReturn(testUser);

    var preferences = userService.getPreferences(1L);

    assertEquals("system", preferences.path("theme").asText());
    assertEquals("zh-CN", preferences.path("language").asText());
    assertTrue(preferences.path("notification").path("enableWs").asBoolean());
  }

  @Test
  void updatePreferences_Success() {
    when(userMapper.selectById(1L)).thenReturn(testUser);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode request = mapper.createObjectNode();
    request.put("theme", "dark");
    ObjectNode notification = mapper.createObjectNode();
    notification.put("muteLike", true);
    request.set("notification", notification);

    var updated = userService.updatePreferences(1L, request);

    assertEquals("dark", updated.path("theme").asText());
    assertTrue(updated.path("notification").path("muteLike").asBoolean());
    verify(userMapper).updateById(any(UserDO.class));
  }

  @Test
  void updatePreferences_InvalidField() {
    when(userMapper.selectById(1L)).thenReturn(testUser);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode request = mapper.createObjectNode();
    request.put("unknownField", "x");

    assertThrows(IllegalArgumentException.class, () -> userService.updatePreferences(1L, request));
  }

  @Test
  void patchPreferences_MergesWithStoredPreferences() {
    when(userMapper.selectById(1L)).thenReturn(testUser);
    testUser.setPreferences(
        "{\"theme\":\"system\",\"language\":\"zh-CN\",\"notification\":{\"muteLike\":false,\"muteReply\":false,\"muteFollow\":false,\"muteFolloweePost\":false,\"enableWs\":true}}");

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode patch = mapper.createObjectNode();
    ObjectNode notification = mapper.createObjectNode();
    notification.put("muteLike", true);
    patch.set("notification", notification);

    var updated = userService.patchPreferences(1L, patch);

    assertTrue(updated.path("notification").path("muteLike").asBoolean());
    assertEquals("system", updated.path("theme").asText());
    verify(userMapper).updateById(any(UserDO.class));
  }

  @Test
  void updatePreferences_TooLargeRejected() {
    when(userMapper.selectById(1L)).thenReturn(testUser);
    setField(userService, "maxPreferencesLength", 40);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode request = mapper.createObjectNode();
    request.put("theme", "dark");
    request.put("language", "zh-CN-extra-long-value");

    assertThrows(IllegalArgumentException.class, () -> userService.updatePreferences(1L, request));
  }

  private void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
