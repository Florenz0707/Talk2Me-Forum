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
        <div class="section-category">
          <h2 class="category-title">综合讨论</h2>
          <div class="sections-grid">
            <router-link to="/section/1" class="section-card">
              <div class="section-icon">
                <i class="fas fa-code"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">软件开发与工程</h3>
                <p class="section-desc">软件设计、开发流程、工程实践</p>
              </div>
            </router-link>
            <router-link to="/section/2" class="section-card">
              <div class="section-icon">
                <i class="fas fa-microchip"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">硬件组件与架构</h3>
                <p class="section-desc">计算机硬件、芯片设计、系统架构</p>
              </div>
            </router-link>
            <router-link to="/section/3" class="section-card">
              <div class="section-icon">
                <i class="fas fa-laptop-code"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">编程语言与框架</h3>
                <p class="section-desc">Python、Java、C++等语言讨论</p>
              </div>
            </router-link>
            <router-link to="/section/4" class="section-card">
              <div class="section-icon">
                <i class="fas fa-database"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">数据库与存储</h3>
                <p class="section-desc">SQL、NoSQL、数据建模</p>
              </div>
            </router-link>
            <router-link to="/section/5" class="section-card">
              <div class="section-icon">
                <i class="fas fa-network-wired"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">网络与安全</h3>
                <p class="section-desc">网络协议、安全漏洞、防护措施</p>
              </div>
            </router-link>
            <router-link to="/section/6" class="section-card">
              <div class="section-icon">
                <i class="fas fa-brain"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">人工智能与机器学习</h3>
                <p class="section-desc">AI算法、深度学习、数据分析</p>
              </div>
            </router-link>
            <router-link to="/section/7" class="section-card">
              <div class="section-icon">
                <i class="fas fa-cloud"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">云计算与大数据</h3>
                <p class="section-desc">云服务、分布式系统、大数据处理</p>
              </div>
            </router-link>
            <router-link to="/section/8" class="section-card">
              <div class="section-icon">
                <i class="fas fa-mobile-alt"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">移动开发</h3>
                <p class="section-desc">iOS、Android、跨平台开发</p>
              </div>
            </router-link>
            <router-link to="/section/9" class="section-card">
              <div class="section-icon">
                <i class="fas fa-desktop"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">前端开发</h3>
                <p class="section-desc">HTML、CSS、JavaScript、框架</p>
              </div>
            </router-link>
            <router-link to="/section/10" class="section-card">
              <div class="section-icon">
                <i class="fas fa-server"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">后端开发</h3>
                <p class="section-desc">服务器端编程、API设计</p>
              </div>
            </router-link>
            <router-link to="/section/11" class="section-card">
              <div class="section-icon">
                <i class="fas fa-robot"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">新兴技术</h3>
                <p class="section-desc">区块链、元宇宙、量子计算</p>
              </div>
            </router-link>
            <router-link to="/section/12" class="section-card">
              <div class="section-icon">
                <i class="fas fa-graduation-cap"></i>
              </div>
              <div class="section-info">
                <h3 class="section-name">计算机科学理论</h3>
                <p class="section-desc">算法、数据结构、计算理论</p>
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

const router = useRouter();

// 页面切换动画
const isLeaving = ref(false);
const isEntering = ref(true);

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

  // 进入动画
  setTimeout(() => {
    isEntering.value = false;
  }, 100);
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

.sections-page * {
  transition: all 0.5s ease-in-out;
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
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.3s,
    transform 0.3s;
}

.fade-enter-active {
  opacity: 0;
  transform: translateY(20px);
}

.fade-enter-active.fade-enter-active {
  opacity: 1;
  transform: translateY(0);
}

.fade-leave-active {
  opacity: 1;
  transform: translateY(0);
}

.fade-leave-active.fade-leave-active {
  opacity: 0;
  transform: translateY(-20px);
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
