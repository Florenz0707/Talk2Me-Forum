<template>
  <div class="home-container">
    <div class="home-card">
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
          <button class="action-button">
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

        // 跳转到登录页面
        router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
        // 即使API调用失败，仍然清除本地存储并跳转到登录页面
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('auth_token')

        // 更新全局登录状态
        updateLoginStatus(false)

        router.push('/login')
      } finally {
        isLoggingOut.value = false
      }
    }

    // 页面挂载时获取用户信息
    onMounted(() => {
      fetchUserInfo()
    })

    return {
      username,
      isLoggingOut,
      handleLogout
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 主页卡片 */
.home-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 600px;
}

/* 主页标题 */
.home-title {
  text-align: center;
  margin-bottom: 10px;
  color: #2d3748;
  font-size: 28px;
  font-weight: 600;
}

/* 主页副标题 */
.home-subtitle {
  text-align: center;
  margin-bottom: 30px;
  color: #718096;
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
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
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
  box-shadow: 0 4px 12px rgba(31, 38, 135, 0.1);
}

/* 统计图标 */
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: #ffffff;
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
  color: #2d3748;
  margin-bottom: 4px;
}

/* 统计标签 */
.stat-label {
  font-size: 12px;
  color: #718096;
}

/* 快速操作区域 */
.quick-actions {
  margin-bottom: 30px;
}

/* 操作标题 */
.actions-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
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
  font-size: 24px;
}

/* 退出登录按钮 */
.logout-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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
    width: 90%;
    margin: 20px auto;
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
    width: 95%;
    padding: 15px;
    margin: 10px auto;
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
