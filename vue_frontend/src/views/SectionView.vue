<template>
  <div
    class="section-page"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 顶部导航栏 -->
    <Header :title="sectionName" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 排序和筛选 -->
      <div class="sort-filter-bar">
        <div class="sort-options">
          <label for="sort">排序方式：</label>
          <select v-model="sortBy" id="sort" @change="handleSortChange">
            <option value="latest">最新发布</option>
            <option value="views">浏览最多</option>
            <option value="replies">回复最多</option>
          </select>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="threads-list">
        <div v-if="loading" class="loading-state">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>
        <div v-else-if="threads.length === 0" class="empty-state">
          <i class="fas fa-file-alt"></i>
          <span>该板块暂无帖子</span>
        </div>
        <div v-else class="threads-container">
          <div
            v-for="thread in threads"
            :key="thread.id"
            class="thread-item"
            @click="navigateToThread(thread.id)"
          >
            <div class="thread-content">
              <h3 class="thread-title">{{ thread.title }}</h3>
              <div class="thread-meta">
                <span class="thread-author">{{ thread.author }}</span>
                <span class="thread-time">{{
                  formatTime(thread.created_at)
                }}</span>
                <span class="thread-stats">
                  <i class="fas fa-eye"></i> {{ thread.views }}
                  <i class="fas fa-comment"></i> {{ thread.replies }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div v-if="!loading && threads.length > 0" class="pagination">
        <button
          class="page-btn"
          :disabled="currentPage === 1"
          @click="handlePageChange(currentPage - 1)"
        >
          <i class="fas fa-chevron-left"></i>
        </button>
        <span class="page-info">
          第 {{ currentPage }} / {{ totalPages }} 页
        </span>
        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="handlePageChange(currentPage + 1)"
        >
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </main>

    <!-- 固定定位的发帖按钮 -->
    <router-link to="/create-thread" class="fixed-create-thread-btn">
      <i class="fas fa-pen"></i>
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import Header from "../components/Header.vue";
import { postApi, sectionApi } from "../utils/api";

const router = useRouter();
const route = useRoute();

// 页面切换动画
const isLeaving = ref(false);
const isEntering = ref(true);

// 板块ID和名称
const sectionId = ref(route.params.id || 1);
const sectionName = ref("加载中...");

// 帖子列表数据
const threads = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const totalPages = ref(1);

// 排序选项
const sortBy = ref("latest"); // latest, popular, commented

// 格式化时间
const formatTime = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;

  if (diff < 60000) {
    return "刚刚";
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + "分钟前";
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + "小时前";
  } else if (diff < 604800000) {
    return Math.floor(diff / 86400000) + "天前";
  } else {
    return date.toLocaleDateString();
  }
};

// 处理排序变化
const handleSortChange = () => {
  currentPage.value = 1;
  fetchThreads();
};

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchThreads();
};

// 导航到帖子详情页
const navigateToThread = (threadId) => {
  router.push(`/thread/${threadId}`);
};

// 获取板块名称
const fetchSectionName = async () => {
  try {
    const response = await sectionApi.getSectionById(sectionId.value);
    if (response.code === 200 && response.data) {
      sectionName.value = response.data.name || "未知板块";
    }
  } catch (error) {
    console.error("获取板块信息失败:", error);
    sectionName.value = "未知板块";
  }
};

// 获取帖子列表
const fetchThreads = async () => {
  loading.value = true;

  try {
    const response = await postApi.getPosts({
      sectionId: sectionId.value,
      page: currentPage.value,
      size: 20,
      sortBy: sortBy.value,
    });

    if (response.code === 200 && response.data) {
      threads.value = response.data.records.map((post) => ({
        id: post.id,
        title: post.title,
        author: post.username || `用户${post.userId}`,
        created_at: post.createTime,
        views: post.viewCount || 0,
        replies: post.replyCount || 0,
      }));
      totalPages.value = response.data.total_pages || 1;
    }
  } catch (error) {
    console.error("获取帖子列表失败:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 监听路由变化，添加离开动画
  router.beforeEach((to, from, next) => {
    if (from.path.startsWith("/section/")) {
      isLeaving.value = true;
      setTimeout(() => {
        next();
      }, 300);
    } else {
      next();
    }
  });

  // 进入动画：延迟清除 isEntering，让 keyframe 动画完整播放
  setTimeout(() => {
    isEntering.value = false;
  }, 600);

  // 获取板块名称和帖子列表
  fetchSectionName();
  fetchThreads();
});

onUnmounted(() => {
  // 清理路由监听器
  router.beforeEach(() => {});
});
</script>

<style scoped>
.section-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.main-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.sort-filter-bar {
  background-color: white;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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

.threads-list {
  margin-bottom: 30px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.loading-state i,
.empty-state i {
  font-size: 32px;
  color: #ccc;
  margin-bottom: 16px;
}

.loading-state span,
.empty-state span {
  font-size: 16px;
  color: #999;
}

.threads-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.thread-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.thread-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.thread-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.thread-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.thread-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.thread-author {
  font-weight: 500;
}

.thread-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.thread-stats i {
  margin-right: 4px;
  font-size: 12px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
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
  text-decoration: none;
}

.fixed-create-thread-btn:hover {
  background-color: var(--secondary-color);
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 动画效果 */
.section-page.fade-enter-active {
  animation: sectionPageEnter 0.5s ease-out;
}

@keyframes sectionPageEnter {
  from {
    opacity: 0;
    filter: blur(10px);
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
}

.section-page.fade-leave-active {
  animation: sectionPageLeave 0.3s ease-in forwards;
}

@keyframes sectionPageLeave {
  from {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
  to {
    opacity: 0;
    filter: blur(10px);
    transform: translateY(-20px);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    max-width: 900px;
    padding: 20px 16px;
  }

  .thread-item {
    padding: 16px;
  }

  .thread-title {
    font-size: 15px;
  }

  .fixed-create-thread-btn {
    width: 50px;
    height: 50px;
    font-size: 20px;
    bottom: 20px;
    right: 20px;
  }
}

@media (max-width: 768px) {
  .main-content {
    max-width: 100%;
    padding: 16px;
  }

  .sort-filter-bar {
    padding: 12px 16px;
  }

  .sort-options {
    flex-wrap: wrap;
    gap: 8px;
  }

  .thread-item {
    padding: 14px;
  }

  .thread-title {
    font-size: 14px;
  }

  .thread-meta {
    font-size: 12px;
    gap: 12px;
  }

  .fixed-create-thread-btn {
    width: 45px;
    height: 45px;
    font-size: 18px;
    bottom: 15px;
    right: 15px;
  }
}
</style>
