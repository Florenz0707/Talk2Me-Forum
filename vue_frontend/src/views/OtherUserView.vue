<template>
  <div
    class="other-user-view"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 头像展示栏 -->
    <div class="avatar-header">
      <div class="avatar-header-content">
        <!-- 用户信息栏 -->
        <div class="user">
          <div class="user-avatar-large">
            <i v-if="!userAvatar" class="fas fa-user-circle"></i>
            <img v-else :src="userAvatar" alt="用户头像" class="avatar-image" />
          </div>
          <div class="user-nav">
            <div class="user-info">
              <div class="username-large">{{ username }}</div>
              <div class="user-bio">{{ userBio }}</div>
            </div>
          </div>
        </div>

        <div class="user-nav-stats">
          <!-- 首页板块 -->
          <div class="nav-and-stats">
            <router-link to="/home" class="nav-link">首页</router-link>
            <router-link to="/sections" class="nav-link">板块</router-link>
          </div>

          <!-- 搜索框 -->
          <div class="search-box-top">
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

          <div class="nav-and-stats">
            <router-link to="/user" class="nav-link user-avatar-link">
              <i class="fas fa-user-circle"></i>
            </router-link>
          </div>
        </div>

        <div class="stats-wrapper">
          <!-- 统计信息 -->
          <div class="stat-item">
            <div class="stat-number">–</div>
            <div class="stat-label">获赞数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">–</div>
            <div class="stat-label">关注数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">–</div>
            <div class="stat-label">粉丝数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons-wrapper">
      <div class="action-buttons">
        <button class="btn-follow" @click="handleFollow">
          <i class="fas fa-user-plus"></i> 关注
        </button>
        <button class="btn-message" @click="handleMessage">
          <i class="fas fa-comment"></i> 发消息
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content-container">
      <div class="content-card">
        <h2>用户发表的帖子</h2>
        <div v-if="loading" class="empty-state">
          <i class="fas fa-spinner fa-spin"></i>
          <p>加载中...</p>
        </div>
        <div v-else-if="userPosts.length === 0" class="empty-state">
          <i class="fas fa-file-alt"></i>
          <p>该用户还没有发表过帖子</p>
        </div>
        <div v-else class="thread-list">
          <div
            v-for="post in userPosts"
            :key="post.id"
            class="thread-item"
            @click="goToThread(post.id)"
          >
            <div class="thread-title">{{ post.title }}</div>
            <div class="thread-meta">
              <span class="thread-time">{{ formatTime(post.createdAt) }}</span>
              <span class="thread-stats">
                <i class="fas fa-eye"></i> {{ post.views }}
                <i class="fas fa-comment"></i> {{ post.replies }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import { postApi } from "../utils/api";

export default {
  name: "OtherUserView",
  setup() {
    const router = useRouter();
    const route = useRoute();
    const isLeaving = ref(false);
    const isEntering = ref(true);
    const username = ref("用户");
    const userAvatar = ref("");
    const userBio = ref("");
    const userPosts = ref([]);
    const loading = ref(true);
    const searchKeyword = ref("");
    let navigationGuard = null;

    const formatTime = (timeString) => {
      if (!timeString) return "";
      const date = new Date(timeString);
      return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    const goToThread = (threadId) => {
      router.push(`/thread/${threadId}`);
    };

    const handleFollow = async () => {
      // TODO: 调用关注API
      console.log("关注用户");
    };

    const handleMessage = () => {
      console.log("发送消息");
    };

    const handleSearch = () => {
      if (searchKeyword.value.trim()) {
        router.push(`/search?q=${encodeURIComponent(searchKeyword.value)}`);
      }
    };

    const fetchUserPosts = async () => {
      const userId = route.params.id;
      if (!userId) return;

      loading.value = true;
      try {
        const response = await postApi.getPosts({ userId, page: 1, size: 50 });
        if (response.code === 200 && response.data) {
          userPosts.value = response.data.records.map((post) => ({
            id: post.id,
            title: post.title,
            createdAt: post.createTime,
            views: post.viewCount || 0,
            replies: post.replyCount || 0,
          }));
          if (response.data.records.length > 0) {
            username.value =
              response.data.records[0].username || `用户${userId}`;
          }
        }
      } catch (error) {
        console.error("获取用户帖子失败:", error);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      setTimeout(() => {
        isEntering.value = false;
      }, 500);

      fetchUserPosts();

      navigationGuard = router.beforeEach((to, from, next) => {
        if (from.path.startsWith("/user/")) {
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
    });

    return {
      isLeaving,
      isEntering,
      username,
      userAvatar,
      userBio,
      userPosts,
      loading,
      searchKeyword,
      formatTime,
      goToThread,
      handleFollow,
      handleMessage,
      handleSearch,
    };
  },
};
</script>

<style scoped>
.other-user-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.fade-enter-active {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    filter: blur(10px);
  }
  to {
    opacity: 1;
    filter: blur(0);
  }
}

.fade-leave-active {
  animation: fadeOut 0.5s ease-out forwards;
}

@keyframes fadeOut {
  from {
    opacity: 1;
    filter: blur(0);
  }
  to {
    opacity: 0;
    filter: blur(10px);
  }
}

.avatar-header {
  background-color: #2c3e50;
  height: 18vh;
  min-height: 100px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: stretch;
  color: white;
  position: relative;
  padding-top: 15px;
}

.search-box-top {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 20px;
  padding: 4px 8px;
  width: 60%;
  margin-bottom: 15px;
  margin-left: 7%;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 8px 12px;
  font-size: 14px;
  background: transparent;
  color: #333;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  padding: 8px 12px;
  font-size: 16px;
}

.search-btn:hover {
  color: #5568d3;
}

.avatar-header-content {
  max-width: 1500px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 30px;
  justify-content: space-between;
}

.user {
  display: flex;
  align-items: end;
  justify-content: flex-start;
  flex: 1;
  width: 300px;
  gap: 10px;
  height: 120px;
}

.user-avatar-large {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 60px;
}

.avatar-image {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-nav {
  display: flex;
  align-items: end;
  height: 100%;
}

.user-info {
  flex: 1;
  max-width: 100px;
  padding: 0 12px;
}

.username-large {
  width: 100px;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.user-bio {
  font-size: 16px;
  color: #e0e0e0;
}

.user-nav-stats {
  display: flex;
  align-items: start;
  justify-content: space-evenly;
  height: 100%;
  width: 80%;
}

.nav-and-stats {
  display: flex;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-avatar-link {
  font-size: 28px;
  padding: 4px 8px;
}

.stats-wrapper {
  display: flex;
  gap: 40px;
  align-items: end;
  height: 100%;
  padding: 0 0 20px;
  width: 300px;
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.action-buttons-wrapper {
  width: 100%;
  margin-top: 20px;
  padding: 0 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
}

.action-buttons {
  width: 80%;
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.action-buttons button {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-follow {
  background-color: #667eea;
  color: white;
}

.btn-follow:hover {
  background-color: #5568d3;
  transform: translateY(-2px);
}

.btn-message {
  background-color: white;
  color: #333;
  border: 1px solid #ddd;
}

.btn-message:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
}

.stats-container {
  display: none;
}

.main-content-container {
  width: 100%;
  padding: 0 20px 40px;
  display: flex;
  justify-content: center;
}

.content-card {
  width: 80%;
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.content-card h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
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

.thread-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.thread-item {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.thread-item:hover {
  background-color: #f0f0f0;
  transform: translateX(4px);
}

.thread-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.thread-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.thread-stats {
  display: flex;
  gap: 16px;
}

.thread-stats i {
  margin-right: 4px;
}
</style>
