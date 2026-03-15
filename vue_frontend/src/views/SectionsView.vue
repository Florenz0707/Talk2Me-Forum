<template>
  <div
    class="sections-page"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 顶部导航栏 -->
    <Header title="板块" :show-create-thread-btn="true" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="sections-container">
        <div v-if="loading" class="loading-state">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>
        <div v-else-if="error" class="error-state">
          <i class="fas fa-exclamation-circle"></i>
          <span>{{ error }}</span>
        </div>
        <div v-else class="section-category">
          <h2 class="category-title">全部板块</h2>
          <div class="sections-grid">
            <router-link
              v-for="section in sections"
              :key="section.id"
              :to="`/section/${section.id}`"
              class="section-card"
            >
              <div class="section-icon">
                <i class="fas fa-comments"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">{{ section.name }}</h3>
                <p class="section-desc">{{ section.description }}</p>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import Header from "../components/Header.vue";
import { sectionApi } from "../utils/api.js";

const router = useRouter();

// 页面切换动画
const isLeaving = ref(false);
const isEntering = ref(true);

// 板块数据
const sections = ref([]);
const loading = ref(false);
const error = ref(null);

// 获取所有板块
const fetchSections = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await sectionApi.getAllSections();
    sections.value = response.data || [];
  } catch (err) {
    error.value = "加载板块失败，请稍后重试";
    console.error("获取板块列表失败:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 监听路由变化，添加离开动画
  router.beforeEach((to, from, next) => {
    if (from.path === "/sections") {
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

  fetchSections();
});

onUnmounted(() => {
  // 清理路由监听器
  router.beforeEach(() => {});
});
</script>

<style scoped>
.sections-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.sections-container {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.section-category {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.sections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.section-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background-color: #f9f9f9;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
}

.section-card:hover {
  background-color: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.loading-state i,
.error-state i {
  font-size: 32px;
  color: #ccc;
  margin-bottom: 16px;
}

.loading-state span,
.error-state span {
  font-size: 16px;
  color: #999;
}

.section-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  flex-shrink: 0;
}

.section-info {
  flex: 1;
}

.section-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.section-desc {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.3;
}

/* 动画效果 */
.sections-page.fade-enter-active {
  animation: sectionsPageEnter 0.5s ease-out;
}

@keyframes sectionsPageEnter {
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

.sections-page.fade-leave-active {
  animation: sectionsPageLeave 0.3s ease-in forwards;
}

@keyframes sectionsPageLeave {
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
  .sections-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 16px;
  }

  .section-card {
    padding: 12px;
  }

  .section-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 20px 16px;
  }

  .section-category {
    padding: 16px;
  }

  .sections-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .section-card {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .section-icon {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
}
</style>
