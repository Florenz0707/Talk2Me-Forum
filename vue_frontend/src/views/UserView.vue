<template>
  <div
    class="user-view-container"
    :class="{
      'bg-gradient-complete': bgGradientComplete,
      'bg-fade-leave-active': isLeaving,
      'fade-enter-active': isEntering,
    }"
  >
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
    <div class="main-content-container">
      <div class="main-content-wrapper">
        <div class="left-sidebar">
          <div class="nav-menu">
            <div
              class="nav-item"
              :class="{ active: activeNavItem === 'notifications' }"
              @click="
                activeNavItem = 'notifications';
                activeStatsTab = null;
              "
            >
              <i class="fas fa-envelope"></i>
              <span>通知中心</span>
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
              <span>回到首页</span>
            </router-link>
            <div class="nav-item logout-item" @click="handleLogout">
              <i class="fas fa-sign-out-alt"></i>
              <span>退出登录</span>
            </div>
          </div>
        </div>
        <div class="right-content">
          <div class="content-card">
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
            <div
              v-if="activeNavItem === 'notifications'"
              class="messages-container"
            >
              <div class="messages-header">
                <h2>通知中心</h2>
                <button
                  v-if="hasUnreadNotifications"
                  class="mark-all-read-btn"
                  :disabled="markAllReadPending"
                  @click="handleMarkAllRead"
                >
                  {{ markAllReadPending ? "标记中..." : "全部标记为已读" }}
                </button>
              </div>
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
                  新增粉丝
                </div>
              </div>
              <div v-if="notificationsLoading" class="notification-status">
                <i class="fas fa-spinner fa-spin"></i>
                <p>通知加载中...</p>
              </div>
              <div
                v-else-if="notificationsError"
                class="notification-status notification-status-error"
              >
                <i class="fas fa-circle-exclamation"></i>
                <p>{{ notificationsError }}</p>
                <button
                  class="retry-notifications-btn"
                  @click="loadNotifications"
                >
                  重新加载
                </button>
              </div>
              <div v-else class="notification-panel">
                <div class="notification-toolbar">
                  <div class="notification-filter-group">
                    <button
                      v-for="filter in notificationReadFilters"
                      :key="filter.value"
                      class="notification-filter-btn"
                      :class="{
                        active: notificationReadFilter === filter.value,
                      }"
                      @click="setNotificationReadFilter(filter.value)"
                    >
                      {{ filter.label }}
                    </button>
                  </div>
                  <div
                    v-if="filteredNotifications.length > 0"
                    class="notification-toolbar-meta"
                  >
                    {{ notificationPaginationSummary }}
                  </div>
                </div>

                <div
                  v-if="filteredNotifications.length === 0"
                  class="empty-state"
                >
                  <i :class="activeNotificationEmptyState.icon"></i>
                  <p>{{ activeNotificationEmptyState.text }}</p>
                </div>
                <div v-else class="notification-list">
                  <div
                    v-for="notif in paginatedNotifications"
                    :key="notif.id"
                    class="notification-item"
                    :class="[
                      getNotificationTypeClass(notif),
                      { unread: !notif.isRead },
                    ]"
                  >
                    <div
                      class="notif-avatar is-clickable"
                      @click.stop="goToNotificationSenderProfile(notif)"
                    >
                      <img
                        v-if="notif.senderAvatar"
                        :src="notif.senderAvatar"
                        alt="用户头像"
                        class="avatar-img"
                      />
                      <i v-else class="fas fa-user-circle"></i>
                    </div>
                    <div
                      class="notif-content"
                      :class="{
                        'is-clickable': supportsNotificationNavigation(notif),
                      }"
                      :role="
                        supportsNotificationNavigation(notif)
                          ? 'button'
                          : undefined
                      "
                      :tabindex="supportsNotificationNavigation(notif) ? 0 : -1"
                      @click="handleNotificationContentClick(notif)"
                      @keydown.enter.prevent="
                        handleNotificationContentClick(notif)
                      "
                      @keydown.space.prevent="
                        handleNotificationContentClick(notif)
                      "
                    >
                      <div class="notif-meta-row">
                        <span
                          class="notif-type-pill"
                          :class="getNotificationTypeClass(notif)"
                        >
                          {{ getNotificationTypeLabel(notif) }}
                        </span>
                        <span class="notif-time">
                          {{ formatTime(notif.createTime) }}
                        </span>
                      </div>
                      <div class="notif-header">
                        <div class="reply-notif-title">
                          <span
                            class="notif-username is-clickable"
                            @click.stop="goToNotificationSenderProfile(notif)"
                          >
                            {{ getNotificationSenderName(notif) }}
                          </span>
                          <span class="notif-action">
                            {{ getNotificationActionText(notif) }}
                          </span>
                        </div>
                      </div>
                      <div
                        v-if="getNotificationTitle(notif)"
                        class="notif-title"
                      >
                        {{ getNotificationTitle(notif) }}
                      </div>
                      <div
                        class="reply-notification-body"
                        :class="{
                          'has-context': hasNotificationContext(notif),
                        }"
                      >
                        <div class="reply-main">
                          <div
                            v-if="getNotificationPreview(notif)"
                            class="notif-preview"
                          >
                            {{ getNotificationPreview(notif) }}
                          </div>
                          <div class="notif-footer">
                            <span
                              class="notif-read-state"
                              :class="{ unread: !notif.isRead }"
                            >
                              {{ notif.isRead ? "已读" : "未读" }}
                            </span>
                            <span
                              v-if="supportsNotificationNavigation(notif)"
                              class="notif-open-indicator"
                            >
                              点击查看详情
                            </span>
                          </div>
                        </div>
                        <div
                          v-if="hasNotificationContext(notif)"
                          class="reply-context"
                          :class="{
                            'is-truncated': isReplyContextTruncated(notif),
                          }"
                        >
                          {{ getReplyContextPreview(notif) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div
                  v-if="filteredNotifications.length > 0"
                  class="notification-pagination"
                >
                  <button
                    class="notification-page-btn"
                    :disabled="!canGoToPreviousNotificationPage"
                    @click="goToPreviousNotificationPage"
                  >
                    上一页
                  </button>
                  <span class="notification-page-text">
                    第 {{ notificationCurrentPage }} /
                    {{ totalNotificationPages }}
                    页
                  </span>
                  <button
                    class="notification-page-btn"
                    :disabled="!canGoToNextNotificationPage"
                    @click="goToNextNotificationPage"
                  >
                    下一页
                  </button>
                </div>
              </div>
            </div>
            <!--            <div v-else-if="activeNavItem === 'favorites'">-->
            <!--              <div v-if="favoritesLoading" class="empty-state">-->
            <!--                <i class="fas fa-spinner fa-spin"></i>-->
            <!--              </div>-->
            <!--              <div v-else-if="favoriteThreads.length === 0" class="empty-state">-->
            <!--                <i class="fas fa-star"></i>-->
            <!--              </div>-->
            <!--              <div v-else class="thread-table-container">-->
            <!--                <table class="thread-table">-->
            <!--                  <thead>-->
            <!--                    <tr class="thread-header">-->
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
import { authApi, userApi, postApi, notificationApi } from "../utils/api";
import { notificationWS } from "../utils/websocket";
import { setUserAvatar } from "../utils/authStorage";
import { isSameUserId, onUserProfileUpdated } from "../utils/profileStats";
import {
  applyIncomingNotification,
  extractIncomingNotifications,
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
    let unsubscribeProfileUpdated = null;
    const activeNavItem = ref("notifications");
    const activeMessageTab = ref("likes");
    const likeNotifications = ref([]);
    const replyNotifications = ref([]);
    const followNotifications = ref([]);
    const notificationReadFilter = ref("all");
    const notificationCurrentPage = ref(1);
    const notificationsLoading = ref(false);
    const notificationsError = ref("");
    const markAllReadPending = ref(false);
    const notificationStatsLoaded = ref(false);
    const autoReadPendingTabs = new Set();
    const autoReadQueuedTabs = new Set();
    const NOTIFICATION_PAGE_SIZE = 100;
    const NOTIFICATION_MAX_PAGES = 20;
    const NOTIFICATION_CARD_PAGE_SIZE = 8;
    const NOTIFICATION_TABS = ["likes", "replies", "followers"];
    const notificationReadFilters = [
      { value: "all", label: "全部" },
      { value: "unread", label: "仅未读" },
      { value: "read", label: "仅已读" },
    ];
    const activeStatsTab = ref(null);
    const currentUserId = ref(authApi.getCurrentUserId() || "");
    const profileLikesCount = ref(0);
    const profileFollowingCount = ref(0);
    const profileFollowersCount = ref(0);
    const followingList = ref([]);
    const followingLoading = ref(false);
    const followersList = ref([]);
    const followersLoading = ref(false);
    const newMessageTarget = ref("");
    const newMessageTargetId = ref("");
    const newMessageContent = ref("");
    const messageContacts = ref([]);
    const selectedContact = ref(null);
    const searchQuery = ref("");
    const showUserMenu = ref(false);
    const userAvatar = ref("");
    const showAvatarOverlay = ref(false);
    const fileInput = ref(null);
    const likeUnreadCount = computed(() => notificationSummary.byType.LIKE);
    const replyUnreadCount = computed(() => notificationSummary.byType.REPLY);
    const followUnreadCount = computed(() => notificationSummary.byType.FOLLOW);
    const likesCount = computed(() => profileLikesCount.value);
    const followingCount = computed(() => profileFollowingCount.value);
    const followersCount = computed(() => profileFollowersCount.value);
    const hasUnreadNotifications = computed(
      () => notificationSummary.total > 0,
    );
    const favoriteThreads = ref([]);
    const favoritesLoading = ref(false);
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
    const REPLY_CONTEXT_CHAR_LIMIT = 36;
    const userProfileCache = new Map();
    const userProfileRequests = new Map();
    const postMetaCache = new Map();
    const postMetaRequests = new Map();
    const pickFirstText = (...values) => {
      for (const value of values) {
        if (typeof value === "string" && value.trim()) {
          return value.trim();
        }
      }
      return "";
    };
    const resolveNotificationThreadId = (notif) => {
      const rawTargetType = String(notif?.targetType || "").toUpperCase();
      const isPostTarget = ["POST", "THREAD", "TOPIC"].includes(rawTargetType);
      return (
        notif?.postId ||
        notif?.post_id ||
        notif?.threadId ||
        notif?.thread_id ||
        notif?.topicId ||
        notif?.topic_id ||
        notif?.sourcePostId ||
        notif?.source_post_id ||
        notif?.post?.id ||
        notif?.post?.postId ||
        notif?.thread?.id ||
        notif?.thread?.postId ||
        (isPostTarget ? notif?.targetId : "")
      );
    };
    const getNotificationSenderName = (notification) =>
      pickFirstText(
        notification?.senderName,
        notification?.actorName,
        notification?.username,
        notification?.user?.username,
      ) || "用户";
    const getReplyText = (notification) =>
      pickFirstText(
        notification?.replyContent,
        notification?.targetPreview,
        notification?.content,
        notification?.message,
      );
    const getReplyContextText = (notification) =>
      pickFirstText(
        notification?.commentContent,
        notification?.targetContent,
        notification?.originalContent,
        notification?.parentContent,
        notification?.quotedContent,
        notification?.sourceContent,
      );
    const getLikeText = (notification) =>
      pickFirstText(
        notification?.targetPreview,
        notification?.targetContent,
        notification?.originalContent,
        notification?.commentContent,
        notification?.sourceContent,
        notification?.content,
        notification?.message,
      );
    const getTargetTitle = (notification) =>
      pickFirstText(
        notification?.targetTitle,
        notification?.postTitle,
        notification?.threadTitle,
        notification?.topicTitle,
      );
    const getReplyPreview = (notification) =>
      getReplyText(notification) || "回复内容暂不可用";
    const getLikePreview = (notification) =>
      getLikeText(notification) || "相关内容暂不可用";
    const getFollowPreview = (notification) =>
      pickFirstText(
        notification?.targetPreview,
        notification?.content,
        notification?.message,
      ) || "点击查看对方主页";
    const getNotificationPreview = (notification) => {
      if (notification?.type === "REPLY") {
        return getReplyPreview(notification);
      }
      if (notification?.type === "FOLLOW") {
        return getFollowPreview(notification);
      }
      return getLikePreview(notification);
    };
    const getReplyContextPreview = (notification) => {
      const contextText = getReplyContextText(notification);
      if (!contextText) {
        return "相关评论内容暂不可用";
      }
      if (contextText.length <= REPLY_CONTEXT_CHAR_LIMIT) {
        return contextText;
      }
      return contextText.slice(0, REPLY_CONTEXT_CHAR_LIMIT);
    };
    const isReplyContextTruncated = (notification) =>
      getReplyContextText(notification).length > REPLY_CONTEXT_CHAR_LIMIT;
    const getDefaultNotificationActionText = (notification) => {
      if (notification?.actionText) {
        return notification.actionText;
      }
      const rawTargetType = String(
        notification?.targetType || "",
      ).toUpperCase();
      if (notification?.type === "FOLLOW") {
        return "关注了我";
      }
      if (notification?.type === "REPLY") {
        if (["POST", "THREAD", "TOPIC"].includes(rawTargetType)) {
          return "回复了我的帖子";
        }
        return "回复了我的评论";
      }
      if (["POST", "THREAD", "TOPIC"].includes(rawTargetType)) {
        return "点赞了我的帖子";
      }
      if (["REPLY", "COMMENT"].includes(rawTargetType)) {
        return "点赞了我的评论";
      }
      return "点赞了我";
    };
    const getNotificationActionText = (notification) =>
      getDefaultNotificationActionText(notification);
    const getNotificationTitle = (notification) => {
      const directTitle = getTargetTitle(notification);
      if (directTitle) {
        return directTitle;
      }
      if (notification?.type === "FOLLOW") {
        return "你的个人主页";
      }
      const rawTargetType = String(
        notification?.targetType || "",
      ).toUpperCase();
      if (["POST", "THREAD", "TOPIC"].includes(rawTargetType)) {
        return "相关帖子";
      }
      if (["REPLY", "COMMENT"].includes(rawTargetType)) {
        return "相关评论";
      }
      return "";
    };
    const hasNotificationContext = (notification) =>
      notification?.type === "REPLY" &&
      Boolean(getReplyContextText(notification));
    const getNotificationTypeClass = (notification) => {
      if (notification?.type === "REPLY") {
        return "reply-notification";
      }
      if (notification?.type === "FOLLOW") {
        return "follow-notification";
      }
      return "like-notification";
    };
    const getNotificationTypeLabel = (notification) => {
      if (notification?.type === "REPLY") {
        return "回复";
      }
      if (notification?.type === "FOLLOW") {
        return "关注";
      }
      return "赞";
    };
    const getUserProfileWithCache = async (userId) => {
      if (!userId) {
        return null;
      }
      if (userProfileCache.has(userId)) {
        return userProfileCache.get(userId);
      }
      if (userProfileRequests.has(userId)) {
        return userProfileRequests.get(userId);
      }
      const request = userApi
        .getUserProfile(userId)
        .then((res) => {
          const profile = res?.data
            ? {
                username: res.data.username || "",
                avatar: res.data.avatar || "",
              }
            : null;
          if (profile) {
            userProfileCache.set(userId, profile);
          }
          return profile;
        })
        .catch((error) => {
          console.error(`获取用户 ${userId} 信息失败:`, error);
          return null;
        })
        .finally(() => {
          userProfileRequests.delete(userId);
        });
      userProfileRequests.set(userId, request);
      return request;
    };
    const getPostMetaWithCache = async (postId) => {
      if (!postId) {
        return null;
      }
      if (postMetaCache.has(postId)) {
        return postMetaCache.get(postId);
      }
      if (postMetaRequests.has(postId)) {
        return postMetaRequests.get(postId);
      }
      const request = postApi
        .getPostById(postId)
        .then((res) => {
          const post = res?.data
            ? {
                id: res.data.id || postId,
                title: res.data.title || "",
              }
            : null;
          if (post) {
            postMetaCache.set(postId, post);
          }
          return post;
        })
        .catch((error) => {
          console.error(`获取帖子 ${postId} 标题失败:`, error);
          return null;
        })
        .finally(() => {
          postMetaRequests.delete(postId);
        });
      postMetaRequests.set(postId, request);
      return request;
    };
    const buildNotificationCard = (
      notification,
      userInfo = null,
      postInfo = null,
    ) => ({
      ...notification,
      senderName:
        notification?.senderName ||
        notification?.actorName ||
        userInfo?.username ||
        "用户",
      senderAvatar:
        notification?.senderAvatar ||
        notification?.actorAvatar ||
        userInfo?.avatar ||
        "",
      targetTitle:
        notification?.targetTitle ||
        notification?.postTitle ||
        notification?.threadTitle ||
        postInfo?.title ||
        "",
      targetPreview:
        notification?.targetPreview || getNotificationPreview(notification),
      actionText: getDefaultNotificationActionText(notification),
      targetUrl:
        notification?.targetUrl ||
        (resolveNotificationThreadId(notification)
          ? `/thread/${resolveNotificationThreadId(notification)}`
          : ""),
      replyContent: getReplyText(notification),
      commentContent: getReplyContextText(notification),
    });
    const enrichNotificationCards = async (notifications = []) => {
      const senderIds = [
        ...new Set(notifications.map((item) => item?.userId).filter(Boolean)),
      ];
      const postIds = [
        ...new Set(
          notifications
            .map((item) => resolveNotificationThreadId(item))
            .filter(Boolean),
        ),
      ];
      await Promise.all([
        ...senderIds.map((userId) => getUserProfileWithCache(userId)),
        ...postIds.map((postId) => getPostMetaWithCache(postId)),
      ]);
      return notifications.map((notification) =>
        buildNotificationCard(
          notification,
          userProfileCache.get(notification?.userId) || null,
          postMetaCache.get(resolveNotificationThreadId(notification)) || null,
        ),
      );
    };
    const prependNotification = (listRef, notification) => {
      if (notification?.id === undefined) {
        listRef.value.unshift(notification);
        return true;
      }
      const existingIndex = listRef.value.findIndex(
        (item) => item.id === notification.id,
      );
      if (existingIndex === -1) {
        listRef.value.unshift(notification);
        return true;
      }
      const mergedNotification = {
        ...listRef.value[existingIndex],
        ...notification,
      };
      listRef.value.splice(existingIndex, 1);
      listRef.value.unshift(mergedNotification);
      return false;
    };
    const removeNotificationById = (listRef, notificationId) => {
      if (notificationId == null) {
        return;
      }
      listRef.value = listRef.value.filter(
        (item) => item.id !== notificationId,
      );
    };
    const normalizeNotificationTab = (tab) =>
      NOTIFICATION_TABS.includes(tab) ? tab : "likes";
    const normalizeNavTab = (tab) => {
      if (tab === "messages") {
        return "notifications";
      }
      if (["notifications", "profile", "settings"].includes(tab)) {
        return tab;
      }
      return "notifications";
    };
    const getNotificationsByTab = (tab) => {
      if (tab === "replies") {
        return replyNotifications.value;
      }
      if (tab === "followers") {
        return followNotifications.value;
      }
      return likeNotifications.value;
    };
    const getNotificationListRefByType = (type) => {
      if (type === "REPLY") {
        return replyNotifications;
      }
      if (type === "FOLLOW") {
        return followNotifications;
      }
      return likeNotifications;
    };
    const activeNotifications = computed(() =>
      getNotificationsByTab(activeMessageTab.value),
    );
    const filteredNotifications = computed(() => {
      if (notificationReadFilter.value === "unread") {
        return activeNotifications.value.filter(
          (notification) => !notification?.isRead,
        );
      }
      if (notificationReadFilter.value === "read") {
        return activeNotifications.value.filter(
          (notification) => notification?.isRead,
        );
      }
      return activeNotifications.value;
    });
    const totalNotificationPages = computed(() =>
      Math.max(
        1,
        Math.ceil(
          filteredNotifications.value.length / NOTIFICATION_CARD_PAGE_SIZE,
        ),
      ),
    );
    const paginatedNotifications = computed(() => {
      const startIndex =
        (notificationCurrentPage.value - 1) * NOTIFICATION_CARD_PAGE_SIZE;
      return filteredNotifications.value.slice(
        startIndex,
        startIndex + NOTIFICATION_CARD_PAGE_SIZE,
      );
    });
    const notificationPaginationSummary = computed(() => {
      const total = filteredNotifications.value.length;
      if (total === 0) {
        return "";
      }
      const startIndex =
        (notificationCurrentPage.value - 1) * NOTIFICATION_CARD_PAGE_SIZE + 1;
      const endIndex = Math.min(
        total,
        startIndex + NOTIFICATION_CARD_PAGE_SIZE - 1,
      );
      return `显示 ${startIndex}-${endIndex} / ${total} 条`;
    });
    const activeNotificationEmptyState = computed(() => {
      const tabMap = {
        likes: { icon: "fas fa-heart", text: "暂无点赞通知" },
        replies: { icon: "fas fa-comment", text: "暂无回复通知" },
        followers: { icon: "fas fa-user-plus", text: "暂无新的粉丝通知" },
      };
      const fallback = tabMap[activeMessageTab.value] || tabMap.likes;
      if (notificationReadFilter.value === "unread") {
        return { icon: fallback.icon, text: "当前筛选下暂无未读通知" };
      }
      if (notificationReadFilter.value === "read") {
        return { icon: fallback.icon, text: "当前筛选下暂无已读通知" };
      }
      return fallback;
    });
    const canGoToPreviousNotificationPage = computed(
      () => notificationCurrentPage.value > 1,
    );
    const canGoToNextNotificationPage = computed(
      () => notificationCurrentPage.value < totalNotificationPages.value,
    );
    const setNotificationReadFilter = (filterValue) => {
      notificationReadFilter.value = filterValue;
      notificationCurrentPage.value = 1;
    };
    const goToPreviousNotificationPage = () => {
      if (!canGoToPreviousNotificationPage.value) {
        return;
      }
      notificationCurrentPage.value -= 1;
    };
    const goToNextNotificationPage = () => {
      if (!canGoToNextNotificationPage.value) {
        return;
      }
      notificationCurrentPage.value += 1;
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
    const getUnreadNotifications = (notifications = []) =>
      notifications.filter(
        (notification) => notification && !notification.isRead,
      );
    const syncNotificationsAsRead = (notifications = []) => {
      const markedNotifications = [];
      notifications.forEach((notification) => {
        if (!notification?.isRead) {
          notification.isRead = true;
          markedNotifications.push(notification);
        }
      });
      if (markedNotifications.length > 0) {
        markNotificationsAsReadInSummary(markedNotifications);
      }
    };
    const markNotificationsAsRead = async (notifications = []) => {
      const unreadNotifications = getUnreadNotifications(notifications);
      if (unreadNotifications.length === 0) {
        return;
      }

      const readableNotifications = unreadNotifications.filter(
        (notification) => notification?.id != null,
      );
      const unreadWithoutId = unreadNotifications.filter(
        (notification) => notification?.id == null,
      );

      if (unreadWithoutId.length > 0) {
        syncNotificationsAsRead(unreadWithoutId);
      }

      if (readableNotifications.length === 0) {
        return;
      }

      const results = await Promise.allSettled(
        readableNotifications.map((notification) =>
          notificationApi.markRead(notification.id),
        ),
      );
      const succeededNotifications = [];

      results.forEach((result, index) => {
        if (result.status === "fulfilled") {
          succeededNotifications.push(readableNotifications[index]);
          return;
        }
        console.error(
          "标记通知已读失败:",
          readableNotifications[index],
          result.reason,
        );
      });

      syncNotificationsAsRead(succeededNotifications);
    };
    const markNotificationTabAsRead = async (tab = activeMessageTab.value) => {
      if (
        activeNavItem.value !== "notifications" ||
        notificationsLoading.value ||
        notificationsError.value
      ) {
        return;
      }

      if (autoReadPendingTabs.has(tab)) {
        autoReadQueuedTabs.add(tab);
        return;
      }

      autoReadPendingTabs.add(tab);
      try {
        do {
          autoReadQueuedTabs.delete(tab);
          await markNotificationsAsRead(getNotificationsByTab(tab));
        } while (autoReadQueuedTabs.has(tab));
      } finally {
        autoReadPendingTabs.delete(tab);
      }
    };
    const markAllNotificationTabsAsRead = () => {
      syncNotificationsAsRead(
        [
          likeNotifications.value,
          replyNotifications.value,
          followNotifications.value,
        ].flat(),
      );
    };
    const applyRouteState = (query = {}) => {
      if (query.tab) {
        activeNavItem.value = normalizeNavTab(query.tab);
        activeStatsTab.value = null;
      }
      if (query.messageTab) {
        activeMessageTab.value = normalizeNotificationTab(query.messageTab);
        activeNavItem.value = "notifications";
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
    const editUsername = ref("");
    const editBio = ref("");
    const editBirthday = ref("");
    const editGender = ref("");
    const editOccupation = ref("");
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
    const triggerFileInput = () => {
      fileInput.value.click();
    };
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
    const fetchUserInfo = async () => {
      const nameFromToken = authApi.getUsernameFromToken();
      if (nameFromToken) {
        username.value = nameFromToken;
        editUsername.value = nameFromToken;
      } else {
        const remembered = localStorage.getItem("rememberedUsername");
        username.value = remembered || "用户";
        editUsername.value = username.value;
      }
      try {
        const res = await userApi.getCurrentProfile();
        if (res.data) {
          const p = res.data;
          currentUserId.value = p.id || p.userId || currentUserId.value;
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
    const loadNotifications = async () => {
      notificationsLoading.value = true;
      notificationsError.value = "";
      let shouldAutoReadActiveTab = false;
      try {
        const records = await fetchNotificationRecords();
        const likeRecords = records.filter((n) => n.type === "LIKE");
        const replyRecords = records.filter((n) => n.type === "REPLY");
        const followRecords = records.filter((n) => n.type === "FOLLOW");
        const enrichedLikeRecords = await enrichNotificationCards(likeRecords);
        const enrichedReplyRecords =
          await enrichNotificationCards(replyRecords);
        const enrichedFollowRecords =
          await enrichNotificationCards(followRecords);
        likeNotifications.value = enrichedLikeRecords;
        replyNotifications.value = enrichedReplyRecords;
        followNotifications.value = enrichedFollowRecords;
        notificationStatsLoaded.value = true;
        shouldAutoReadActiveTab = true;
      } catch (error) {
        console.error("通知加载失败:", error);
        notificationsError.value = error?.message || "通知加载失败，请稍后重试";
      } finally {
        notificationsLoading.value = false;
      }
      if (shouldAutoReadActiveTab) {
        await markNotificationTabAsRead();
      }
    };
    const handleNotification = async (notification) => {
      const normalizedNotifications = extractIncomingNotifications(
        notification,
      ).filter((item) => ["LIKE", "REPLY", "FOLLOW"].includes(item?.type));

      applyIncomingNotification(notification);

      if (normalizedNotifications.length === 0) {
        return;
      }

      const notificationsByType = {
        LIKE: [],
        REPLY: [],
        FOLLOW: [],
      };
      let shouldRefreshUserStats = false;

      normalizedNotifications.forEach((normalizedNotification) => {
        if (normalizedNotification.eventType === "DELETED") {
          removeNotificationById(
            getNotificationListRefByType(normalizedNotification.type),
            normalizedNotification.id,
          );
        } else {
          notificationsByType[normalizedNotification.type].push(
            normalizedNotification,
          );
        }

        if (
          normalizedNotification.type === "LIKE" ||
          normalizedNotification.type === "FOLLOW"
        ) {
          shouldRefreshUserStats = true;
        }
      });

      await Promise.all(
        Object.entries(notificationsByType).map(async ([type, items]) => {
          if (items.length === 0) {
            return;
          }
          const enrichedNotifications = await enrichNotificationCards(items);
          enrichedNotifications.forEach((enrichedNotification) => {
            prependNotification(
              getNotificationListRefByType(type),
              enrichedNotification,
            );
          });
        }),
      );

      notificationStatsLoaded.value = true;
      if (shouldRefreshUserStats) {
        fetchUserInfo();
      }
      await markNotificationTabAsRead();
    };
    const resolveNotificationTargetUrl = (notif) => {
      const rawUrl = pickFirstText(notif?.targetUrl, notif?.url, notif?.link);
      if (!rawUrl) {
        return "";
      }
      if (rawUrl.startsWith("/")) {
        return rawUrl;
      }
      try {
        const parsedUrl = new URL(rawUrl, window.location.origin);
        if (parsedUrl.origin !== window.location.origin) {
          return "";
        }
        return `${parsedUrl.pathname}${parsedUrl.search}${parsedUrl.hash}`;
      } catch {
        return "";
      }
    };
    const resolveNotificationRoute = (notif) => {
      const targetUrl = resolveNotificationTargetUrl(notif);
      if (targetUrl) {
        return targetUrl;
      }
      const rawTargetType = String(notif?.targetType || "").toUpperCase();
      const isUserTarget =
        notif?.type === "FOLLOW" ||
        ["USER", "PROFILE", "FOLLOWER", "FOLLOWING"].includes(rawTargetType);
      const threadId = resolveNotificationThreadId(notif);
      const userId = isUserTarget
        ? notif?.type === "FOLLOW"
          ? notif?.userId ||
            notif?.actorId ||
            notif?.senderId ||
            notif?.targetId
          : notif?.targetUserId ||
            notif?.targetId ||
            notif?.userId ||
            notif?.actorId ||
            notif?.senderId
        : "";
      if (threadId) {
        return {
          name: "ThreadDetail",
          params: { id: String(threadId) },
        };
      }
      if (userId) {
        const resolvedCurrentUserId =
          authApi.getCurrentUserId() || currentUserId.value;
        if (isSameUserId(resolvedCurrentUserId, userId)) {
          return { name: "User" };
        }
        return {
          name: "OtherUser",
          params: { id: String(userId) },
        };
      }
      return null;
    };
    const supportsNotificationNavigation = (notif) =>
      Boolean(resolveNotificationRoute(notif));
    const markNotificationAsReadBeforeNavigation = async (notif) => {
      if (!notif) {
        return;
      }
      try {
        if (!notif.isRead && notif.id != null) {
          await notificationApi.markRead(notif.id);
          notif.isRead = true;
          markNotificationAsReadInSummary(notif);
        }
      } catch (error) {
        console.error("标记通知已读失败:", error);
      }
    };
    const navigateToNotification = async (notif, preferredRoute = null) => {
      if (!notif) {
        return;
      }
      await markNotificationAsReadBeforeNavigation(notif);
      const targetRoute = preferredRoute || resolveNotificationRoute(notif);
      if (targetRoute) {
        router.push(targetRoute);
      }
    };
    const handleNotificationContentClick = async (notif) => {
      await navigateToNotification(notif);
    };
    const goToUserProfile = (userId) => {
      if (!userId) {
        return;
      }
      const resolvedCurrentUserId =
        authApi.getCurrentUserId() || currentUserId.value;
      if (isSameUserId(resolvedCurrentUserId, userId)) {
        router.push({ name: "User" });
        return;
      }
      router.push({
        name: "OtherUser",
        params: { id: String(userId) },
      });
    };
    const goToNotificationSenderProfile = (notif) => {
      goToUserProfile(notif?.userId || notif?.actorId);
    };
    const handleMarkAllRead = async () => {
      if (!hasUnreadNotifications.value || markAllReadPending.value) {
        return;
      }
      markAllReadPending.value = true;
      try {
        await notificationApi.markAllRead();
        markAllNotificationTabsAsRead();
      } catch (error) {
        console.error("全部标记为已读失败:", error);
      } finally {
        markAllReadPending.value = false;
      }
    };
    const handleLogout = async () => {
      isLoggingOut.value = true;
      try {
        await authApi.logout();
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("rememberedUsername");
        localStorage.removeItem("auth_token");
        updateLoginStatus(false);
        router.push("/");
      } catch (error) {
        console.error("退出登录失败:", error);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("rememberedUsername");
        localStorage.removeItem("auth_token");
        updateLoginStatus(false);
        router.push("/");
      } finally {
        isLoggingOut.value = false;
      }
    };
    watch(activeStatsTab, (newTab) => {
      if (newTab === "following") {
        fetchFollowing();
      } else if (newTab === "followers") {
        fetchFollowers();
      }
    });
    watch(activeMessageTab, () => {
      notificationCurrentPage.value = 1;
      markNotificationTabAsRead();
    });
    watch(activeNavItem, (newValue) => {
      if (newValue === "notifications") {
        markNotificationTabAsRead();
      }
    });
    watch(filteredNotifications, () => {
      if (notificationCurrentPage.value > totalNotificationPages.value) {
        notificationCurrentPage.value = totalNotificationPages.value;
      }
    });
    watch(isLoggedIn, (newValue, oldValue) => {
      if (newValue && !oldValue) {
        fetchUserInfo();
        loadNotifications();
        refreshNotificationSummary();
      }
    });
    watch(
      () => route.fullPath,
      () => {
        applyRouteState(route.query);
      },
    );
    onMounted(async () => {
      // 确保登录态有效，未登录时弹出登录框并返回首页。
      const authenticated = await authApi.ensureSession();
      if (!authenticated) {
        window.dispatchEvent(new CustomEvent("open-login-modal"));
        router.push("/");
        return;
      }
      applyRouteState(route.query);
      fetchUserInfo();
      fetchStats();
      loadNotifications();
      refreshNotificationSummary();

      // 建立通知订阅，并在资料更新时刷新当前用户信息。
      notificationWS.connect();
      unsubscribeNotification =
        notificationWS.onNotification(handleNotification);
      unsubscribeProfileUpdated = onUserProfileUpdated(({ userId }) => {
        if (isSameUserId(currentUserId.value, userId)) {
          fetchUserInfo();
        }
      });

      setTimeout(() => {
        isEntering.value = false;
      }, 600);

      setTimeout(() => {
        bgGradientComplete.value = true;
      }, 100);

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
    onBeforeUnmount(() => {
      if (navigationGuard) {
        navigationGuard();
      }
      if (unsubscribeNotification) {
        unsubscribeNotification();
        unsubscribeNotification = null;
      }
      if (unsubscribeProfileUpdated) {
        unsubscribeProfileUpdated();
        unsubscribeProfileUpdated = null;
      }
    });
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        console.log("搜索内容:", searchQuery.value);
      }
    };
    const handleSendMessage = async () => {
      if (!newMessageContent.value.trim()) {
        alert("请输入消息内容");
        return;
      }
      // TODO: 接入发送私信接口。
      console.log(
        "发送消息给:",
        newMessageTarget.value,
        newMessageContent.value,
      );
      alert("私信功能暂未开放");
      cancelNewMessage();
    };
    const cancelNewMessage = () => {
      newMessageTarget.value = "";
      newMessageTargetId.value = "";
      newMessageContent.value = "";
      selectedContact.value = null;
      router.replace({ path: "/user", query: { tab: "notifications" } });
    };
    const selectContact = (contact) => {
      selectedContact.value = contact;
      newMessageTarget.value = contact.name;
      newMessageTargetId.value = contact.id;
    };
    const fetchStats = async () => {};
    const fetchFollowing = async () => {
      followingLoading.value = true;
      try {
        // const data = await userApi.getFollowing();
        // followingList.value = data;
        followingList.value = [];
      } catch (error) {
        console.error("获取关注列表失败:", error);
      } finally {
        followingLoading.value = false;
      }
    };
    const fetchFollowers = async () => {
      followersLoading.value = true;
      try {
        // const data = await userApi.getFollowers();
        // followersList.value = data;
        followersList.value = [];
      } catch (error) {
        console.error("获取粉丝列表失败:", error);
      } finally {
        followersLoading.value = false;
      }
    };
    const handleUnfollow = async (userId) => {
      // await userApi.unfollow(userId);
      console.log("取消关注用户:", userId);
      alert("取消关注成功");
      fetchFollowing();
    };
    const handleFollowBack = async (userId) => {
      // await userApi.follow(userId);
      console.log("回关用户:", userId);
      alert("关注成功");
      fetchFollowers();
    };
    const handleStatsClick = (tab) => {
      activeStatsTab.value = tab;
      if (tab === "likes") {
        activeNavItem.value = "notifications";
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
      notificationReadFilter,
      notificationReadFilters,
      filteredNotifications,
      paginatedNotifications,
      totalNotificationPages,
      notificationPaginationSummary,
      activeNotificationEmptyState,
      notificationCurrentPage,
      canGoToPreviousNotificationPage,
      canGoToNextNotificationPage,
      setNotificationReadFilter,
      goToPreviousNotificationPage,
      goToNextNotificationPage,
      getLikePreview,
      getReplyPreview,
      getNotificationPreview,
      getNotificationActionText,
      getNotificationTitle,
      getNotificationTypeClass,
      getNotificationTypeLabel,
      getNotificationSenderName,
      hasNotificationContext,
      supportsNotificationNavigation,
      getReplyContextPreview,
      isReplyContextTruncated,
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
      handleNotificationContentClick,
      goToNotificationSenderProfile,
      handleMarkAllRead,
      hasUnreadNotifications,
      activeStatsTab,
      likeUnreadCount,
      replyUnreadCount,
      followUnreadCount,
      notificationsLoading,
      notificationsError,
      markAllReadPending,
      loadNotifications,
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
.user-view-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
  transition: background-color 0.3s ease-in-out;
}
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
.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  width: 120px;
  height: 120px;
  margin-left: -20px;
}
.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}
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
.change-avatar-text {
  color: white;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  user-select: none;
}
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
.user-view-container.bg-gradient-complete {
  background-color: #f5f7fa;
}
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
.bili-header-bar {
  background-color: #2c3e50;
  z-index: 1000;
  height: 75px;
  box-sizing: border-box;
}
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
.user-avatar-container {
  position: relative;
  display: inline-block;
}
.user-avatar {
  font-size: 30px;
  display: block;
}
/* 用户头像下拉菜单 */
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
.user-dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}
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
.nav-item.router-link-active {
  background-color: #e3f2fd;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
}
.nav-item.router-link-active i {
  color: var(--primary-color);
}
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
.right-content {
  flex: 1;
  min-width: 0;
  background-color: #f5f7fa;
  padding: 30px;
  border-left: 1px solid #e8ecf0;
}
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
.fan-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
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
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
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
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
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
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
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
.messages-container h2 {
  margin-bottom: 20px;
}
.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.messages-header h2 {
  margin-bottom: 0;
}
.notification-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.notification-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.notification-filter-group {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.notification-filter-btn {
  padding: 6px 14px;
  border: 1px solid #d7deea;
  border-radius: 999px;
  background-color: #fff;
  color: #576074;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition:
    border-color 0.2s,
    color 0.2s,
    background-color 0.2s;
}
.notification-filter-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}
.notification-filter-btn.active {
  border-color: transparent;
  background-color: var(--primary-color);
  color: #fff;
}
.notification-toolbar-meta {
  color: #7b8798;
  font-size: 13px;
}
.notification-hint {
  margin: -4px 0 16px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}
.mark-all-read-btn {
  padding: 6px 16px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.mark-all-read-btn:hover {
  opacity: 0.88;
}
.mark-all-read-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
.notification-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 56px 20px;
  border: 1px dashed #d7deea;
  border-radius: 12px;
  background-color: #fbfcfe;
  color: #6b7280;
}
.notification-status i {
  font-size: 28px;
}
.notification-status p {
  margin: 0;
  font-size: 14px;
}
.notification-status-error {
  border-color: rgba(239, 68, 68, 0.22);
  background-color: #fff7f7;
  color: #b42318;
}
.retry-notifications-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 999px;
  background-color: var(--primary-color);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.retry-notifications-btn:hover {
  opacity: 0.9;
}
.messages-layout {
  display: flex;
  gap: 20px;
  min-height: 400px;
}
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
.message-input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}
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
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.notification-item {
  --reply-context-bg: #f9f9f9;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 15px;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.96),
    rgba(245, 247, 250, 0.96)
  );
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.04);
  transition:
    background-color 0.2s,
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
}
.notification-item:hover {
  --reply-context-bg: #f0f0f0;
  background-color: #f0f0f0;
  box-shadow: 0 16px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}
.notification-item.unread {
  --reply-context-bg: #fff5f5;
  border-color: rgba(255, 71, 87, 0.18);
  background: linear-gradient(
    180deg,
    rgba(255, 245, 245, 0.98),
    rgba(255, 249, 249, 0.98)
  );
}
.notification-item.unread .notif-text {
  font-weight: 600;
}
.notification-item.unread .notif-username,
.notification-item.unread .notif-action,
.notification-item.unread .notif-preview,
.notification-item.unread .notif-title {
  font-weight: 600;
}
.like-notification {
  border-left: 4px solid rgba(239, 68, 68, 0.55);
}
.reply-notification {
  border-left: 4px solid rgba(59, 130, 246, 0.5);
}
.follow-notification {
  border-left: 4px solid rgba(16, 185, 129, 0.5);
}
.notif-icon {
  font-size: 20px;
  color: var(--primary-color);
  margin-top: 2px;
}
.notif-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}
.notif-avatar.is-clickable {
  cursor: pointer;
}
.notif-avatar .avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.notif-avatar i {
  font-size: 40px;
  color: #999;
}
.reply-notification .notif-content {
  flex: 1;
  min-width: 0;
}
.notif-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.notif-type-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}
.notif-type-pill.like-notification {
  background-color: rgba(239, 68, 68, 0.12);
  color: #d92d20;
}
.notif-type-pill.reply-notification {
  background-color: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
}
.notif-type-pill.follow-notification {
  background-color: rgba(16, 185, 129, 0.14);
  color: #047857;
}
.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.notif-username {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}
.notif-username.is-clickable {
  cursor: pointer;
}
.reply-notif-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.notif-action {
  color: #666;
  font-size: 13px;
}
.notif-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.notif-content.is-clickable {
  cursor: pointer;
}
.notif-content.is-clickable:focus-visible {
  outline: 2px solid rgba(59, 130, 246, 0.35);
  outline-offset: 4px;
  border-radius: 6px;
}
.notif-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
.notif-preview {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  word-break: break-word;
}
.notif-title {
  margin-bottom: 6px;
  color: #182230;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  word-break: break-word;
}
.reply-notification-body {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
}
.reply-notification-body.has-context {
  align-items: stretch;
}
.reply-main {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}
.reply-main .notif-preview {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.reply-context {
  position: relative;
  flex: 0 0 150px;
  max-width: 150px;
  min-width: 120px;
  padding-left: 12px;
  border-left: 1px solid rgba(15, 23, 42, 0.08);
  color: #8b94a4;
  font-size: 13px;
  line-height: 1.45;
  max-height: calc(1.45em * 3);
  overflow: hidden;
  word-break: break-word;
}
.reply-context.is-truncated::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 42px;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0),
    var(--reply-context-bg) 82%
  );
  pointer-events: none;
}
.notif-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}
.notif-read-state {
  color: #8b94a4;
  font-size: 12px;
  font-weight: 600;
}
.notif-read-state.unread {
  color: #d92d20;
}
.notif-open-indicator {
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 600;
}
.notif-time {
  font-size: 12px;
  color: #999;
}
.notification-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
.notification-page-btn {
  padding: 7px 14px;
  border: 1px solid #d7deea;
  border-radius: 999px;
  background-color: #fff;
  color: #3f4a5a;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.notification-page-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}
.notification-page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.notification-page-text {
  color: #6b7280;
  font-size: 13px;
}
@media (max-width: 768px) {
  .notification-toolbar,
  .notification-pagination,
  .notif-meta-row,
  .notif-footer {
    align-items: flex-start;
    flex-direction: column;
  }
  .reply-notification-body {
    flex-direction: column;
    gap: 10px;
  }
  .reply-context {
    flex-basis: auto;
    max-width: none;
    min-width: 0;
    width: 100%;
    padding-left: 0;
    padding-top: 10px;
    border-left: 0;
    border-top: 1px solid rgba(15, 23, 42, 0.08);
    max-height: calc(1.45em * 2);
  }
  .reply-context.is-truncated::after {
    top: auto;
    left: 0;
    width: auto;
    height: 28px;
    background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0),
      var(--reply-context-bg) 85%
    );
  }
}
</style>
