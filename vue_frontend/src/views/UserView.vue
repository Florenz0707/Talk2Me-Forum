<template>
  <div
    class="user-view-container"
    :class="{
      'bg-gradient-complete': bgGradientComplete,
      'bg-fade-leave-active': isLeaving,
      'fade-enter-active': isEntering,
    }"
  >
    <!-- 头像展示栏 -->
    <div class="avatar-header">
      <div class="avatar-header-content">
        <div
          class="avatar-wrapper"
          @mouseenter="showAvatarOverlay = true"
          @mouseleave="showAvatarOverlay = false"
        >
          <div class="user-avatar-large">
            <i v-if="!userAvatar" class="fas fa-user-circle"></i>
            <img v-else :src="userAvatar" alt="用户头像" class="avatar-image" />
          </div>
          <div
            class="avatar-overlay"
            :class="{ show: showAvatarOverlay }"
            @click="triggerFileInput"
          >
            <span class="change-avatar-text">更换头像</span>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </div>
        <div class="user-info">
          <div class="username-large">{{ username }}</div>
          <div class="user-bio">{{ editBio }}</div>
        </div>
      </div>
    </div>

    <!-- 数据指标行 -->
    <div class="stats-container">
      <div class="stats-wrapper">
        <div
          class="stat-item"
          :class="{ 'stat-active': activeStatsTab === 'likes' }"
          @click="handleStatsClick('likes')"
        >
          <div class="stat-number">{{ likesCount }}</div>
          <div class="stat-label">获赞数</div>
        </div>
        <div
          class="stat-item"
          :class="{ 'stat-active': activeStatsTab === 'following' }"
          @click="handleStatsClick('following')"
        >
          <div class="stat-number">{{ followingCount }}</div>
          <div class="stat-label">关注数</div>
        </div>
        <div
          class="stat-item"
          :class="{ 'stat-active': activeStatsTab === 'followers' }"
          @click="handleStatsClick('followers')"
        >
          <div class="stat-number">{{ followersCount }}</div>
          <div class="stat-label">粉丝数</div>
        </div>
      </div>
    </div>

    <!-- 主内容容器 -->
    <div class="main-content-container">
      <!-- 主内容区域 -->
      <div class="main-content-wrapper">
        <!-- 左侧导航栏 -->
        <div class="left-sidebar">
          <div class="nav-menu">
            <div
              class="nav-item"
              :class="{ active: activeNavItem === 'messages' }"
              @click="
                activeNavItem = 'messages';
                activeStatsTab = null;
              "
            >
              <i class="fas fa-envelope"></i>
              <span>我的消息</span>
            </div>
            <!--            <div-->
            <!--              class="nav-item"-->
            <!--              :class="{ active: activeNavItem === 'favorites' }"-->
            <!--              @click="-->
            <!--                activeNavItem = 'favorites';-->
            <!--                activeStatsTab = null;-->
            <!--              "-->
            <!--            >-->
            <!--              <i class="fas fa-star"></i>-->
            <!--              <span>收藏</span>-->
            <!--            </div>-->
            <div
              class="nav-item"
              :class="{ active: activeNavItem === 'profile' }"
              @click="
                activeNavItem = 'profile';
                activeStatsTab = null;
              "
            >
              <i class="fas fa-user"></i>
              <span>个人资料</span>
            </div>
            <div
              class="nav-item"
              :class="{ active: activeNavItem === 'settings' }"
              @click="
                activeNavItem = 'settings';
                activeStatsTab = null;
              "
            >
              <i class="fas fa-cog"></i>
              <span>设置</span>
            </div>
            <router-link to="/" class="nav-item">
              <i class="fas fa-home"></i>
              <span>回到主页</span>
            </router-link>
            <div class="nav-item logout-item" @click="handleLogout">
              <i class="fas fa-sign-out-alt"></i>
              <span>退出登录</span>
            </div>
          </div>
        </div>

        <!-- 右侧内容区域 -->
        <div class="right-content">
          <!-- 动态内容区域 -->
          <div class="content-card">
            <!-- 关注列表 -->
            <div
              v-if="activeStatsTab === 'following'"
              class="user-list-container"
            >
              <h2>我的关注</h2>
              <div v-if="followingLoading" class="empty-state">
                <i class="fas fa-spinner fa-spin"></i>
                <p>加载中...</p>
              </div>
              <div v-else-if="followingList.length === 0" class="empty-state">
                <i class="fas fa-user-friends"></i>
                <p>暂无关注</p>
              </div>
              <div v-else class="user-list">
                <div
                  v-for="user in followingList"
                  :key="user.id"
                  class="user-item"
                >
                  <div class="user-item-avatar">
                    <i class="fas fa-user-circle"></i>
                  </div>
                  <div class="user-item-info">
                    <div class="user-item-name">{{ user.username }}</div>
                    <div class="user-item-bio">
                      {{ user.bio || "暂无简介" }}
                    </div>
                  </div>
                  <button class="unfollow-btn" @click="handleUnfollow(user.id)">
                    取消关注
                  </button>
                </div>
              </div>
            </div>

            <!-- 粉丝列表 -->
            <div
              v-else-if="activeStatsTab === 'followers'"
              class="user-list-container"
            >
              <h2>我的粉丝</h2>
              <div v-if="followersLoading" class="empty-state">
                <i class="fas fa-spinner fa-spin"></i>
                <p>加载中...</p>
              </div>
              <div v-else-if="followersList.length === 0" class="empty-state">
                <i class="fas fa-users"></i>
                <p>暂无粉丝</p>
              </div>
              <div v-else class="user-list">
                <div
                  v-for="user in followersList"
                  :key="user.id"
                  class="user-item"
                >
                  <div class="user-item-avatar">
                    <i class="fas fa-user-circle"></i>
                  </div>
                  <div class="user-item-info">
                    <div class="user-item-name">{{ user.username }}</div>
                    <div class="user-item-bio">
                      {{ user.bio || "暂无简介" }}
                    </div>
                  </div>
                  <button
                    class="follow-back-btn"
                    @click="handleFollowBack(user.id)"
                  >
                    回关
                  </button>
                </div>
              </div>
            </div>

            <!-- 我的消息内容 -->
            <div v-if="activeNavItem === 'messages'" class="messages-container">
              <h2>我的消息</h2>

              <!-- 消息导航栏 -->
              <div class="message-tabs">
                <div
                  class="message-tab"
                  :class="{ active: activeMessageTab === 'likes' }"
                  @click="activeMessageTab = 'likes'"
                >
                  <span v-if="likeUnreadCount > 0" class="message-tab-badge">
                    {{ formatUnreadCount(likeUnreadCount) }}
                  </span>
                  收到的赞
                </div>
                <div
                  class="message-tab"
                  :class="{ active: activeMessageTab === 'replies' }"
                  @click="activeMessageTab = 'replies'"
                >
                  <span v-if="replyUnreadCount > 0" class="message-tab-badge">
                    {{ formatUnreadCount(replyUnreadCount) }}
                  </span>
                  回复我的
                </div>
                <div
                  class="message-tab"
                  :class="{ active: activeMessageTab === 'followers' }"
                  @click="activeMessageTab = 'followers'"
                >
                  <span v-if="followUnreadCount > 0" class="message-tab-badge">
                    {{ formatUnreadCount(followUnreadCount) }}
                  </span>
                  新的粉丝
                </div>
                <div
                  class="message-tab"
                  :class="{ active: activeMessageTab === 'private' }"
                  @click="activeMessageTab = 'private'"
                >
                  我的私信
                </div>
              </div>

              <!-- 收到的赞 -->
              <div
                v-if="activeMessageTab === 'likes'"
                class="notification-list"
              >
                <div v-if="likeNotifications.length === 0" class="empty-state">
                  <i class="fas fa-heart"></i>
                  <p>暂无点赞通知</p>
                </div>
                <div v-else>
                  <div
                    v-for="notif in likeNotifications"
                    :key="notif.id"
                    class="notification-item"
                    :class="{ unread: !notif.isRead }"
                    @click="handleNotificationClick(notif)"
                  >
                    <i class="fas fa-heart notif-icon"></i>
                    <div class="notif-content">
                      <span class="notif-text">{{ notif.content }}</span>
                      <span class="notif-time">{{
                        formatTime(notif.createTime)
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 回复我的 -->
              <div
                v-if="activeMessageTab === 'replies'"
                class="notification-list"
              >
                <div v-if="replyNotifications.length === 0" class="empty-state">
                  <i class="fas fa-comment"></i>
                  <p>暂无回复通知</p>
                </div>
                <div v-else>
                  <div
                    v-for="notif in replyNotifications"
                    :key="notif.id"
                    class="notification-item"
                    :class="{ unread: !notif.isRead }"
                    @click="handleNotificationClick(notif)"
                  >
                    <i class="fas fa-comment notif-icon"></i>
                    <div class="notif-content">
                      <span class="notif-text">{{ notif.content }}</span>
                      <span class="notif-time">{{
                        formatTime(notif.createTime)
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 我的私信 -->
              <div
                v-if="activeMessageTab === 'followers'"
                class="notification-list"
              >
                <div
                  v-if="followNotifications.length === 0"
                  class="empty-state"
                >
                  <i class="fas fa-user-plus"></i>
                  <p>暂无新的粉丝通知</p>
                </div>
                <div v-else>
                  <div
                    v-for="notif in followNotifications"
                    :key="notif.id"
                    class="notification-item"
                    :class="{ unread: !notif.isRead }"
                    @click="handleNotificationClick(notif)"
                  >
                    <i class="fas fa-user-plus notif-icon"></i>
                    <div class="notif-content">
                      <span class="notif-text">{{ notif.content }}</span>
                      <span class="notif-time">{{
                        formatTime(notif.createTime)
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div
                v-if="activeMessageTab === 'private'"
                class="messages-layout"
              >
                <!-- 左侧联系人列表 -->
                <div
                  v-if="messageContacts.length > 0 || newMessageTarget"
                  class="contacts-sidebar"
                >
                  <!-- 目标用户信息栏 -->
                  <div v-if="newMessageTarget" class="target-user-card">
                    <div class="target-user-avatar">
                      <i class="fas fa-user-circle"></i>
                    </div>
                    <div class="target-user-name">{{ newMessageTarget }}</div>
                  </div>
                  <div
                    v-for="contact in messageContacts"
                    :key="contact.id"
                    class="contact-item"
                    :class="{ active: selectedContact?.id === contact.id }"
                    @click="selectContact(contact)"
                  >
                    <i class="fas fa-user-circle"></i>
                    <span>{{ contact.name }}</span>
                  </div>
                </div>

                <!-- 右侧消息输入区 -->
                <div class="message-input-area">
                  <div v-if="newMessageTarget" class="new-message-form">
                    <h3>发送消息给 {{ newMessageTarget }}</h3>
                    <textarea
                      v-model="newMessageContent"
                      class="message-textarea"
                      placeholder="输入消息内容..."
                    ></textarea>
                    <div class="message-actions">
                      <button class="send-btn" @click="handleSendMessage">
                        发送
                      </button>
                      <button class="cancel-btn" @click="cancelNewMessage">
                        取消
                      </button>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    <i class="fas fa-envelope-open"></i>
                    <p>暂无新消息</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 收藏内容 -->
            <!--            <div v-else-if="activeNavItem === 'favorites'">-->
            <!--              <h2>默认收藏夹</h2>-->
            <!--              <div v-if="favoritesLoading" class="empty-state">-->
            <!--                <i class="fas fa-spinner fa-spin"></i>-->
            <!--                <p>加载中...</p>-->
            <!--              </div>-->
            <!--              <div v-else-if="favoriteThreads.length === 0" class="empty-state">-->
            <!--                <i class="fas fa-star"></i>-->
            <!--                <p>暂无收藏</p>-->
            <!--              </div>-->
            <!--              <div v-else class="thread-table-container">-->
            <!--                <table class="thread-table">-->
            <!--                  <thead>-->
            <!--                    <tr class="thread-header">-->
            <!--                      <th class="thread-info">帖子</th>-->
            <!--                      <th class="thread-author">作者</th>-->
            <!--                      <th class="thread-time">时间</th>-->
            <!--                      <th class="thread-replies">回复</th>-->
            <!--                      <th class="thread-views">浏览</th>-->
            <!--                    </tr>-->
            <!--                  </thead>-->
            <!--                  <tbody>-->
            <!--                    <tr-->
            <!--                      v-for="thread in favoriteThreads"-->
            <!--                      :key="thread.id"-->
            <!--                      class="thread-item"-->
            <!--                    >-->
            <!--                      <td class="thread-info">-->
            <!--                        <div class="thread-title">-->
            <!--                          <router-link-->
            <!--                            :to="'/thread/' + thread.id"-->
            <!--                            :title="thread.title"-->
            <!--                            >{{ thread.title }}</router-link-->
            <!--                          >-->
            <!--                        </div>-->
            <!--                      </td>-->
            <!--                      <td class="thread-author">-->
            <!--                        <span class="author-name">{{ thread.author }}</span>-->
            <!--                      </td>-->
            <!--                      <td class="thread-time">-->
            <!--                        {{ formatTime(thread.createdAt) }}-->
            <!--                      </td>-->
            <!--                      <td class="thread-replies">{{ thread.replies }}</td>-->
            <!--                      <td class="thread-views">{{ thread.views }}</td>-->
            <!--                    </tr>-->
            <!--                  </tbody>-->
            <!--                </table>-->
            <!--              </div>-->
            <!--            </div>-->

            <!-- 个人资料内容 -->
            <div v-else-if="activeNavItem === 'profile'">
              <h2>个人资料</h2>
              <div class="profile-form">
                <div class="form-group">
                  <label>用户名</label>
                  <input
                    type="text"
                    :value="editUsername"
                    disabled
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>个性签名</label>
                  <textarea v-model="editBio" class="form-textarea"></textarea>
                </div>
                <div class="form-group">
                  <label>生日</label>
                  <input
                    type="date"
                    v-model="editBirthday"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>性别</label>
                  <select v-model="editGender" class="form-input">
                    <option value="">请选择</option>
                    <option value="MALE">男</option>
                    <option value="FEMALE">女</option>
                    <option value="OTHER">其他</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>职业</label>
                  <input
                    type="text"
                    v-model="editOccupation"
                    class="form-input"
                    placeholder="请输入职业"
                  />
                </div>
                <button class="save-btn" @click="handleSaveProfile">
                  保存修改
                </button>
              </div>
            </div>

            <!-- 设置内容 -->
            <div v-else-if="activeNavItem === 'settings'">
              <h2>设置</h2>
              <div class="settings-list">
                <div class="setting-item">
                  <span>通知设置</span>
                  <label class="switch">
                    <input type="checkbox" checked />
                    <span class="slider"></span>
                  </label>
                </div>
                <div class="setting-item">
                  <span>隐私设置</span>
                  <label class="switch">
                    <input type="checkbox" checked />
                    <span class="slider"></span>
                  </label>
                </div>
                <div class="setting-item">
                  <span>深色模式</span>
                  <label class="switch">
                    <input
                      type="checkbox"
                      :checked="isDarkMode"
                      @change="toggleDarkMode"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, onBeforeUnmount, inject, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { authApi, userApi, notificationApi } from "../utils/api";
import { notificationWS } from "../utils/websocket";
import { setUserAvatar } from "../utils/authStorage";
import {
  applyIncomingNotification,
  buildEffectiveLikeNotifications,
  markNotificationsAsReadInSummary,
  notificationSummary,
  markNotificationAsReadInSummary,
  normalizeNotification,
  refreshNotificationSummary,
} from "../utils/notificationState";

export default {
  name: "UserView",
  setup() {
    const router = useRouter();
    const route = useRoute();
    const updateLoginStatus = inject("updateLoginStatus");
    const isLoggedIn = inject("isLoggedIn");
    const isDarkMode = inject("isDarkMode");
    const toggleDarkMode = inject("toggleDarkMode");
    const username = ref("");
    const userEmail = ref("");
    const isLoggingOut = ref(false);
    const bgGradientComplete = ref(false);
    const isLeaving = ref(false);
    const isEntering = ref(true);
    let navigationGuard = null;
    let unsubscribeNotification = null;

    // 导航栏选中项
    const activeNavItem = ref("messages");

    // 消息导航栏
    const activeMessageTab = ref("likes");

    // 通知列表
    const likeNotifications = ref([]);
    const replyNotifications = ref([]);
    const followNotifications = ref([]);
    const notificationStatsLoaded = ref(false);
    const NOTIFICATION_PAGE_SIZE = 100;
    const NOTIFICATION_MAX_PAGES = 20;

    // 数据指标导航
    const activeStatsTab = ref(null);
    const profileLikesCount = ref(0);
    const profileFollowingCount = ref(0);
    const profileFollowersCount = ref(0);

    // 关注列表
    const followingList = ref([]);
    const followingLoading = ref(false);

    // 粉丝列表
    const followersList = ref([]);
    const followersLoading = ref(false);

    // 新消息相关
    const newMessageTarget = ref("");
    const newMessageTargetId = ref("");
    const newMessageContent = ref("");
    const messageContacts = ref([]);
    const selectedContact = ref(null);

    // 搜索功能
    const searchQuery = ref("");
    const showUserMenu = ref(false);

    // 头像相关
    const userAvatar = ref("");
    const showAvatarOverlay = ref(false);
    const fileInput = ref(null);
    const likeUnreadCount = computed(() => notificationSummary.byType.LIKE);
    const replyUnreadCount = computed(() => notificationSummary.byType.REPLY);
    const followUnreadCount = computed(() => notificationSummary.byType.FOLLOW);
    const likesCount = computed(() => profileLikesCount.value);
    const followingCount = computed(() => profileFollowingCount.value);
    const followersCount = computed(() =>
      notificationStatsLoaded.value
        ? Math.max(
            profileFollowersCount.value,
            followNotifications.value.length,
          )
        : profileFollowersCount.value,
    );

    // 收藏的帖子数据（从后端拉取，暂无接口时为空）
    const favoriteThreads = ref([]);
    const favoritesLoading = ref(false);

    // 格式化时间函数
    const formatTime = (timeString) => {
      const date = new Date(timeString);
      return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    const formatUnreadCount = (count) => (count > 99 ? "99+" : count);

    const prependNotification = (listRef, notification) => {
      const hasSameId =
        notification?.id !== undefined &&
        listRef.value.some((item) => item.id === notification.id);

      if (!hasSameId) {
        listRef.value.unshift(notification);
        return true;
      }

      return false;
    };

    const getNotificationsByTab = (tab) => {
      if (tab === "likes") return likeNotifications.value;
      if (tab === "replies") return replyNotifications.value;
      if (tab === "followers") return followNotifications.value;
      return [];
    };

    const fetchNotificationRecords = async () => {
      const records = [];
      let page = 1;
      let totalPages = 1;

      while (page <= totalPages && page <= NOTIFICATION_MAX_PAGES) {
        const res = await notificationApi.getNotifications({
          page,
          size: NOTIFICATION_PAGE_SIZE,
        });
        const pageData = res?.data || {};
        const pageRecords = Array.isArray(pageData.records)
          ? pageData.records
          : [];

        records.push(...pageRecords.map(normalizeNotification));

        if (typeof pageData.pages === "number" && pageData.pages > 0) {
          totalPages = pageData.pages;
        } else if (typeof pageData.total === "number") {
          totalPages = Math.max(
            1,
            Math.ceil(pageData.total / NOTIFICATION_PAGE_SIZE),
          );
        } else if (pageRecords.length < NOTIFICATION_PAGE_SIZE) {
          totalPages = page;
        } else {
          totalPages = page + 1;
        }

        if (pageRecords.length === 0) {
          break;
        }

        page += 1;
      }

      return records;
    };

    const markTabNotificationsAsRead = async (tab) => {
      const unreadNotifications = getNotificationsByTab(tab).filter(
        (notif) => !notif?.isRead && notif?.id != null,
      );

      if (!unreadNotifications.length) {
        return;
      }

      const results = await Promise.allSettled(
        unreadNotifications.map((notif) => notificationApi.markRead(notif.id)),
      );
      const markedNotifications = [];

      results.forEach((result, index) => {
        if (result.status !== "fulfilled") {
          console.error("标记通知已读失败:", result.reason);
          return;
        }

        const notification = unreadNotifications[index];
        notification.isRead = true;
        markedNotifications.push(notification);
      });

      markNotificationsAsReadInSummary(markedNotifications);
    };

    const applyRouteState = (query = {}) => {
      if (query.tab) {
        activeNavItem.value = query.tab;
        activeStatsTab.value = null;
      }

      if (query.messageTab) {
        activeMessageTab.value = query.messageTab;
        activeNavItem.value = "messages";
      }

      if (query.statsTab) {
        activeStatsTab.value = query.statsTab;
        activeNavItem.value = null;
      }

      if (query.targetUser) {
        newMessageTarget.value = query.targetUser;
        newMessageTargetId.value = query.targetUserId || "";
        selectedContact.value = {
          id: query.targetUserId || "",
          name: query.targetUser,
        };
      }
    };

    // 编辑个人资料
    const editUsername = ref("");
    const editBio = ref("");
    const editBirthday = ref("");
    const editGender = ref("");
    const editOccupation = ref("");

    // 保存个人资料
    const handleSaveProfile = async () => {
      try {
        const profileData = {
          bio: editBio.value,
          birthday: editBirthday.value || null,
          gender: editGender.value || null,
          occupation: editOccupation.value || null,
        };
        const response = await userApi.updateProfile(profileData);
        if (response.data) {
          const p = response.data;
          if (p.bio !== undefined) editBio.value = p.bio || "";
          if (p.birthday !== undefined) editBirthday.value = p.birthday || "";
          if (p.gender !== undefined) editGender.value = p.gender || "";
          if (p.occupation !== undefined)
            editOccupation.value = p.occupation || "";
        }
        alert("个人资料保存成功");
      } catch (error) {
        console.error("保存个人资料失败:", error);
        alert("保存失败，请稍后重试");
      }
    };

    // 触发文件选择
    const triggerFileInput = () => {
      fileInput.value.click();
    };

    // 处理头像更换
    const handleAvatarChange = async (event) => {
      const file = event.target.files[0];
      if (!file) return;

      if (!file.type.startsWith("image/")) {
        alert("请选择图片文件");
        return;
      }

      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        alert("图片大小不能超过5MB");
        return;
      }

      try {
        const response = await userApi.uploadAvatar(file);
        // API 返回 ResultString: { code, message, data: "url_string" }
        if (response.data) {
          userAvatar.value = response.data;
          setUserAvatar(response.data);
          alert("头像上传成功");
        }
      } catch (error) {
        console.error("上传头像失败:", error);
        alert("上传失败，请稍后重试");
      }
    };

    // 获取当前登录用户信息（含完整 profile）
    const fetchUserInfo = async () => {
      // 先从 token 取用户名作为初始值
      const nameFromToken = authApi.getUsernameFromToken();
      if (nameFromToken) {
        username.value = nameFromToken;
        editUsername.value = nameFromToken;
      } else {
        const remembered = localStorage.getItem("rememberedUsername");
        username.value = remembered || "用户";
        editUsername.value = username.value;
      }
      // 从后端拉取完整 profile
      try {
        const res = await userApi.getCurrentProfile();
        if (res.data) {
          const p = res.data;
          if (p.username) {
            username.value = p.username;
            editUsername.value = p.username;
          }
          if (p.bio) editBio.value = p.bio;
          if (p.avatar) {
            userAvatar.value = p.avatar;
            setUserAvatar(p.avatar);
          }
          if (p.birthday) editBirthday.value = p.birthday;
          if (p.gender) editGender.value = p.gender;
          if (p.occupation) editOccupation.value = p.occupation;
          profileLikesCount.value = p.likeCount || 0;
          profileFollowingCount.value = p.followingCount || 0;
          profileFollowersCount.value = p.followerCount || 0;
        }
      } catch (error) {
        console.error("获取用户资料失败:", error);
      }
    };

    // 加载通知列表
    const loadNotifications = async () => {
      try {
        const records = await fetchNotificationRecords();
        likeNotifications.value = buildEffectiveLikeNotifications(records);
        replyNotifications.value = records.filter((n) => n.type === "REPLY");
        followNotifications.value = records.filter((n) => n.type === "FOLLOW");
        notificationStatsLoaded.value = true;
        if (activeNavItem.value === "messages") {
          await markTabNotificationsAsRead(activeMessageTab.value);
        }
      } catch (error) {
        console.error("加载通知失败:", error);
      }
    };

    // 处理WebSocket通知
    const handleNotification = (notification) => {
      const normalizedNotification = applyIncomingNotification(notification);
      if (normalizedNotification.type === "LIKE") {
        likeNotifications.value = buildEffectiveLikeNotifications([
          normalizedNotification,
          ...likeNotifications.value,
        ]);
        notificationStatsLoaded.value = true;
        fetchUserInfo();
      } else if (normalizedNotification.type === "REPLY") {
        prependNotification(replyNotifications, normalizedNotification);
      } else if (normalizedNotification.type === "FOLLOW") {
        prependNotification(followNotifications, normalizedNotification);
        notificationStatsLoaded.value = true;
      }
    };

    // 退出登录
    const handleNotificationClick = async (notif) => {
      if (notif?.isRead || notif?.id == null) {
        return;
      }

      try {
        await notificationApi.markRead(notif.id);
        notif.isRead = true;
        markNotificationAsReadInSummary(notif);
      } catch (error) {
        console.error("鏍囪閫氱煡宸茶澶辫触:", error);
      }
    };

    const handleLogout = async () => {
      isLoggingOut.value = true;
      try {
        // 调用退出登录API
        await authApi.logout();
        console.log("退出登录成功");

        // 清除本地存储的用户信息和Token
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("rememberedUsername");
        localStorage.removeItem("auth_token");

        // 更新全局登录状态
        updateLoginStatus(false);

        // 导航到主页（路由守卫会触发离开动画）
        router.push("/");
      } catch (error) {
        console.error("退出登录失败:", error);
        // 即使API调用失败，仍然清除本地存储并跳转到主页
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("rememberedUsername");
        localStorage.removeItem("auth_token");

        // 更新全局登录状态
        updateLoginStatus(false);

        // 导航到主页（路由守卫会触发离开动画）
        router.push("/");
      } finally {
        isLoggingOut.value = false;
      }
    };

    // 监听 activeStatsTab 变化
    watch(activeStatsTab, (newTab) => {
      if (newTab === "following") {
        fetchFollowing();
      } else if (newTab === "followers") {
        fetchFollowers();
      }
    });

    watch([activeNavItem, activeMessageTab], ([navItem, messageTab]) => {
      if (navItem !== "messages") {
        return;
      }

      markTabNotificationsAsRead(messageTab);
    });

    // 401 后用户重新登录时，自动重新加载用户信息
    watch(isLoggedIn, (newValue, oldValue) => {
      if (newValue && !oldValue) {
        fetchUserInfo();
        loadNotifications();
        refreshNotificationSummary();
      }
    });

    // 页面挂载时获取用户信息
    watch(
      () => route.fullPath,
      () => {
        applyRouteState(route.query);
      },
    );

    onMounted(() => {
      // 未登录则跳回主页并弹出登录框
      if (!authApi.isAuthenticated()) {
        window.dispatchEvent(new CustomEvent("open-login-modal"));
        router.push("/");
        return;
      }

      applyRouteState(route.query);
      fetchUserInfo();
      fetchStats();
      loadNotifications();
      refreshNotificationSummary();

      // 连接WebSocket
      notificationWS.connect();
      unsubscribeNotification =
        notificationWS.onNotification(handleNotification);

      // 进入动画：延迟清除 isEntering，让 keyframe 动画完整播放
      setTimeout(() => {
        isEntering.value = false;
      }, 600);

      // 触发背景渐变动画
      setTimeout(() => {
        bgGradientComplete.value = true;

        // 背景渐变完成后触发卡片弹出动画
        setTimeout(() => {
          // 动画完成
        }, 300); // 0.3秒背景渐变时间
      }, 100); // 给DOM一点时间渲染

      // 设置导航守卫
      navigationGuard = router.beforeEach((to, from, next) => {
        if (from.path === route.path) {
          isLeaving.value = true;
          setTimeout(() => {
            next();
          }, 500);
        } else {
          next();
        }
      });
    });

    // 组件卸载时清除导航守卫
    onBeforeUnmount(() => {
      if (navigationGuard) {
        navigationGuard();
      }
      if (unsubscribeNotification) {
        unsubscribeNotification();
        unsubscribeNotification = null;
      }
    });

    // 处理搜索
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        console.log("搜索内容:", searchQuery.value);
        // 这里可以添加搜索逻辑，例如调用API或过滤本地数据
        // 暂时只打印搜索内容
      }
    };

    // 发送消息
    const handleSendMessage = async () => {
      if (!newMessageContent.value.trim()) {
        alert("请输入消息内容");
        return;
      }
      // TODO: 调用发送消息API
      console.log(
        "发送消息给:",
        newMessageTarget.value,
        newMessageContent.value,
      );
      alert("消息发送成功");
      cancelNewMessage();
    };

    // 取消新消息
    const cancelNewMessage = () => {
      newMessageTarget.value = "";
      newMessageTargetId.value = "";
      newMessageContent.value = "";
      selectedContact.value = null;
      router.replace({ path: "/user", query: { tab: "messages" } });
    };

    // 选择联系人
    const selectContact = (contact) => {
      selectedContact.value = contact;
      newMessageTarget.value = contact.name;
      newMessageTargetId.value = contact.id;
    };

    // 获取统计数据（由 fetchUserInfo 中的 getCurrentProfile 统一处理）
    const fetchStats = async () => {};

    // 获取关注列表
    const fetchFollowing = async () => {
      followingLoading.value = true;
      try {
        // TODO: 调用后端接口
        // const data = await userApi.getFollowing();
        // followingList.value = data;
        followingList.value = [];
      } catch (error) {
        console.error("获取关注列表失败:", error);
      } finally {
        followingLoading.value = false;
      }
    };

    // 获取粉丝列表
    const fetchFollowers = async () => {
      followersLoading.value = true;
      try {
        // TODO: 调用后端接口
        // const data = await userApi.getFollowers();
        // followersList.value = data;
        followersList.value = [];
      } catch (error) {
        console.error("获取粉丝列表失败:", error);
      } finally {
        followersLoading.value = false;
      }
    };

    // 取消关注
    const handleUnfollow = async (userId) => {
      // TODO: 调用后端接口
      // await userApi.unfollow(userId);
      console.log("取消关注用户:", userId);
      alert("取消关注成功");
      fetchFollowing();
    };

    // 回关
    const handleFollowBack = async (userId) => {
      // TODO: 调用后端接口
      // await userApi.follow(userId);
      console.log("回关用户:", userId);
      alert("关注成功");
      fetchFollowers();
    };

    // 处理数据指标点击
    const handleStatsClick = (tab) => {
      activeStatsTab.value = tab;
      if (tab === "likes") {
        activeNavItem.value = "messages";
        activeMessageTab.value = "likes";
      } else {
        activeNavItem.value = null;
      }
    };

    return {
      username,
      userEmail,
      isLoggingOut,
      handleLogout,
      bgGradientComplete,
      isLeaving,
      isEntering,
      activeNavItem,
      activeMessageTab,
      likeNotifications,
      replyNotifications,
      followNotifications,
      isLoggedIn,
      isDarkMode,
      toggleDarkMode,
      searchQuery,
      showUserMenu,
      handleSearch,
      favoriteThreads,
      favoritesLoading,
      formatTime,
      formatUnreadCount,
      editUsername,
      editBio,
      editBirthday,
      editGender,
      editOccupation,
      handleSaveProfile,
      userAvatar,
      showAvatarOverlay,
      fileInput,
      triggerFileInput,
      handleAvatarChange,
      newMessageTarget,
      newMessageTargetId,
      newMessageContent,
      messageContacts,
      selectedContact,
      selectContact,
      handleSendMessage,
      cancelNewMessage,
      handleNotificationClick,
      activeStatsTab,
      likeUnreadCount,
      replyUnreadCount,
      followUnreadCount,
      likesCount,
      followingCount,
      followersCount,
      followingList,
      followingLoading,
      followersList,
      followersLoading,
      handleUnfollow,
      handleFollowBack,
      handleStatsClick,
    };
  },
};
</script>

<style scoped>
/* 用户页面容器 */
.user-view-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
  transition: background-color 0.3s ease-in-out;
}

/* 头像展示栏 */
.avatar-header {
  background-color: #2c3e50;
  height: 20vh;
  min-height: 100px;
  display: flex;
  flex-direction: row;
  align-items: center;
  color: white;
}

.avatar-header-content {
  max-width: 1500px;
  width: 100%;
  margin: 0 auto;
  padding: 0 0px;
  display: flex;
  align-items: center;
  gap: 30px;
}

.user-avatar-large {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 120px;
  line-height: 1;
  padding: 0;
  margin: 0;
}

/* 头像包装器 */
.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  width: 120px;
  height: 120px;
  margin-left: -20px;
}

/* 头像图片 */
.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

/* 头像悬停遮罩层 */
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  border: 3px solid rgba(255, 255, 255, 0.5);
}

.avatar-overlay.show {
  opacity: 1;
  visibility: visible;
}

/* 更换头像文字 */
.change-avatar-text {
  color: white;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  user-select: none;
}

/* 头像悬停效果 */
.avatar-wrapper:hover .user-avatar-large {
  filter: brightness(0.8);
}

.avatar-wrapper:hover .avatar-image {
  filter: brightness(0.8);
}

.user-info {
  flex: 1;
}

.username-large {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.user-bio {
  font-size: 16px;
  color: #e0e0e0;
  line-height: 1.5;
}

/* 背景渐变完成状态 */
.user-view-container.bg-gradient-complete {
  background-color: #f5f7fa;
}

/* 背景离开时的渐变动画 */
.user-view-container.bg-fade-leave-active {
  animation: userPageLeave 0.5s ease-in-out forwards;
}

@keyframes userPageLeave {
  from {
    opacity: 1;
    filter: blur(0);
    transform: scale(1);
  }
  to {
    opacity: 0;
    filter: blur(20px);
    transform: scale(0.95);
  }
}

/* 页面进入时的渐入动画 */
.user-view-container.fade-enter-active {
  animation: userPageEnter 0.5s ease-out;
}

@keyframes userPageEnter {
  from {
    opacity: 0;
    filter: blur(10px);
  }
  to {
    opacity: 1;
    filter: blur(0);
  }
}

/* 顶部导航栏 - 与HomeView一致 */
.bili-header-bar {
  background-color: #2c3e50;
  z-index: 1000;
  height: 75px;
  box-sizing: border-box;
}

/* 论坛logo样式 */
.forum-logo {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin-right: 40px;
  cursor: pointer;
  transition: none !important;
}

.forum-logo:hover {
  color: var(--quinary-color);
}

.bili-header-bar .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 20px;
}

.left-entry,
.right-entry {
  flex: 1;
}

.left-entry {
  display: flex;
  justify-content: flex-start;
}

.right-entry {
  display: flex;
  justify-content: flex-end;
}

.flex {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin-right: 15px;
}

/* 右侧元素紧凑排列 */
.right-entry .flex {
  align-items: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: white;
  font-size: 16px;
  font-weight: 500;
  padding: 8px 0;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #e0e0e0;
}

.nav-link.active {
  color: #e0e0e0;
  font-weight: 600;
}

/* 搜索栏样式 */
.center-search-container {
  flex: 2;
  display: flex;
  justify-content: center;
}

.offset-center-search {
  margin-left: -50px;
}

.nav-search-content {
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  width: 100%;
  max-width: 1000px;
}

.nav-search-input {
  flex: 1;
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  font-size: 16px;
  outline: none;
}

.nav-search-btn {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  color: #666666;
  cursor: pointer;
  transition: color 0.3s ease;
  font-size: 16px;
}

.nav-search-btn:hover {
  color: var(--primary-color);
}

/* 用户头像容器 */
.user-avatar-container {
  position: relative;
  display: inline-block;
}

/* 用户头像样式 */
.user-avatar {
  font-size: 30px;
  display: block;
}

/* 用户下拉菜单 */
.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e0e0e0;
  min-width: 180px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 1000;
}

/* 下拉菜单显示状态 */
.user-dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

/* 下拉菜单项 */
.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
}

.dropdown-item i {
  margin-right: 10px;
  color: #666;
  font-size: 16px;
}

.dropdown-item span {
  color: #333;
  font-size: 14px;
}

/* 数据指标行 */
.stats-container {
  width: 100%;
  margin-top: 20px;
  padding: 0 20px;
  box-sizing: border-box;
}

.stats-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 40px;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:not(.disabled):hover {
  background-color: #e8f4f8;
}

.stat-item.stat-active .stat-number,
.stat-item.stat-active .stat-label {
  color: #667eea;
}

.stat-item.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.stat-number {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 主内容区域包装器 */
.main-content-container {
  width: 100%;
  margin-top: 20px;
  padding: 0 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
}

.main-content-wrapper {
  width: 80%;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  gap: 0;
}

/* 左侧导航栏 */
.left-sidebar {
  width: 200px;
  flex-shrink: 0;
  background-color: #f5f7fa;
}

.nav-menu {
  background-color: #f5f7fa;
  overflow: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
  margin-right: 0;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  background-color: #e3f2fd;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
}

.nav-item i {
  font-size: 16px;
}

.nav-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 确保router-link作为菜单项时的样式 */
.nav-item.router-link-active {
  background-color: #e3f2fd;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
}

.nav-item.router-link-active i {
  color: var(--primary-color);
}

/* 退出登录项样式 */
.logout-item {
  margin-top: 20px;
  color: #e74c3c !important;
  border-top: 1px solid #e0e0e0;
  padding-top: 15px;
}

.logout-item:hover {
  background-color: #fef2f2 !important;
  color: #c0392b !important;
}

.logout-item i {
  color: #e74c3c !important;
}

.logout-item:hover i {
  color: #c0392b !important;
}

/* 目标用户信息卡片 */
.target-user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #e8f4f8;
  border-radius: 8px;
  border: 2px solid var(--primary-color);
}

.target-user-card .target-user-avatar {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.target-user-card .target-user-avatar i {
  font-size: 48px;
  color: var(--primary-color);
}

.target-user-card .target-user-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  text-align: center;
  word-break: break-word;
}

/* 确保router-link的文本颜色与其他菜单项一致 */
.nav-item {
  color: #333;
  text-decoration: none;
}

.nav-item i {
  color: #666;
}

.nav-item:hover i {
  color: #333;
}

/* 右侧内容区域 */
.right-content {
  flex: 1;
  min-width: 0;
  background-color: #f5f7fa;
  padding: 30px;
  border-left: 1px solid #e8ecf0;
}

/* 内容卡片 */
.content-card {
  background-color: #f5f7fa;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
}

.content-card h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

/* 粉丝列表 */
.fan-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 粉丝项 */
.fan-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.fan-item:hover {
  background-color: #f5f5f5;
}

/* 粉丝头像 */
.fan-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  color: #666;
  flex-shrink: 0;
}

/* 粉丝信息 */
.fan-info {
  flex: 1;
  min-width: 0;
}

.fan-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.fan-id {
  font-size: 12px;
  color: #999;
}

/* 回关按钮 */
.follow-btn {
  padding: 6px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.follow-btn:hover {
  background-color: #2980b9;
}

/* 消息列表 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.message-item:hover {
  background-color: #f5f5f5;
}

/* 消息头像 */
.message-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  color: #666;
  flex-shrink: 0;
}

/* 消息内容 */
.message-content {
  flex: 1;
  min-width: 0;
}

.message-sender {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.message-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  color: #999;
}

/* 个人资料表单 */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 表单组 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 表单输入框 */
.form-input,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  box-sizing: border-box;
  background-color: #f9f9f9;
  transition: border-color 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.form-textarea {
  height: 120px;
  resize: vertical;
  min-height: 80px;
}

/* 保存按钮 */
.save-btn {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  align-self: flex-start;
}

.save-btn:hover {
  background-color: #2980b9;
}

/* 设置列表 */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 设置项 */
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.setting-item:hover {
  background-color: #f5f5f5;
}

.setting-item span {
  font-size: 14px;
  color: #333;
}

/* 开关按钮 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #3498db;
}

input:focus + .slider {
  box-shadow: 0 0 1px #3498db;
}

input:checked + .slider:before {
  transform: translateX(24px);
}

/* 消息容器布局 */
.messages-container h2 {
  margin-bottom: 20px;
}

.messages-layout {
  display: flex;
  gap: 20px;
  min-height: 400px;
}

/* 联系人侧边栏 */
.contacts-sidebar {
  width: 200px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
  flex-shrink: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.contact-item:hover {
  background-color: #e8e8e8;
}

.contact-item.active {
  background-color: #667eea;
  color: white;
}

.contact-item i {
  font-size: 20px;
}

/* 消息输入区域 */
.message-input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 新消息表单 */
.new-message-form {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.new-message-form h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.message-textarea {
  width: 100%;
  flex: 1;
  min-height: 120px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  resize: vertical;
  box-sizing: border-box;
  background-color: white;
}

.message-textarea:focus {
  outline: none;
  border-color: #3498db;
}

.message-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.send-btn,
.cancel-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn {
  background-color: #3498db;
  color: white;
}

.send-btn:hover {
  background-color: #2980b9;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #d0d0d0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content-wrapper {
    flex-direction: column;
    padding: 10px;
  }

  .left-sidebar {
    width: 100%;
  }

  .nav-menu {
    display: flex;
    overflow-x: auto;
    border-radius: 8px;
  }

  .nav-item {
    flex-shrink: 0;
    border-left: none;
    border-bottom: 3px solid transparent;
  }

  .nav-item.active {
    border-left: none;
    border-bottom-color: var(--primary-color);
  }

  .content-card {
    padding: 20px;
  }

  .fan-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .fan-info {
    width: 100%;
  }

  .follow-btn {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .top-nav-bar .container {
    padding: 0 10px;
  }

  .top-nav-bar .left-section .logo {
    font-size: 16px;
  }

  .top-nav-bar .user-avatar {
    font-size: 20px;
  }

  .top-nav-bar .username {
    font-size: 12px;
  }

  .content-card {
    padding: 15px;
  }

  .content-card h2 {
    font-size: 18px;
  }

  .fan-item {
    padding: 10px;
  }

  .fan-avatar {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

/* 空状态提示 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;
}

.empty-state i {
  font-size: 40px;
  color: #ccc;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 帖子表格样式 */
.thread-table-container {
  margin-top: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
}

.thread-table {
  width: 100%;
  border-collapse: collapse;
}

.thread-header {
  background-color: #f0f0f0;
}

.thread-header th {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #e0e0e0;
}

.thread-item {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.thread-item:hover {
  background-color: #f5f5f5;
}

.thread-item td {
  padding: 16px;
  font-size: 14px;
  color: #333;
}

.thread-info {
  min-width: 400px;
}

.thread-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.thread-title a {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.thread-title a:hover {
  color: #3498db;
}

.thread-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.thread-tag.hot {
  background-color: #e74c3c;
  color: white;
}

.thread-tag.recommended {
  background-color: #27ae60;
  color: white;
}

.thread-author {
  width: 120px;
}

.author-name {
  color: #666;
  font-size: 14px;
}

.thread-time {
  width: 150px;
  color: #999;
  font-size: 14px;
}

.thread-replies,
.thread-views {
  width: 80px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

/* 响应式设计 - 帖子表格 */
@media (max-width: 768px) {
  .thread-info {
    min-width: 200px;
  }

  .thread-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .thread-author,
  .thread-time,
  .thread-replies,
  .thread-views {
    width: auto;
    font-size: 12px;
  }

  .thread-item td {
    padding: 10px;
  }
}

/* 用户列表容器 */
.user-list-container h2 {
  margin-bottom: 20px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.user-item:hover {
  background-color: #f0f0f0;
}

.user-item-avatar {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
}

.user-item-avatar i {
  font-size: 50px;
  color: #999;
}

.user-item-info {
  flex: 1;
  min-width: 0;
}

.user-item-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.user-item-bio {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unfollow-btn,
.follow-back-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.unfollow-btn {
  background-color: #e0e0e0;
  color: #333;
}

.unfollow-btn:hover {
  background-color: #d0d0d0;
}

.follow-back-btn {
  background-color: var(--primary-color);
  color: white;
}

.follow-back-btn:hover {
  opacity: 0.9;
}

/* 消息导航栏 */
.message-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.message-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.message-tab:hover {
  color: var(--primary-color);
}

.message-tab.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

.message-tab-badge {
  min-width: 20px;
  padding: 2px 6px;
  border-radius: 999px;
  background-color: #ff5a5f;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
  text-align: center;
}

/* 通知列表 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
  transition:
    background-color 0.2s,
    border-color 0.2s,
    transform 0.2s;
}

.notification-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-1px);
}

.notification-item.unread {
  background-color: #fff5f5;
  border-color: rgba(255, 71, 87, 0.18);
}

.notification-item.unread .notif-text {
  font-weight: 600;
}

.notif-icon {
  font-size: 20px;
  color: var(--primary-color);
  margin-top: 2px;
}

.notif-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notif-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.notif-time {
  font-size: 12px;
  color: #999;
}
</style>
