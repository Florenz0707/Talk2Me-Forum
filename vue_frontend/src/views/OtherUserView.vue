<template>
  <div
    class="other-user-view"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <Header title="用户主页" />

    <!-- 用户信息栏 -->
    <div class="user-profile-section">
      <div class="user-avatar-small">
        <div class="user-info-container">
          <div class="user-avatar-large">
            <i v-if="!userAvatar" class="fas fa-user-circle"></i>
            <img v-else :src="userAvatar" alt="用户头像" class="avatar-image" />
          </div>
          <div class="user-details">
            <div class="username-large">{{ username }}</div>
            <div class="user-bio">{{ userBio }}</div>
          </div>
        </div>

        <div class="stats-and-actions">
          <div class="stats-wrapper">
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
          <div class="action-buttons">
            <button class="btn-follow" @click="handleFollow">
              <i class="fas fa-user-plus"></i> 关注
            </button>
            <button class="btn-message" @click="handleMessage">
              <i class="fas fa-comment"></i> 发消息
            </button>
          </div>
        </div>
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
import Header from "../components/Header.vue";

export default {
  name: "OtherUserView",
  components: {
    Header,
  },
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
      router.push({
        path: "/user",
        query: {
          tab: "messages",
          targetUser: username.value,
          targetUserId: route.params.id,
        },
      });
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
      formatTime,
      goToThread,
      handleFollow,
      handleMessage,
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

.user-profile-section {
  background-color: #f5f7fa;
  padding: 40px 10px 10px 10px;
  display: flex;
  justify-content: center;
  align-items: end;
  color: #333;
  width: 100%;
  height: 100%;
}

.user-avatar-small {
  width: 77%;
  display: flex;
  align-items: end;
  justify-content: space-between;
  height: 100%;
}

.user-info-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--forum-dark-color);
  color: white;
  font-size: 80px;
  border-radius: 50%;
  flex-shrink: 0;
}

.avatar-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username-large {
  font-size: 28px;
  font-weight: 700;
}

.user-bio {
  font-size: 16px;
  color: #666;
}

.stats-and-actions {
  display: flex;
  align-items: center;
  gap: 40px;
}

.stats-wrapper {
  display: flex;
  gap: 40px;
  color: #333;
  background-color: transparent;
}

.stat-item {
  text-align: center;
  background: transparent;
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

.action-buttons {
  display: flex;
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
