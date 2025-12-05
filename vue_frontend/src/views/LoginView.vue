<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>

      <div class="input-group">
        <i class="fas fa-user"></i>
        <input
          type="text"
          v-model="loginForm.username"
          placeholder="请输入用户名"
          required
        >
      </div>

      <div class="input-group">
        <i class="fas fa-lock"></i>
        <input
          :type="showPassword ? 'text' : 'password'"
          v-model="loginForm.password"
          placeholder="请输入密码"
          required
        >
        <button
          class="password-toggle"
          @click="togglePasswordVisibility"
        >
          <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
        </button>
      </div>

      <button class="login-button" @click="handleLogin" :disabled="isLoading">
        {{ isLoading ? '登录中...' : '登录' }}
      </button>

      <div class="register-link">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../utils/api'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const loginForm = ref({
      username: '',
      password: ''
    })
    const showPassword = ref(false)
    const isLoading = ref(false)

    const togglePasswordVisibility = () => {
      showPassword.value = !showPassword.value
    }

    const handleLogin = async () => {
      isLoading.value = true
      try {
        const response = await authApi.login(
          loginForm.value.username,
          loginForm.value.password
        )
        console.log('登录成功:', response)
        // 登录成功后跳转到首页（这里暂时跳转到注册页作为演示）
        router.push('/register')
      } catch (error) {
        console.error('登录失败:', error)
        alert(error.message || '登录失败，请重试')
      } finally {
        isLoading.value = false
      }
    }

    return {
      loginForm,
      showPassword,
      isLoading,
      togglePasswordVisibility,
      handleLogin
    }
  }
}
</script>

<style scoped>
/* 登录容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 登录卡片 */
.login-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 450px;
}

/* 登录标题 */
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #2d3748;
  font-size: 24px;
  font-weight: 600;
}

/* 输入组 */
.input-group {
  position: relative;
  margin-bottom: 25px;
}

/* 输入框 */
.input-group input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  color: #2d3748;
  outline: none;
  transition: all 0.3s ease;
}

/* 输入框聚焦 */
.input-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

/* 输入框图标 */
.input-group i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #718096;
  font-size: 18px;
}

/* 密码切换按钮 */
.password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: #667eea;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.login-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 注册链接 */
.register-link {
  text-align: center;
  color: #718096;
  font-size: 14px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #764ba2;
}
</style>
