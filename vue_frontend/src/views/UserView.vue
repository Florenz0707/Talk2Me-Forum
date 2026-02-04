<template>
  <div class="home-container" :class="{ 'bg-gradient-complete': bgGradientComplete, 'bg-fade-leave-active': isLeaving }">
    <div class="home-card" :class="{ 'card-popup-active': showCardPopup, 'card-popup-leave-active': isLeaving }">
      <div class="header-section">
        <button class="back-home-button" @click="goToHome">
          <i class="fas fa-home"></i>
          <span>返回主页</span>
        </button>
      </div>
      <h2 class="home-title">欢迎回来，{{ username }}</h2>
      <p class="home-subtitle">这是您的个人主页</p>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-envelope"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">新消息</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-user-friends"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">新粉丝</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-heart"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">获赞数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-bookmark"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">收藏数</div>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <h3 class="actions-title">快速操作</h3>
        <div class="actions-grid">
          <button class="action-button" @click="navigateToCreateThread">
            <i class="fas fa-pen"></i>
            <span>发布动态</span>
          </button>

          <button class="action-button">
            <i class="fas fa-search"></i>
            <span>搜索内容</span>
          </button>

          <button class="action-button">
            <i class="fas fa-cog"></i>
            <span>设置</span>
          </button>

          <button class="action-button">
            <i class="fas fa-user"></i>
            <span>个人资料</span>
          </button>
        </div>
      </div>

      <button class="logout-button" @click="handleLogout" :disabled="isLoggingOut">
        {{ isLoggingOut ? '退出中...' : '退出登录' }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../utils/api'

export default {
  name: 'UserView',
  setup() {
    const router = useRouter()
    const updateLoginStatus = inject('updateLoginStatus')
    const username = ref('')
    const isLoggingOut = ref(false)
    const bgGradientComplete = ref(false)
    const showCardPopup = ref(false)
    const isLeaving = ref(false)

    // 获取当前登录用户信息
    const fetchUserInfo = async () => {
      try {
        // 这里可以调用获取用户信息的API
        // 暂时使用localStorage中的用户名
        const rememberedUsername = localStorage.getItem('rememberedUsername')
        if (rememberedUsername) {
          username.value = rememberedUsername
        } else {
          // 如果没有记住用户名，可以从登录响应中获取
          // 或者使用一个默认值
          username.value = '用户'
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        username.value = '用户'
      }
    }

    // 退出登录
    const handleLogout = async () => {
      isLoggingOut.value = true
      try {
        // 调用退出登录API
        await authApi.logout()
        console.log('退出登录成功')

        // 清除本地存储的用户信息和Token
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('auth_token')

        // 更新全局登录状态
        updateLoginStatus(false)

        // 触发离开动画后跳转到登录页面
        triggerLeaveAnimation(() => {
          router.push('/login')
        })
      } catch (error) {
        console.error('退出登录失败:', error)
        // 即使API调用失败，仍然清除本地存储并跳转到登录页面
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('auth_token')

        // 更新全局登录状态
        updateLoginStatus(false)

        // 触发离开动画后跳转到登录页面
        triggerLeaveAnimation(() => {
          router.push('/login')
        })
      } finally {
        isLoggingOut.value = false
      }
    }

    // 页面挂载时获取用户信息
    onMounted(() => {
      fetchUserInfo()

      // 触发背景渐变动画
      setTimeout(() => {
        bgGradientComplete.value = true

        // 背景渐变完成后触发卡片弹出动画
        setTimeout(() => {
          showCardPopup.value = true
        }, 300) // 0.3秒背景渐变时间
      }, 100) // 给DOM一点时间渲染
    })

    // 返回主页
    const goToHome = () => {
      triggerLeaveAnimation(() => {
        router.push('/home')
      })
    }

    // 导航到发布动态页面
    const navigateToCreateThread = () => {
      triggerLeaveAnimation(() => {
        router.push('/create-thread')
      })
    }

    // 触发离开动画
    const triggerLeaveAnimation = (callback) => {
      isLeaving.value = true

      // 卡片弹出动画完成后（0.3秒），启动背景渐变动画
      setTimeout(() => {
        bgGradientComplete.value = false

        // 背景渐变动画完成后（0.3秒），执行导航
        setTimeout(() => {
          callback()
        }, 300) // 0.3秒背景渐变时间
      }, 300) // 0.3秒卡片弹出动画时间
    }

    return {
      username,
      isLoggingOut,
      handleLogout,
      goToHome,
      navigateToCreateThread,
      bgGradientComplete,
      showCardPopup,
      isLeaving
    }
  }
}
</script>

<style scoped>
/* 主页容器 */
.home-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #FFFFFF;
  padding: 20px;
  position: relative;
  transition: background-color 0.3s ease-in-out;
}

/* 背景渐变完成状态 */
.home-container.bg-gradient-complete {
  background-color: var(--sapphire-blue);
}

/* 主页卡片 */
.home-card {
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 800px;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* 卡片弹出动画激活状态 */
.home-card.card-popup-active {
  opacity: 1;
  transform: scale(1);
}

/* 卡片离开时的弹出动画 */
.home-card.card-popup-leave-active {
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* 背景离开时的渐变动画 */
.home-container.bg-fade-leave-active {
  transition: background-color 0.3s ease-in-out;
}



/* 主页标题 */
.home-title {
  text-align: center;
  margin-bottom: 10px;
  color: var(--text-color);
  font-size: 28px;
  font-weight: 600;
}

/* 主页副标题 */
.header-section {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

/* 返回主页按钮 */
.back-home-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--quinary-color) 100%);
  border: 2px solid var(--primary-color);
  border-radius: 8px;
  color: var(--senary-color);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-home-button i {
  color: var(--primary-color);
}

.back-home-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.back-home-button:active {
  transform: translateY(0);
}

.home-subtitle {
  text-align: center;
  margin-bottom: 30px;
  color: var(--light-text-color);
  font-size: 16px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

/* 统计卡片 */
.stat-card {
  background: var(--quinary-color);
  border: 2px solid var(--primary-color);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

/* 统计卡片悬停效果 */
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 统计图标 */
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--quinary-color);
  font-size: 20px;
}

/* 统计内容 */
.stat-content {
  flex: 1;
}

/* 统计数字 */
.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: var(--senary-color);
  margin-bottom: 4px;
}

/* 统计标签 */
.stat-label {
  font-size: 12px;
  color: var(--senary-color);
}

/* 快速操作区域 */
.quick-actions {
  margin-bottom: 30px;
}

/* 操作标题 */
.actions-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 16px;
  text-align: center;
}

/* 操作按钮网格 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

/* 操作按钮 */
.action-button {
  background: var(--quinary-color);
  border: 2px solid var(--primary-color);
  border-radius: 8px;
  color: var(--senary-color);
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
}

/* 操作按钮悬停效果 */
.action-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 操作按钮图标 */
.action-button i {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--quinary-color);
  font-size: 20px;
}

/* 退出登录按钮 */
.logout-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, var(--tertiary-color) 0%, var(--error-color) 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 退出登录按钮悬停效果 */
.logout-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.logout-button:hover::after {
  width: 300px;
  height: 300px;
}

.logout-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.4);
}

.logout-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 20px;
  }

  .home-card {
    padding: 20px;
  }

  .home-title {
    font-size: 24px;
    margin-bottom: 15px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 针对小屏幕手机优化 */
@media (max-width: 480px) {
  .home-container {
    padding: 15px;
  }

  .home-card {
    padding: 15px;
  }

  .home-title {
    font-size: 20px;
    margin-bottom: 10px;
  }

  .home-subtitle {
    font-size: 14px;
    margin-bottom: 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 15px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  .stat-number {
    font-size: 20px;
  }

  .actions-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .action-button {
    padding: 12px;
    font-size: 13px;
  }

  .action-button i {
    font-size: 20px;
  }

  .logout-button {
    padding: 10px;
    font-size: 14px;
  }
}
</style>
