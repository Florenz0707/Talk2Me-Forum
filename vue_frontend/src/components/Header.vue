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
      <div class="user-actions">
        <router-link v-if="showAuthButtons" to="/login" class="btn btn-primary"
          >登录</router-link
        >
        <router-link
          v-if="showAuthButtons"
          to="/register"
          class="btn btn-secondary"
          >注册</router-link
        >
        <template v-else>
          <div
            class="user-avatar-container"
            @mouseenter="showUserMenu = true"
            @mouseleave="showUserMenu = false"
          >
            <router-link to="/user" class="nav-link user-avatar">
              <i class="fas fa-user-circle"></i>
            </router-link>
            <div class="user-dropdown-menu" :class="{ show: showUserMenu }">
              <div class="dropdown-item">
                <i class="fas fa-envelope"></i>
                <span>新消息</span>
              </div>
              <div class="dropdown-item">
                <i class="fas fa-palette"></i>
                <span>主题颜色</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, inject, ref } from "vue";
import { useRouter } from "vue-router";

// 接收props
const props = defineProps({
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
  font-size: 24px;
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
</style>
