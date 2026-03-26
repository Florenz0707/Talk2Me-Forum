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
            type="text"
            v-model="searchKeyword"
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
            </router-link>
            <div class="user-dropdown-menu" :class="{ show: showUserMenu }">
              <div class="dropdown-item" @click="goToMessages">
                <i class="fas fa-envelope"></i>
                <span>我的消息</span>
              </div>
              <div class="dropdown-item" @click="goToLikes">
                <i class="fas fa-heart"></i>
                <span>收到的赞</span>
              </div>
              <div class="dropdown-item" @click="goToFollowers">
                <i class="fas fa-user-plus"></i>
                <span>新的粉丝</span>
              </div>
              <div class="dropdown-item" @click="goToReplies">
                <i class="fas fa-comment"></i>
                <span>回复我的</span>
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
import { computed, inject, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import AuthModal from "./AuthModal.vue";

// 接收props
defineProps({
  title: {
    type: String,
    default: "论坛首页",
  },
});

const router = useRouter();

// 从全局注入获取登录状态
const isLoggedIn = inject("isLoggedIn");

// 计算是否显示登录/注册按钮
const showAuthButtons = computed(() => !isLoggedIn.value);

// 用户菜单控制
const showUserMenu = ref(false);

// 用户头像
const userAvatar = ref("");

// Auth modal state
const showAuthModal = ref(false);
const authModalTab = ref("login");

const openModal = (tab) => {
  authModalTab.value = tab;
  showAuthModal.value = true;
};

// 搜索关键词
const searchKeyword = ref("");

// 搜索处理函数
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: "/search",
      query: { keyword: searchKeyword.value.trim() },
    });
  }
};

// 跳转到新消息
const goToMessages = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "private" },
  });
  showUserMenu.value = false;
};

// 跳转到收到的赞
const goToLikes = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "likes" },
  });
  showUserMenu.value = false;
};

// 跳转到新的粉丝
const goToFollowers = () => {
  router.push({ path: "/user", query: { statsTab: "followers" } });
  showUserMenu.value = false;
};

// 跳转到回复我的
const goToReplies = () => {
  router.push({
    path: "/user",
    query: { tab: "messages", messageTab: "replies" },
  });
  showUserMenu.value = false;
};

// 加载用户头像
onMounted(() => {
  const savedAvatar = localStorage.getItem("userAvatar");
  if (savedAvatar) {
    userAvatar.value = savedAvatar;
  }

  // 监听需要打开登录弹窗的事件（如 token 过期、401 响应）
  window.addEventListener("open-login-modal", () => openModal("login"));
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
}

.avatar-image {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
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
