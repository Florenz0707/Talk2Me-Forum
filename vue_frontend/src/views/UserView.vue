<template>
  <div class="user-view-container" :class="{ 'bg-gradient-complete': bgGradientComplete, 'bg-fade-leave-active': isLeaving }">
    <!-- 头像展示栏 -->
    <div class="avatar-header">
      <div class="avatar-header-content">
        <div class="user-avatar-large">
          <i class="fas fa-user-circle"></i>
        </div>
        <div class="user-info">
          <div class="username-large">{{ username }}</div>
          <div class="user-bio">{{ editBio }}</div>
        </div>
      </div>
    </div>

    <!-- 顶部导航栏 -->
    <div class="bili-header-bar">
      <div class="container">
        <div class="left-entry">
        </div>

        <div class="right-entry">
          <ul class="flex">
            <li class="nav-item">
              <template v-if="!isLoggedIn">
                <router-link to="/login" class="nav-link">登录</router-link>
              </template>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 数据指标行 -->
    <div class="stats-container">
      <div class="stats-wrapper">
        <div class="stat-item">
          <div class="stat-number">128</div>
          <div class="stat-label">获赞数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">184</div>
          <div class="stat-label">关注数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">15</div>
          <div class="stat-label">粉丝数</div>
        </div>
      </div>
    </div>

    <!-- 主内容容器 -->
    <div class="main-content-container">
      <!-- 主内容区域 -->
      <div class="main-content-wrapper">
        <!-- 左侧导航栏 -->
        <div class="left-sidebar">
          <div class="nav-menu">
            <router-link to="/" class="nav-item">
              <i class="fas fa-home"></i>
              <span>回到主页</span>
            </router-link>
            <div
              class="nav-item"
              :class="{ 'active': activeNavItem === 'messages' }"
              @click="activeNavItem = 'messages'"
            >
              <i class="fas fa-envelope"></i>
              <span>新消息</span>
            </div>
            <div
              class="nav-item"
              :class="{ 'active': activeNavItem === 'favorites' }"
              @click="activeNavItem = 'favorites'"
            >
              <i class="fas fa-star"></i>
              <span>收藏</span>
            </div>
            <div
              class="nav-item"
              :class="{ 'active': activeNavItem === 'profile' }"
              @click="activeNavItem = 'profile'"
            >
              <i class="fas fa-user"></i>
              <span>个人资料</span>
            </div>
            <div
              class="nav-item"
              :class="{ 'active': activeNavItem === 'settings' }"
              @click="activeNavItem = 'settings'"
            >
              <i class="fas fa-cog"></i>
              <span>设置</span>
            </div>
          </div>
        </div>

        <!-- 右侧内容区域 -->
        <div class="right-content">
          <!-- 动态内容区域 -->
          <div class="content-card">
            <!-- 新消息内容 -->
            <div v-if="activeNavItem === 'messages'">
              <h2>新消息</h2>
              <div class="messages-list">
                <div class="message-item">
                  <div class="message-avatar">
                    <i class="fas fa-user"></i>
                  </div>
                  <div class="message-content">
                    <div class="message-sender">用户1</div>
                    <div class="message-text">你好，最近怎么样？</div>
                    <div class="message-time">10分钟前</div>
                  </div>
                </div>
                <div class="message-item">
                  <div class="message-avatar">
                    <i class="fas fa-user"></i>
                  </div>
                  <div class="message-content">
                    <div class="message-sender">用户2</div>
                    <div class="message-text">谢谢你的点赞！</div>
                    <div class="message-time">1小时前</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 收藏内容 -->
            <div v-else-if="activeNavItem === 'favorites'">
              <h2>默认收藏夹</h2>
              <div class="thread-table-container">
                <table class="thread-table">
                  <thead>
                    <tr class="thread-header">
                      <th class="thread-info">帖子</th>
                      <th class="thread-author">作者</th>
                      <th class="thread-time">时间</th>
                      <th class="thread-replies">回复</th>
                      <th class="thread-views">浏览</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="thread in favoriteThreads" :key="thread.id" class="thread-item">
                      <td class="thread-info">
                        <div class="thread-title">
                          <router-link :to="'/thread/' + thread.id" :title="thread.title">{{ thread.title }}</router-link>
                        </div>
                      </td>
                      <td class="thread-author">
                        <span class="author-name">{{ thread.author }}</span>
                      </td>
                      <td class="thread-time">{{ formatTime(thread.createdAt) }}</td>
                      <td class="thread-replies">{{ thread.replies }}</td>
                      <td class="thread-views">{{ thread.views }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- 个人资料内容 -->
            <div v-else-if="activeNavItem === 'profile'">
              <h2>个人资料</h2>
              <div class="profile-form">
                <div class="form-group">
                  <label>用户名</label>
                  <input type="text" v-model="editUsername" class="form-input">
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input type="email" value="user@example.com" disabled class="form-input">
                </div>
                <div class="form-group">
                  <label>个性签名</label>
                  <textarea v-model="editBio" class="form-textarea"></textarea>
                </div>
                <button class="save-btn" @click="handleSaveProfile">保存修改</button>
              </div>
            </div>

            <!-- 设置内容 -->
            <div v-else-if="activeNavItem === 'settings'">
              <h2>设置</h2>
              <div class="settings-list">
                <div class="setting-item">
                  <span>通知设置</span>
                  <label class="switch">
                    <input type="checkbox" checked>
                    <span class="slider"></span>
                  </label>
                </div>
                <div class="setting-item">
                  <span>隐私设置</span>
                  <label class="switch">
                    <input type="checkbox" checked>
                    <span class="slider"></span>
                  </label>
                </div>
                <div class="setting-item">
                  <span>深色模式</span>
                  <label class="switch">
                    <input type="checkbox">
                    <span class="slider"></span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
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
    const isLoggedIn = inject('isLoggedIn')
    const username = ref('')
    const isLoggingOut = ref(false)
    const bgGradientComplete = ref(false)
    const isLeaving = ref(false)

    // 导航栏选中项
    const activeNavItem = ref('favorites')

    // 搜索功能
    const searchQuery = ref('')
    const showUserMenu = ref(false)

    // 收藏的帖子数据
    const favoriteThreads = ref([
      {
        id: 1,
        title: '如何提高Vue项目的性能',
        author: '技术专家',
        createdAt: '2024-01-15T10:30:00',
        replies: 23,
        views: 156,
        isHot: true,
        isRecommended: false
      },
      {
        id: 2,
        title: 'TypeScript入门教程',
        author: '编程爱好者',
        createdAt: '2024-01-10T14:20:00',
        replies: 15,
        views: 98,
        isHot: false,
        isRecommended: true
      },
      {
        id: 3,
        title: '前端工程化最佳实践',
        author: '架构师',
        createdAt: '2024-01-05T09:15:00',
        replies: 31,
        views: 210,
        isHot: true,
        isRecommended: true
      }
    ])

    // 格式化时间函数
    const formatTime = (timeString) => {
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // 编辑个人资料
    const editUsername = ref('')
    const editBio = ref('')

    // 保存个人资料
    const handleSaveProfile = async () => {
      try {
        console.log('保存个人资料:', {
          username: editUsername.value,
          bio: editBio.value
        })

        // 预留向后端传输修改结果的接口位置
        // 这里应该调用API更新用户资料
        // await userApi.updateProfile({ username: editUsername.value, bio: editBio.value })

        // 假设更新成功，更新本地用户名
        username.value = editUsername.value

        // 显示保存成功提示
        alert('个人资料保存成功')
      } catch (error) {
        console.error('保存个人资料失败:', error)
        // 显示保存失败提示
        alert('保存失败，请稍后重试')
      }
    }

    // 获取当前登录用户信息
    const fetchUserInfo = async () => {
      try {
        // 这里可以调用获取用户信息的API
        // 暂时使用localStorage中的用户名
        const rememberedUsername = localStorage.getItem('rememberedUsername')
        if (rememberedUsername) {
          username.value = rememberedUsername
          editUsername.value = rememberedUsername
        } else {
          // 如果没有记住用户名，可以从登录响应中获取
          // 或者使用一个默认值
          username.value = '用户'
          editUsername.value = '用户'
        }

        // 初始化个性签名
        editBio.value = '这是一个个性签名，展示用户的个人简介'
      } catch (error) {
        console.error('获取用户信息失败:', error)
        username.value = '用户'
        editUsername.value = '用户'
        editBio.value = '这是一个个性签名，展示用户的个人简介'
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

    // 处理搜索
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        console.log('搜索内容:', searchQuery.value)
        // 这里可以添加搜索逻辑，例如调用API或过滤本地数据
        // 暂时只打印搜索内容
      }
    }

    return {
      username,
      isLoggingOut,
      handleLogout,
      bgGradientComplete,
      isLeaving,
      activeNavItem,
      isLoggedIn,
      searchQuery,
      showUserMenu,
      handleSearch,
      favoriteThreads,
      formatTime,
      editUsername,
      editBio,
      handleSaveProfile
    }
  }
}
</script>

<style scoped>
/* 用户页面容器 */
.user-view-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
  transition: background-color 0.3s ease-in-out;
}

/* 头像展示栏 */
.avatar-header {
  background-color: #2c3e50;
  height: 20vh;
  min-height: 150px;
  display: flex;
  align-items: center;
  color: white;
}

.avatar-header-content {
  max-width: 1500px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 30px;
}

.user-avatar-large {
  font-size: 120px;
  color: white;
  margin-left: -20px;
}

.user-info {
  flex: 1;
}

.username-large {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}


.user-bio {
  font-size: 16px;
  color: #e0e0e0;
  line-height: 1.5;
}

/* 背景渐变完成状态 */
.user-view-container.bg-gradient-complete {
  background-color: #f5f7fa;
}

/* 背景离开时的渐变动画 */
.user-view-container.bg-fade-leave-active {
  transition: background-color 0.3s ease-in-out;
}

/* 顶部导航栏 - 与HomeView一致 */
.bili-header-bar {
  background-color: #2c3e50;
  z-index: 1000;
  height: 75px;
  box-sizing: border-box;
}

/* 论坛logo样式 */
.forum-logo {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin-right: 40px;
  cursor: pointer;
  transition: none !important;
}

.forum-logo:hover {
  color: var(--quinary-color);
}

.bili-header-bar .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 20px;
}

.left-entry, .right-entry {
  flex: 1;
}

.left-entry {
  display: flex;
  justify-content: flex-start;
}

.right-entry {
  display: flex;
  justify-content: flex-end;
}

.flex {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin-right: 15px;
}

/* 右侧元素紧凑排列 */
.right-entry .flex {
  align-items: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: white;
  font-size: 16px;
  font-weight: 500;
  padding: 8px 0;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #e0e0e0;
}

.nav-link.active {
  color: #e0e0e0;
  font-weight: 600;
}

/* 搜索栏样式 */
.center-search-container {
  flex: 2;
  display: flex;
  justify-content: center;
}

.offset-center-search {
  margin-left: -50px;
}

.nav-search-content {
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  width: 100%;
  max-width: 1000px;
}

.nav-search-input {
  flex: 1;
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  font-size: 16px;
  outline: none;
}

.nav-search-btn {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  color: #666666;
  cursor: pointer;
  transition: color 0.3s ease;
  font-size: 16px;
}

.nav-search-btn:hover {
  color: var(--primary-color);
}

/* 用户头像容器 */
.user-avatar-container {
  position: relative;
  display: inline-block;
}

/* 用户头像样式 */
.user-avatar {
  font-size: 30px;
  display: block;
}

/* 用户下拉菜单 */
.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e0e0e0;
  min-width: 180px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 1000;
}

/* 下拉菜单显示状态 */
.user-dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

/* 下拉菜单项 */
.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
}

.dropdown-item i {
  margin-right: 10px;
  color: #666;
  font-size: 16px;
}

.dropdown-item span {
  color: #333;
  font-size: 14px;
}

/* 数据指标行 */
.stats-container {
  width: 100%;
  margin-top: 20px;
  padding: 0 20px;
  box-sizing: border-box;
}

.stats-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 主内容区域包装器 */
.main-content-container {
  width: 100%;
  margin-top: 20px;
  padding: 0 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
}

.main-content-wrapper {
  width: 80%;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  gap: 0;
}

/* 左侧导航栏 */
.left-sidebar {
  width: 200px;
  flex-shrink: 0;
  background-color: #f5f7fa;
}

.nav-menu {
  background-color: #f5f7fa;
  overflow: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
  margin-right: 0;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  background-color: #e3f2fd;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
}

.nav-item i {
  font-size: 16px;
}

.nav-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 确保router-link作为菜单项时的样式 */
.nav-item.router-link-active {
  background-color: #e3f2fd;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
}

.nav-item.router-link-active i {
  color: var(--primary-color);
}

/* 确保router-link的文本颜色与其他菜单项一致 */
.nav-item {
  color: #333;
  text-decoration: none;
}

.nav-item i {
  color: #666;
}

.nav-item:hover i {
  color: #333;
}

/* 右侧内容区域 */
.right-content {
  flex: 1;
  min-width: 0;
  background-color: #f5f7fa;
  padding: 30px;
}

/* 内容卡片 */
.content-card {
  background-color: #f5f7fa;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
}

.content-card h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

/* 粉丝列表 */
.fan-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 粉丝项 */
.fan-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.fan-item:hover {
  background-color: #f5f5f5;
}

/* 粉丝头像 */
.fan-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  color: #666;
  flex-shrink: 0;
}

/* 粉丝信息 */
.fan-info {
  flex: 1;
  min-width: 0;
}

.fan-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.fan-id {
  font-size: 12px;
  color: #999;
}

/* 回关按钮 */
.follow-btn {
  padding: 6px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.follow-btn:hover {
  background-color: #2980b9;
}

/* 消息列表 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.message-item:hover {
  background-color: #f5f5f5;
}

/* 消息头像 */
.message-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  color: #666;
  flex-shrink: 0;
}

/* 消息内容 */
.message-content {
  flex: 1;
  min-width: 0;
}

.message-sender {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.message-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  color: #999;
}

/* 个人资料表单 */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 表单组 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 表单输入框 */
.form-input,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  box-sizing: border-box;
  background-color: #f9f9f9;
  transition: border-color 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.form-textarea {
  height: 120px;
  resize: vertical;
  min-height: 80px;
}

/* 保存按钮 */
.save-btn {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  align-self: flex-start;
}

.save-btn:hover {
  background-color: #2980b9;
}

/* 设置列表 */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 设置项 */
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.setting-item:hover {
  background-color: #f5f5f5;
}

.setting-item span {
  font-size: 14px;
  color: #333;
}

/* 开关按钮 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #3498db;
}

input:focus + .slider {
  box-shadow: 0 0 1px #3498db;
}

input:checked + .slider:before {
  transform: translateX(24px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content-wrapper {
    flex-direction: column;
    padding: 10px;
  }

  .left-sidebar {
    width: 100%;
  }

  .nav-menu {
    display: flex;
    overflow-x: auto;
    border-radius: 8px;
  }

  .nav-item {
    flex-shrink: 0;
    border-left: none;
    border-bottom: 3px solid transparent;
  }

  .nav-item.active {
    border-left: none;
    border-bottom-color: var(--primary-color);
  }

  .content-card {
    padding: 20px;
  }

  .fan-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .fan-info {
    width: 100%;
  }

  .follow-btn {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .top-nav-bar .container {
    padding: 0 10px;
  }

  .top-nav-bar .left-section .logo {
    font-size: 16px;
  }

  .top-nav-bar .user-avatar {
    font-size: 20px;
  }

  .top-nav-bar .username {
    font-size: 12px;
  }

  .content-card {
    padding: 15px;
  }

  .content-card h2 {
    font-size: 18px;
  }

  .fan-item {
    padding: 10px;
  }

  .fan-avatar {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

/* 帖子表格样式 */
.thread-table-container {
  margin-top: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
}

.thread-table {
  width: 100%;
  border-collapse: collapse;
}

.thread-header {
  background-color: #f0f0f0;
}

.thread-header th {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #e0e0e0;
}

.thread-item {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.thread-item:hover {
  background-color: #f5f5f5;
}

.thread-item td {
  padding: 16px;
  font-size: 14px;
  color: #333;
}

.thread-info {
  min-width: 400px;
}

.thread-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.thread-title a {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.thread-title a:hover {
  color: #3498db;
}

.thread-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.thread-tag.hot {
  background-color: #e74c3c;
  color: white;
}

.thread-tag.recommended {
  background-color: #27ae60;
  color: white;
}

.thread-author {
  width: 120px;
}

.author-name {
  color: #666;
  font-size: 14px;
}

.thread-time {
  width: 150px;
  color: #999;
  font-size: 14px;
}

.thread-replies,
.thread-views {
  width: 80px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

/* 响应式设计 - 帖子表格 */
@media (max-width: 768px) {
  .thread-info {
    min-width: 200px;
  }

  .thread-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .thread-author,
  .thread-time,
  .thread-replies,
  .thread-views {
    width: auto;
    font-size: 12px;
  }

  .thread-item td {
    padding: 10px;
  }
}
</style>
