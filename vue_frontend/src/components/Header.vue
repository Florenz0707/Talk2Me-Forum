<template>
  <header class="page-header">
    <div class="container">
      <div class="header-left">
        <h1 class="page-title">{{ title }}</h1>
        <div class="nav-buttons">
          <router-link to="/home" class="nav-button">首页</router-link>
          <router-link to="/sections" class="nav-button">板块</router-link>
        </div>
      </div>

      <div class="header-center">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索帖子..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <div class="user-actions">
        <template v-if="showAuthButtons">
          <button class="auth-circle-btn" @click="openModal('login')">
            登录
          </button>
        </template>

        <template v-else>
          <div
            class="user-avatar-container"
            @mouseenter="showUserMenu = true"
            @mouseleave="showUserMenu = false"
          >
            <router-link to="/user" class="nav-link user-avatar">
              <i v-if="!userAvatar" class="fas fa-user-circle"></i>
              <img
                v-else
                :src="userAvatar"
                alt="用户头像"
                class="avatar-image"
              />
              <span v-if="hasUnread" class="avatar-unread-dot"></span>
            </router-link>

            <div class="user-dropdown-menu" :class="{ show: showUserMenu }">
              <div class="dropdown-item" @click="goToLikes">
                <i class="fas fa-heart"></i>
                <span>收到的赞</span>
                <span
                  v-if="notificationSummary.byType.LIKE > 0"
                  class="menu-unread-badge"
                >
                  {{ formatUnreadCount(notificationSummary.byType.LIKE) }}
                </span>
              </div>
              <div class="dropdown-item" @click="goToReplies">
                <i class="fas fa-comment"></i>
                <span>回复我的</span>
                <span
                  v-if="notificationSummary.byType.REPLY > 0"
                  class="menu-unread-badge"
                >
                  {{ formatUnreadCount(notificationSummary.byType.REPLY) }}
                </span>
              </div>
              <div class="dropdown-item" @click="goToFollowers">
                <i class="fas fa-user-plus"></i>
                <span>新的粉丝</span>
                <span
                  v-if="notificationSummary.byType.FOLLOW > 0"
                  class="menu-unread-badge"
                >
                  {{ formatUnreadCount(notificationSummary.byType.FOLLOW) }}
                </span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </header>

  <AuthModal
    :visible="showAuthModal"
    :initial-tab="authModalTab"
    @close="showAuthModal = false"
  />
</template>

<script setup>
import { computed, inject, onMounted, onUnmounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import AuthModal from "./AuthModal.vue";
import { notificationWS } from "../utils/websocket";
import { getUserAvatar } from "../utils/authStorage";
import {
  applyIncomingNotification,
  notificationSummary,
  refreshNotificationSummary,
  resetNotificationSummary,
} from "../utils/notificationState";

defineProps({
  title: {
    type: String,
    default: "论坛首页",
  },
});

const router = useRouter();
const isLoggedIn = inject("isLoggedIn", ref(false));

const showAuthButtons = computed(() => !isLoggedIn.value);
const hasUnread = computed(() => notificationSummary.total > 0);
const showUserMenu = ref(false);
const userAvatar = ref("");

const showAuthModal = ref(false);
const authModalTab = ref("login");
const searchKeyword = ref("");

let pollInterval = null;
let unsubscribeNotification = null;

const openModal = (tab) => {
  authModalTab.value = tab;
  showAuthModal.value = true;
};

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    return;
  }
  router.push({
    path: "/search",
    query: { keyword: searchKeyword.value.trim() },
  });
};

const formatUnreadCount = (count) => (count > 99 ? "99+" : count);

const goToLikes = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "likes" },
  });
  showUserMenu.value = false;
};

const goToReplies = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "replies" },
  });
  showUserMenu.value = false;
};

const goToFollowers = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "followers" },
  });
  showUserMenu.value = false;
};

const startUnreadSync = () => {
  if (!isLoggedIn.value) {
    return;
  }

  refreshNotificationSummary();

  if (!pollInterval) {
    pollInterval = setInterval(refreshNotificationSummary, 30000);
  }

  if (!unsubscribeNotification) {
    unsubscribeNotification = notificationWS.onNotification((notification) => {
      applyIncomingNotification(notification);
    });
  }
};

const stopUnreadSync = () => {
  if (pollInterval) {
    clearInterval(pollInterval);
    pollInterval = null;
  }
  if (unsubscribeNotification) {
    unsubscribeNotification();
    unsubscribeNotification = null;
  }
};

const handleOpenLoginModal = () => {
  openModal("login");
};

const syncUserAvatar = () => {
  userAvatar.value = getUserAvatar() || "";
};

onMounted(() => {
  syncUserAvatar();

  window.addEventListener("open-login-modal", handleOpenLoginModal);
  window.addEventListener("authChange", syncUserAvatar);
  window.addEventListener("userAvatarChange", syncUserAvatar);
  startUnreadSync();
});

watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    startUnreadSync();
    return;
  }
  resetNotificationSummary();
  stopUnreadSync();
});

onUnmounted(() => {
  stopUnreadSync();
  window.removeEventListener("open-login-modal", handleOpenLoginModal);
  window.removeEventListener("authChange", syncUserAvatar);
  window.removeEventListener("userAvatarChange", syncUserAvatar);
});
</script>

<style scoped>
.page-header {
  background-color: var(--forum-dark-color);
  color: white;
  padding: var(--header-padding);
  box-shadow: var(--shadow-medium);
}

.page-header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: var(--container-max-width);
  margin: 0 auto;
  padding: var(--container-padding);
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.page-title {
  margin: 0;
  font-size: var(--page-title-size);
  font-weight: 600;
}

.nav-buttons {
  display: flex;
  gap: 20px;
}

.nav-button {
  color: white;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.user-actions {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.header-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  width: 100%;
  max-width: 360px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 8px 12px;
  font-size: 13px;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  border: none;
  background: #e3e5e7;
  color: #505050;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: #dcdfe6;
}

.user-avatar-container {
  position: relative;
}

.nav-link {
  color: white;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.nav-link:hover {
  color: rgba(255, 255, 255, 0.8);
}

.user-avatar {
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.avatar-image {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-unread-dot {
  position: absolute;
  top: 1px;
  right: 0;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #ff4757;
  border: 2px solid var(--forum-dark-color);
}

.menu-unread-badge {
  min-width: 18px;
  height: 18px;
  margin-left: auto;
  padding: 0 6px;
  border-radius: 999px;
  background: #ff4757;
  color: white;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 150px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 200;
  margin-top: 8px;
}

.user-dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  color: #333;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
}

.dropdown-item i {
  font-size: 16px;
  color: #666;
}

.dropdown-divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 4px 0;
}

.auth-circle-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: #fff;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    opacity 0.2s,
    transform 0.2s;
  flex-shrink: 0;
}

.auth-circle-btn:hover {
  opacity: 0.88;
  transform: translateY(-1px);
}
</style>
