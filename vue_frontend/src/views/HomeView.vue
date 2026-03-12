<template>
  <div
    class="home-page"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 导航栏 -->
    <Header title="论坛首页" :show-create-thread-btn="false" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <nav class="breadcrumb">
          <span>首页</span>
        </nav>

        <!-- 版块信息 -->
        <div class="forum-info">
          <h2 class="forum-title">综合讨论区</h2>
          <p class="forum-description">
            欢迎来到Talk2Me论坛综合讨论区，这里是用户交流的主要场所。
          </p>
        </div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="sort-options">
            <label for="sort">排序方式：</label>
            <select v-model="sortBy" id="sort" @change="handleSortChange">
              <option value="latest">最新发布</option>
              <option value="replies">回复最多</option>
              <option value="views">浏览最多</option>
            </select>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="thread-list">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <!-- 错误提示 -->
          <div v-else-if="error" class="error-container">
            <p class="error-message">{{ error }}</p>
            <button class="btn btn-primary" @click="loadThreads">重试</button>
          </div>

          <!-- 帖子列表 -->
          <table v-else class="thread-table">
            <thead>
              <tr>
                <th class="thread-info">主题</th>
                <th class="thread-author">作者</th>
                <th class="thread-time">发布时间</th>
                <th class="thread-replies">回复</th>
                <th class="thread-views">浏览</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="thread in threads"
                :key="thread.id"
                class="thread-item"
              >
                <td class="thread-info">
                  <div class="thread-title">
                    <router-link
                      :to="'/thread/' + thread.id"
                      :title="thread.title"
                      >{{ thread.title }}</router-link
                    >
                    <span v-if="thread.isHot" class="thread-tag hot">热门</span>
                    <span
                      v-if="thread.isRecommended"
                      class="thread-tag recommended"
                      >推荐</span
                    >
                  </div>
                </td>
                <td class="thread-author">
                  <span class="author-name">{{ thread.author }}</span>
                </td>
                <td class="thread-time">{{ formatTime(thread.createdAt) }}</td>
                <td class="thread-replies">{{ thread.replies }}</td>
                <td class="thread-views">{{ thread.views }}</td>
              </tr>
              <!-- 空数据提示 -->
              <tr v-if="threads.length === 0">
                <td colspan="5" class="empty-message">暂无帖子</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页控件 -->
        <div class="pagination">
          <button
            class="page-btn"
            :disabled="currentPage === 1"
            @click="handlePageChange(currentPage - 1)"
          >
            上一页
          </button>

          <span class="page-info"
            >第 {{ currentPage }} / {{ totalPages }} 页</span
          >

          <button
            class="page-btn"
            :disabled="currentPage === totalPages"
            @click="handlePageChange(currentPage + 1)"
          >
            下一页
          </button>

          <div class="page-jump">
            <span>跳转至</span>
            <input
              type="number"
              v-model.number="jumpPage"
              :min="1"
              :max="totalPages"
              @keyup.enter="handlePageJump"
            />
            <span>页</span>
            <button class="btn btn-small" @click="handlePageJump">确定</button>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="container">
        <p>© 2024 Talk2Me论坛 - 技术支持：Vue 3 + Spring Boot</p>
      </div>
    </footer>
  </div>
  <!-- 固定定位的发帖按钮 -->
  <router-link to="/create-thread" class="fixed-create-thread-btn">
    <i class="fas fa-pen"></i>
  </router-link>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, inject } from "vue";
import { useRouter, useRoute } from "vue-router";
import Header from "../components/Header.vue";

const router = useRouter();
const route = useRoute();

// 离开页面动画控制
const isLeaving = ref(false);
let navigationGuard = null;

// 进入页面动画控制
const isEntering = ref(true);

// 从全局注入获取登录状态
const checkLoginStatus = inject("checkLoginStatus");

// 帖子列表数据
const threads = ref([]);

// 分页相关数据
const currentPage = ref(1);
const totalPages = ref(1);
const jumpPage = ref(1);
const totalPosts = ref(0);

// 排序
const sortBy = ref("latest");

// 加载状态和错误处理
const loading = ref(false);
const error = ref(null);

// 格式化时间
const formatTime = (timeString) => {
  const date = new Date(timeString);
  const now = new Date();
  const diff = now - date;

  // 计算时间差
  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 30) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

// 加载帖子数据
const loadThreads = async () => {
  loading.value = true;
  error.value = null;

  try {
    // 构建请求URL
    const url = new URL("http://localhost:8099/talk2me/api/v1/posts");
    url.searchParams.append("page", currentPage.value);
    url.searchParams.append("size", 20);

    // 发送请求
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error("请求失败");
    }

    const data = await response.json();

    // 处理响应数据
    if (data.code === 200 && data.data) {
      // 转换API返回的数据结构为前端需要的格式
      threads.value = data.data.records.map((post) => ({
        id: post.id,
        title: post.title,
        author: post.userId, // 这里需要根据实际情况调整，可能需要从用户服务获取用户名
        createdAt: post.createTime,
        replies: post.replyCount,
        views: post.viewCount,
        isHot: post.replyCount > 30, // 根据回复数判断是否热门
        isRecommended: post.likeCount > 20, // 根据点赞数判断是否推荐
      }));

      // 更新分页信息
      totalPages.value = data.data.pages;
      totalPosts.value = data.data.total;
      jumpPage.value = currentPage.value;
    } else {
      throw new Error(data.message || "获取帖子列表失败");
    }
  } catch (err) {
    error.value = err.message;
    console.error("加载帖子失败:", err);
  } finally {
    loading.value = false;
  }
};

// 处理排序变化
const handleSortChange = () => {
  console.log("排序方式变更为:", sortBy.value);
  // 重置页码
  currentPage.value = 1;
  jumpPage.value = 1;
  loadThreads();
};

// 处理分页变化
const handlePageChange = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    jumpPage.value = page;
    loadThreads();
  }
};

// 处理页面跳转
const handlePageJump = () => {
  let page = jumpPage.value;
  if (page < 1) page = 1;
  if (page > totalPages.value) page = totalPages.value;

  currentPage.value = page;
  jumpPage.value = page;
  loadThreads();
};

// 页面加载时初始化数据
onMounted(() => {
  checkLoginStatus();
  loadThreads();

  // 进入动画：组件挂载后延迟清除 isEntering，让 keyframe 动画完整播放
  setTimeout(() => {
    isEntering.value = false;
  }, 600);

  // 设置导航守卫
  navigationGuard = router.beforeEach((to, from, next) => {
    if (from.path === route.path) {
      // 从当前页面离开，触发动画
      isLeaving.value = true;

      // 等待动画完成后再导航
      setTimeout(() => {
        next();
      }, 500); // 0.5秒动画时间
    } else {
      next();
    }
  });
});

// 组件卸载时清除导航守卫
onBeforeUnmount(() => {
  if (navigationGuard) {
    navigationGuard(); // 调用返回的函数移除守卫
  }
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 页面进入时的渐入动画 */
.home-page.fade-enter-active {
  animation: homePageEnter 0.5s ease-out;
}

@keyframes homePageEnter {
  from {
    opacity: 0;
    filter: blur(20px);
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    filter: blur(0);
    transform: scale(1);
  }
}

/* 页面离开时的淡出动画 */
.home-page.fade-leave-active {
  animation: homePageLeave 0.5s ease-in-out forwards;
}

@keyframes homePageLeave {
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

/* 固定定位的发帖按钮 */
.fixed-create-thread-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 999;
}

.fixed-create-thread-btn:hover {
  background-color: var(--secondary-color);
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 移动端适配 */
@media (max-width: 1024px) {
  .fixed-create-thread-btn {
    width: 50px;
    height: 50px;
    font-size: 20px;
    bottom: 20px;
    right: 20px;
  }
}

@media (max-width: 768px) {
  .fixed-create-thread-btn {
    width: 45px;
    height: 45px;
    font-size: 18px;
    bottom: 15px;
    right: 15px;
  }
}

/* 主内容区域 */
.main-content {
  flex: 1;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

/* 版块信息 */
.forum-info {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.forum-title {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.forum-description {
  margin: 0;
  color: #666;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background-color: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-options label {
  font-size: 14px;
  color: #666;
}

.sort-options select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

/* 帖子列表 */
.thread-list {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 错误提示 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
}

.error-message {
  color: #dc3545;
  margin-bottom: 16px;
  font-size: 16px;
}

/* 空数据提示 */
.empty-message {
  text-align: center;
  padding: 60px 0;
  color: #666;
  font-size: 16px;
}

.thread-table {
  width: 100%;
  border-collapse: collapse;
}

.thread-table thead {
  background-color: #f8f9fa;
  border-bottom: 2px solid #e9ecef;
}

.thread-table th {
  padding: 15px 20px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.thread-table td {
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  font-size: 14px;
}

.thread-info {
  width: 50%;
}

.thread-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.thread-title a {
  color: #2c3e50;
  text-decoration: none;
  font-weight: 500;
  line-height: 1.5;
}

.thread-title a:hover {
  color: #3498db;
}

.thread-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.thread-tag.hot {
  background-color: #ffebee;
  color: #c62828;
}

.thread-tag.recommended {
  background-color: #e3f2fd;
  color: #1565c0;
}

.thread-author {
  width: 15%;
}

.author-name {
  color: #666;
}

.thread-time,
.thread-replies,
.thread-views {
  width: 10%;
  color: #999;
  text-align: center;
}

/* 分页控件 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #ccc;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.page-jump input {
  width: 60px;
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
  background-color: var(--background-color);
  color: var(--text-color);
  border: 1px solid var(--forum-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-small:hover {
  opacity: 0.85;
}

/* 页脚 */
.page-footer {
  background-color: #2c3e50;
  color: white;
  text-align: center;
  padding: 20px 0;
  margin-top: auto;
}

.page-footer .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 搜索栏 */
  .search-bar {
    padding: 10px 0;
  }

  .search-form {
    gap: 8px;
  }

  .search-input {
    padding: 8px 12px;
    font-size: 14px;
  }

  .search-button {
    padding: 8px 16px;
    font-size: 14px;
  }

  .search-button span {
    display: none;
  }

  .action-bar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .sort-options {
    justify-content: space-between;
  }

  .thread-table {
    display: block;
    overflow-x: auto;
  }

  .thread-info {
    width: 40%;
  }

  .thread-author,
  .thread-last {
    width: 20%;
  }

  .thread-time,
  .thread-replies,
  .thread-views {
    width: 10%;
  }

  .pagination {
    flex-direction: column;
    gap: 15px;
  }

  .page-jump {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .main-content .container {
    padding: 0 10px;
  }

  .forum-info,
  .action-bar,
  .thread-list,
  .pagination {
    padding: 15px;
  }

  .thread-table th,
  .thread-table td {
    padding: 10px;
    font-size: 13px;
  }

  .thread-title a {
    font-size: 13px;
  }

  .thread-tag {
    font-size: 11px;
    padding: 1px 6px;
  }
}
</style>
