<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, provide } from "vue";
import { notificationWS } from "./utils/websocket";
import { authApi } from "./utils/api";

// 全局登录状态
const isLoggedIn = ref(false);

// 检查用户登录状态
const checkLoginStatus = async () => {
  try {
    isLoggedIn.value = await authApi.ensureSession();
  } catch (error) {
    console.error("检查登录状态失败:", error);
    isLoggedIn.value = false;
  }
};

// 更新登录状态
const updateLoginStatus = (status) => {
  isLoggedIn.value = status;
};

// 全局深色模式状态
const isDarkMode = ref(false);

// 切换深色模式
const toggleDarkMode = () => {
  document.documentElement.classList.add("color-transitioning");
  isDarkMode.value = !isDarkMode.value;
  if (isDarkMode.value) {
    document.documentElement.classList.add("dark");
    localStorage.setItem("darkMode", "true");
  } else {
    document.documentElement.classList.remove("dark");
    localStorage.setItem("darkMode", "false");
  }
  setTimeout(() => {
    document.documentElement.classList.remove("color-transitioning");
  }, 400);
};

// 提供全局状态和方法
provide("isLoggedIn", isLoggedIn);
provide("checkLoginStatus", checkLoginStatus);
provide("updateLoginStatus", updateLoginStatus);
provide("isDarkMode", isDarkMode);
provide("toggleDarkMode", toggleDarkMode);

const handleAuthChange = (event) => {
  isLoggedIn.value = !!event.detail.isAuthenticated;
  if (event.detail.isAuthenticated) {
    notificationWS.connect();
  } else {
    notificationWS.disconnect();
  }
};

// 组件挂载时检查登录状态及深色模式偏好
onMounted(() => {
  checkLoginStatus();

  // 恢复深色模式设置
  const savedDarkMode = localStorage.getItem("darkMode");
  if (savedDarkMode === "true") {
    isDarkMode.value = true;
    document.documentElement.classList.add("dark");
  }

  // 初始化 WebSocket 连接
  if (isLoggedIn.value) {
    notificationWS.connect();
  }

  // 监听localStorage变化，实现多标签页同步
  window.addEventListener("authChange", handleAuthChange);

  // 监听 authChange 事件（同 Tab 内 token 清除或登录成功时触发）
});

onUnmounted(() => {
  window.removeEventListener("authChange", handleAuthChange);
  notificationWS.disconnect();
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

body {
  background-color: var(--background-color);
}

#app {
  min-height: 100vh;
}

/* ===== 深色模式全局覆盖 ===== */
/* 页面整体背景 */
html.dark body,
html.dark #app,
html.dark .home-page,
html.dark .user-view-container,
html.dark .main-content {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

/* 卡片/面板类白色背景 */
html.dark .forum-info,
html.dark .action-bar,
html.dark .thread-list,
html.dark .pagination,
html.dark .content-card,
html.dark .main-content-wrapper,
html.dark .left-sidebar,
html.dark .nav-menu,
html.dark .right-content,
html.dark .user-profile-section {
  background-color: #1e1e1e !important;
  color: #e0e0e0 !important;
}

html.dark .stats-wrapper {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

/* 消息、收藏、设置列表项 */
html.dark .message-item,
html.dark .fan-item,
html.dark .setting-item,
html.dark .thread-table-container {
  background-color: #2a2a2a !important;
  color: #e0e0e0 !important;
}

html.dark .message-item:hover,
html.dark .fan-item:hover,
html.dark .setting-item:hover {
  background-color: #333 !important;
}

/* 表格 */
html.dark .thread-table thead,
html.dark .thread-header {
  background-color: #2d2d2d !important;
}

html.dark .thread-table th,
html.dark .thread-header th {
  color: #ccc !important;
  border-color: #444 !important;
}

html.dark .thread-table td,
html.dark .thread-item td {
  color: #ccc !important;
  border-color: #333 !important;
}

html.dark .thread-item {
  background-color: #2a2a2a !important;
  border: none !important;
}

html.dark .thread-item:hover,
html.dark .thread-table tbody tr:hover {
  background-color: #333 !important;
}

/* 标题文字 */
html.dark h1,
html.dark h2,
html.dark h3,
html.dark .forum-title,
html.dark .content-card h2,
html.dark .stat-number,
html.dark .username-large,
html.dark .forum-title,
html.dark .thread-title a,
html.dark .message-sender,
html.dark .fan-name,
html.dark .form-group label {
  color: #e0e0e0 !important;
}

/* 次级文字 */
html.dark .forum-description,
html.dark .author-name,
html.dark .thread-time,
html.dark .thread-replies,
html.dark .thread-views,
html.dark .stat-label,
html.dark .message-text,
html.dark .message-time,
html.dark .fan-id,
html.dark .breadcrumb,
html.dark .page-info,
html.dark .sort-options label,
html.dark .setting-item span {
  color: #aaa !important;
}

/* 导航菜单 */
html.dark .nav-item {
  color: #ccc !important;
}

html.dark .nav-item:hover {
  background-color: #2a2a2a !important;
}

html.dark .nav-item i {
  color: #aaa !important;
}

html.dark .nav-item.active {
  background-color: #1a3a5c !important;
  color: #5dade2 !important;
}

html.dark .nav-item.active i,
html.dark .nav-item.router-link-active i {
  color: #5dade2 !important;
}

/* 表单输入框 */
html.dark .form-input,
html.dark .form-textarea,
html.dark .sort-options select,
html.dark .page-jump input {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

/* 分页按钮 */
html.dark .page-btn {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #ccc !important;
}

html.dark .page-btn:hover:not(:disabled) {
  background-color: #3a3a3a !important;
}

/* 下拉菜单 */
html.dark .user-dropdown-menu {
  background-color: #1e1e1e !important;
  border-color: #444 !important;
}

html.dark .dropdown-item {
  color: #ccc !important;
}

html.dark .dropdown-item:hover {
  background-color: #2d2d2d !important;
}

html.dark .dropdown-item i {
  color: #aaa !important;
}

/* Header 背景 */
html.dark .page-header {
  background-color: #1a2332 !important;
}

/* 搜索框 */
html.dark .search-box {
  background: #2d2d2d !important;
}

html.dark .search-input {
  color: #e0e0e0 !important;
  background: transparent !important;
}

html.dark .search-input::placeholder {
  color: #777 !important;
}

html.dark .search-btn {
  background: #3a3a3a !important;
  color: #aaa !important;
}

/* 消息头像、粉丝头像 */
html.dark .message-avatar,
html.dark .fan-avatar {
  background-color: #3a3a3a !important;
  color: #aaa !important;
}

/* 修复缺失的边框颜色覆盖 */
html.dark .thread-item {
  border-bottom-color: #333 !important;
}

html.dark .logout-item {
  border-top-color: #444 !important;
}

html.dark .right-content {
  border-left-color: #333 !important;
}

/* ===== HomeView 白色边框修复 ===== */
/* 卡片阴影在深色模式下改为暗色边框风格 */
html.dark .forum-info,
html.dark .action-bar,
html.dark .thread-list,
html.dark .pagination {
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

/* 修复表格 thead 的亮色下边框 */
html.dark .thread-table thead {
  border-bottom-color: #444 !important;
}

/* ===== CreateThreadView 深色模式适配 ===== */
html.dark .create-thread-page {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

html.dark .create-thread-form {
  background-color: #1e1e1e !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .section-title {
  color: #e0e0e0 !important;
}

html.dark .editor-toolbar {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
}

html.dark .toolbar-btn {
  background-color: #252525 !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .toolbar-btn:hover {
  background-color: #5dade2 !important;
  border-color: #5dade2 !important;
  color: #fff !important;
}

html.dark .form-select {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .editor-textarea {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .editor-textarea::placeholder {
  color: #777 !important;
}

html.dark .preview-section {
  background-color: #252525 !important;
  border-color: #444 !important;
}

html.dark .preview-title {
  color: #ccc !important;
}

html.dark .preview-content {
  color: #e0e0e0 !important;
}

html.dark .drafts-list {
  background-color: #252525 !important;
  border-color: #444 !important;
}

html.dark .drafts-list h3 {
  color: #e0e0e0 !important;
}

html.dark .no-drafts {
  color: #aaa !important;
}

html.dark .draft-title {
  color: #e0e0e0 !important;
}

html.dark .draft-time {
  color: #777 !important;
}

html.dark .draft-item {
  border-bottom-color: #444 !important;
}

/* ===== SectionsView 深色模式适配 ===== */
html.dark .sections-page {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

html.dark .section-category {
  background-color: #1e1e1e !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .category-title {
  color: #e0e0e0 !important;
  border-bottom-color: #444 !important;
}

html.dark .section-card {
  background-color: #252525 !important;
  border-color: #444 !important;
}

html.dark .section-card:hover {
  background-color: #2d2d2d !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5) !important;
}

html.dark .section-name {
  color: #e0e0e0 !important;
}

html.dark .section-desc {
  color: #aaa !important;
}

/* ===== SectionView 深色模式适配 ===== */
html.dark .section-page {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

html.dark .section-page .sort-filter-bar,
html.dark .section-page .loading-state,
html.dark .section-page .empty-state {
  background-color: #1e1e1e !important;
  color: #e0e0e0 !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .section-page .thread-item {
  background-color: #1e1e1e !important;
  color: #e0e0e0 !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .section-page .thread-item:hover {
  background-color: #2a2a2a !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5) !important;
}

html.dark .section-page .thread-title {
  color: #e0e0e0 !important;
}

html.dark .section-page .thread-meta,
html.dark .section-page .thread-time,
html.dark .section-page .thread-stats,
html.dark .section-page .page-info {
  color: #aaa !important;
}

html.dark .section-page .sort-options label {
  color: #aaa !important;
}

html.dark .section-page .sort-options select {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .section-page .page-btn {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #ccc !important;
}

html.dark .section-page .page-btn:hover:not(:disabled) {
  background-color: #3a3a3a !important;
}

html.dark .section-page .loading-state i,
html.dark .section-page .empty-state i {
  color: #555 !important;
}

/* ===== ThreadDetailView 深色模式适配 ===== */
html.dark .thread-detail-page {
  background-color: #121212 !important;
}

html.dark .thread-detail-card,
html.dark .comments-section {
  background-color: #1e1e1e !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .thread-title,
html.dark .comment-author,
html.dark .reply-author {
  color: #e0e0e0 !important;
}

html.dark .thread-content,
html.dark .comment-text,
html.dark .reply-text {
  color: #ccc !important;
}

html.dark .thread-content :deep(h3) {
  color: #e0e0e0 !important;
}

html.dark .section-tag {
  background-color: #2d2d2d !important;
  color: #aaa !important;
}

html.dark .section-tag:hover {
  background-color: #3a3a3a !important;
}

html.dark .author-info,
html.dark .thread-stats {
  border-color: #444 !important;
}

html.dark .breadcrumb-current,
html.dark .post-time,
html.dark .stat-item,
html.dark .comment-time,
html.dark .reply-time,
html.dark .input-tip {
  color: #aaa !important;
}

html.dark .comment-item {
  border-bottom-color: #333 !important;
}

html.dark .reply-item {
  background-color: #252525 !important;
}

html.dark .input-area textarea,
html.dark .reply-input-wrapper textarea {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .input-area textarea::placeholder,
html.dark .reply-input-wrapper textarea::placeholder {
  color: #777 !important;
}

html.dark .floating-btn,
html.dark .back-to-top-btn {
  background-color: #2d2d2d !important;
  color: #ccc !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5) !important;
}

html.dark .floating-btn:hover,
html.dark .back-to-top-btn:hover {
  background-color: #3a3a3a !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.7) !important;
}

html.dark .page-footer {
  background-color: #1e1e1e !important;
  color: #aaa !important;
}

/* ===== OtherUserView 深色模式适配 ===== */
html.dark .other-user-view {
  background-color: #121212 !important;
}

html.dark .user-profile-section {
  background-color: #121212 !important;
  color: #e0e0e0 !important;
}

html.dark .username-large {
  color: #e0e0e0 !important;
}

html.dark .user-bio {
  color: #aaa !important;
}

html.dark .stat-item {
  color: #e0e0e0 !important;
}

html.dark .stat-label {
  color: #aaa !important;
}

html.dark .btn-message {
  background-color: #2d2d2d !important;
  color: #e0e0e0 !important;
  border-color: #444 !important;
}

html.dark .btn-message:hover {
  background-color: #3a3a3a !important;
}

html.dark .content-card {
  background-color: #1e1e1e !important;
  box-shadow: none !important;
  border: 1px solid #333 !important;
}

html.dark .content-card h2 {
  color: #e0e0e0 !important;
}

html.dark .thread-item {
  background-color: #252525 !important;
}

html.dark .thread-item:hover {
  background-color: #2d2d2d !important;
}

html.dark .thread-title {
  color: #e0e0e0 !important;
}

html.dark .thread-meta {
  color: #aaa !important;
}

html.dark .empty-state {
  color: #777 !important;
}

html.dark .empty-state i {
  color: #555 !important;
}

/* ===== UserView 新增功能深色模式适配 ===== */
/* 统计数据hover效果 */
html.dark .stat-item:not(.disabled):hover {
  background-color: #2a2a2a !important;
}

html.dark .stat-item.stat-active .stat-number,
html.dark .stat-item.stat-active .stat-label {
  color: #7c93ee !important;
}

/* 用户列表 */
html.dark .user-item {
  background-color: #252525 !important;
}

html.dark .user-item:hover {
  background-color: #2d2d2d !important;
}

html.dark .user-item-name {
  color: #e0e0e0 !important;
}

html.dark .user-item-bio {
  color: #aaa !important;
}

html.dark .user-item-avatar i {
  color: #666 !important;
}

html.dark .unfollow-btn {
  background-color: #2d2d2d !important;
  color: #ccc !important;
}

html.dark .unfollow-btn:hover {
  background-color: #3a3a3a !important;
}

html.dark .follow-back-btn {
  background-color: #7c93ee !important;
  color: #fff !important;
}

html.dark .follow-back-btn:hover {
  opacity: 0.9;
}

/* 消息功能 */
html.dark .messages-layout {
  color: #e0e0e0 !important;
}

html.dark .contacts-sidebar {
  background-color: #252525 !important;
}

html.dark .contact-item {
  color: #ccc !important;
}

html.dark .contact-item:hover {
  background-color: #2d2d2d !important;
}

html.dark .contact-item.active {
  background-color: #7c93ee !important;
  color: #fff !important;
}

html.dark .new-message-form {
  background-color: #252525 !important;
}

html.dark .new-message-form h3 {
  color: #e0e0e0 !important;
}

html.dark .message-textarea {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .message-textarea:focus {
  border-color: #7c93ee !important;
}

html.dark .send-btn {
  background-color: #7c93ee !important;
  color: #fff !important;
}

html.dark .send-btn:hover {
  background-color: #6a81dc !important;
}

html.dark .cancel-btn {
  background-color: #2d2d2d !important;
  color: #ccc !important;
}

html.dark .cancel-btn:hover {
  background-color: #3a3a3a !important;
}

html.dark .target-user-card {
  background-color: #1a2a3a !important;
  border-color: #7c93ee !important;
}

html.dark .target-user-card .target-user-avatar i {
  color: #7c93ee !important;
}

html.dark .target-user-card .target-user-name {
  color: #e0e0e0 !important;
}

/* 深色模式切换平滑过渡动画 */
html.color-transitioning *,
html.color-transitioning *::before,
html.color-transitioning *::after {
  transition:
    background-color 0.3s ease,
    color 0.3s ease,
    border-color 0.3s ease !important;
}
</style>
