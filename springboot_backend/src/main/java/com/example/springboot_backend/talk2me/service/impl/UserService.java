package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.LikeDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.PostViewDO;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostViewMapper;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements IUserService {

  private final UserMapper userMapper;
  private final UserStatsMapper userStatsMapper;
  private final PostViewMapper postViewMapper;
  private final LikeMapper likeMapper;
  private final ObjectMapper objectMapper;

  @Value("${upload.avatar.path:uploads/avatars}")
  private String avatarUploadPath;

  @Value("${user.preferences.max-length:8192}")
  private int maxPreferencesLength;

  public UserService(
      UserMapper userMapper,
      UserStatsMapper userStatsMapper,
      PostViewMapper postViewMapper,
      LikeMapper likeMapper,
      ObjectMapper objectMapper) {
    this.userMapper = userMapper;
    this.userStatsMapper = userStatsMapper;
    this.postViewMapper = postViewMapper;
    this.likeMapper = likeMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public UserProfileResponse getProfile(Long userId) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    UserStatsDO stats = userStatsMapper.selectById(userId);
    if (stats == null) {
      stats = initUserStats(userId);
    }

    return buildProfileResponse(user, stats);
  }

  @Override
  public Page<PostDO> listViewedPosts(Long userId, Integer page, Integer size, String order) {
    Page<PostDO> pageParam = new Page<>(page, size);
    Page<PostDO> result = postViewMapper.selectViewedPosts(pageParam, userId, isAscending(order));
    fillPostLikedState(result.getRecords(), userId);
    return result;
  }

  @Override
  @Transactional
  public void deleteViewedPost(Long userId, Long postId) {
    LambdaQueryWrapper<PostViewDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostViewDO::getUserId, userId).eq(PostViewDO::getPostId, postId);
    PostViewDO postView = postViewMapper.selectOne(wrapper);
    if (postView == null || isDeleted(postView.getHistoryDeleted())) {
      return;
    }

    postView.setHistoryDeleted(1);
    postView.setUpdateTime(LocalDateTime.now());
    postViewMapper.updateById(postView);
  }

  @Override
  @Transactional
  public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    user.setBio(request.getBio());
    user.setBirthday(request.getBirthday());
    user.setGender(request.getGender());
    user.setOccupation(request.getOccupation());
    user.setUpdateTime(LocalDateTime.now());

    userMapper.updateById(user);

    UserStatsDO stats = userStatsMapper.selectById(userId);
    if (stats == null) {
      stats = initUserStats(userId);
    }

    return buildProfileResponse(user, stats);
  }

  @Override
  public JsonNode getPreferences(Long userId) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    return mergedPreferences(user.getPreferences());
  }

  @Override
  @Transactional
  public JsonNode updatePreferences(Long userId, JsonNode preferences) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    JsonNode validated = validateAndNormalizePreferences(preferences);
    persistPreferences(user, validated);
    return validated;
  }

  @Override
  @Transactional
  public JsonNode patchPreferences(Long userId, JsonNode patchPreferences) {
    UserDO user = userMapper.selectById(userId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    if (patchPreferences == null || patchPreferences.isNull() || !patchPreferences.isObject()) {
      throw new IllegalArgumentException("patchPreferences 必须是 JSON 对象");
    }

    JsonNode current = mergedPreferences(user.getPreferences());
    ObjectNode mergedPatch = merge((ObjectNode) current.deepCopy(), patchPreferences);
    JsonNode validated = validateAndNormalizePreferences(mergedPatch);
    persistPreferences(user, validated);
    return validated;
  }

  private void persistPreferences(UserDO user, JsonNode validatedPreferences) {
    String serialized;
    try {
      serialized = objectMapper.writeValueAsString(validatedPreferences);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("用户配置序列化失败", e);
    }

    int payloadLength = serialized.getBytes(StandardCharsets.UTF_8).length;
    if (payloadLength > maxPreferencesLength) {
      throw new IllegalArgumentException("preferences 大小超过限制: " + maxPreferencesLength + " bytes");
    }

    user.setPreferences(serialized);
    user.setUpdateTime(LocalDateTime.now());
    userMapper.updateById(user);
  }

  @Override
  @Transactional
  public String uploadAvatar(Long userId, MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("文件不能为空");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    String filename = UUID.randomUUID().toString() + extension;

    try {
      Path uploadDir = Paths.get(avatarUploadPath).toAbsolutePath().normalize();
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      Path filePath = uploadDir.resolve(filename).normalize();
      file.transferTo(filePath);

      String avatarUrl = "/avatars/" + filename;

      UserDO user = userMapper.selectById(userId);
      if (user == null) {
        throw new RuntimeException("用户不存在");
      }
      user.setAvatar(avatarUrl);
      user.setUpdateTime(LocalDateTime.now());
      userMapper.updateById(user);

      return avatarUrl;
    } catch (IOException e) {
      throw new RuntimeException("文件上传失败", e);
    }
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new IllegalArgumentException("文件名不能为空");
    }
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
      throw new IllegalArgumentException("文件必须包含有效后缀名");
    }
    return originalFilename.substring(dotIndex);
  }

  private UserStatsDO initUserStats(Long userId) {
    UserStatsDO stats = new UserStatsDO();
    stats.setUserId(userId);
    stats.setLikeCount(0);
    stats.setFollowerCount(0);
    stats.setFollowingCount(0);
    stats.setCreateTime(LocalDateTime.now());
    stats.setUpdateTime(LocalDateTime.now());
    userStatsMapper.insert(stats);
    return stats;
  }

  private UserProfileResponse buildProfileResponse(UserDO user, UserStatsDO stats) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setBio(user.getBio());
    response.setAvatar(user.getAvatar());
    response.setBirthday(user.getBirthday());
    response.setGender(user.getGender());
    response.setOccupation(user.getOccupation());
    response.setLikeCount(stats.getLikeCount());
    response.setFollowerCount(stats.getFollowerCount());
    response.setFollowingCount(stats.getFollowingCount());
    return response;
  }

  private boolean isAscending(String order) {
    if (order == null || order.isBlank()) {
      return false;
    }

    String normalized = order.toLowerCase(Locale.ROOT);
    if ("asc".equals(normalized)) {
      return true;
    }
    if ("desc".equals(normalized)) {
      return false;
    }
    throw new IllegalArgumentException("排序参数仅支持 asc 或 desc");
  }

  private boolean isDeleted(Integer deleted) {
    return deleted != null && deleted != 0;
  }

  private void fillPostLikedState(List<PostDO> posts, Long currentUserId) {
    if (posts == null || posts.isEmpty()) {
      return;
    }

    Set<Long> likedPostIds =
        getLikedPostIds(currentUserId, posts.stream().map(PostDO::getId).toList());
    posts.forEach(post -> post.setIsLiked(likedPostIds.contains(post.getId())));
  }

  private Set<Long> getLikedPostIds(Long currentUserId, List<Long> postIds) {
    if (currentUserId == null || postIds == null || postIds.isEmpty()) {
      return Collections.emptySet();
    }

    LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(LikeDO::getUserId, currentUserId)
        .eq(LikeDO::getTargetType, "POST")
        .in(LikeDO::getTargetId, postIds);
    return likeMapper.selectList(wrapper).stream()
        .map(LikeDO::getTargetId)
        .collect(Collectors.toSet());
  }

  private JsonNode mergedPreferences(String rawPreferences) {
    ObjectNode defaults = defaultPreferences();
    if (rawPreferences == null || rawPreferences.isBlank()) {
      return defaults;
    }
    try {
      JsonNode parsed = objectMapper.readTree(rawPreferences);
      if (parsed == null || !parsed.isObject()) {
        return defaults;
      }
      return merge(defaults, parsed);
    } catch (JsonProcessingException e) {
      return defaults;
    }
  }

  private JsonNode validateAndNormalizePreferences(JsonNode preferences) {
    if (preferences == null || preferences.isNull() || !preferences.isObject()) {
      throw new IllegalArgumentException("preferences 必须是 JSON 对象");
    }

    ObjectNode merged = merge(defaultPreferences(), preferences);
    validateTopLevel(merged);
    validateNotification(merged.path("notification"));
    return merged;
  }

  private void validateTopLevel(JsonNode preferences) {
    preferences
        .fieldNames()
        .forEachRemaining(
            field -> {
              if (!"theme".equals(field)
                  && !"language".equals(field)
                  && !"notification".equals(field)) {
                throw new IllegalArgumentException("不支持的配置字段: " + field);
              }
            });

    String theme = preferences.path("theme").asText();
    if (!"system".equals(theme) && !"light".equals(theme) && !"dark".equals(theme)) {
      throw new IllegalArgumentException("theme 仅支持 system/light/dark");
    }

    String language = preferences.path("language").asText();
    if (language.isBlank() || language.length() > 20) {
      throw new IllegalArgumentException("language 长度必须在 1-20 之间");
    }
  }

  private void validateNotification(JsonNode notification) {
    if (!notification.isObject()) {
      throw new IllegalArgumentException("notification 必须是对象");
    }
    notification
        .fieldNames()
        .forEachRemaining(
            field -> {
              boolean supported =
                  "enableWs".equals(field)
                      || "muteLike".equals(field)
                      || "muteReply".equals(field)
                      || "muteFollow".equals(field)
                      || "muteFolloweePost".equals(field);
              if (!supported) {
                throw new IllegalArgumentException("不支持的 notification 字段: " + field);
              }
              if (!notification.path(field).isBoolean()) {
                throw new IllegalArgumentException(field + " 必须是布尔值");
              }
            });
  }

  private ObjectNode defaultPreferences() {
    ObjectNode defaults = objectMapper.createObjectNode();
    defaults.put("theme", "system");
    defaults.put("language", "zh-CN");

    ObjectNode notification = objectMapper.createObjectNode();
    notification.put("enableWs", true);
    notification.put("muteLike", false);
    notification.put("muteReply", false);
    notification.put("muteFollow", false);
    notification.put("muteFolloweePost", false);
    defaults.set("notification", notification);
    return defaults;
  }

  private ObjectNode merge(ObjectNode base, JsonNode patch) {
    if (patch == null || !patch.isObject()) {
      return base;
    }
    patch
        .fieldNames()
        .forEachRemaining(
            field -> {
              JsonNode patchValue = patch.get(field);
              JsonNode baseValue = base.get(field);
              if (baseValue != null
                  && baseValue.isObject()
                  && patchValue != null
                  && patchValue.isObject()) {
                base.set(field, merge((ObjectNode) baseValue.deepCopy(), patchValue));
              } else {
                base.set(field, patchValue);
              }
            });
    return base;
  }
}
