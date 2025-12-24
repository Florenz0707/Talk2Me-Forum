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
          @blur="validateUsername"
          @input="clearError('username')"
          :class="{ 'input-error': errors.username }"
        >
        <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
      </div>

      <div class="input-group">
        <i class="fas fa-lock"></i>
        <input
          :type="showPassword ? 'text' : 'password'"
          v-model="loginForm.password"
          placeholder="请输入密码"
          @blur="validatePassword"
          @input="clearError('password')"
          :class="{ 'input-error': errors.password }"
        >
        <button
          class="password-toggle"
          @click="togglePasswordVisibility"
        >
          <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
        </button>
        <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
      </div>

      <div v-if="errors.form" class="form-error">{{ errors.form }}</div>
      <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

      <div class="login-options">
        <div class="remember-me">
          <input type="checkbox" id="rememberMe" v-model="rememberMe">
          <label for="rememberMe">记住我</label>
        </div>
        <div class="forgot-password">
          <a href="#" @click.prevent="handleForgotPassword">忘记密码？</a>
        </div>
      </div>

      <button class="login-button" @click="handleLogin" :disabled="isLoading">
        {{ isLoading ? '登录中...' : '登录' }}
      </button>

      <button class="refresh-token-button" @click="handleRefreshToken" :disabled="isTokenRefreshing">
        {{ isTokenRefreshing ? '刷新中...' : '刷新Token' }}
      </button>

      <button class="verify-token-button" @click="handleVerifyToken" :disabled="isTokenVerifying">
        {{ isTokenVerifying ? '验证中...' : '验证Token' }}
      </button>

      <button class="guest-access-button" @click="handleGuestAccess">
        <i class="fas fa-user-secret"></i>
        <span>以游客身份进入</span>
      </button>

      <div class="register-link">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '../utils/api'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const updateLoginStatus = inject('updateLoginStatus')
    const loginForm = ref({
      username: '',
      password: ''
    })
    const showPassword = ref(false)
    const isLoading = ref(false)
    const isTokenRefreshing = ref(false)
    const isTokenVerifying = ref(false)
    const rememberMe = ref(false)
    const errors = ref({})
    const successMessage = ref('')

    const togglePasswordVisibility = () => {
      showPassword.value = !showPassword.value
    }

    // 验证用户名
    const validateUsername = () => {
      if (!loginForm.value.username.trim()) {
        errors.value.username = '用户名不能为空'
        return false
      }
      if (loginForm.value.username.length < 3) {
        errors.value.username = '用户名至少需要3个字符'
        return false
      }
      if (loginForm.value.username.length > 20) {
        errors.value.username = '用户名不能超过20个字符'
        return false
      }
      return true
    }

    // 验证密码
    const validatePassword = () => {
      if (!loginForm.value.password) {
        errors.value.password = '密码不能为空'
        return false
      }
      if (loginForm.value.password.length < 6) {
        errors.value.password = '密码至少需要6个字符'
        return false
      }
      return true
    }

    // 验证整个表单
    const validateForm = () => {
      let isValid = true
      errors.value = {}

      if (!validateUsername()) isValid = false
      if (!validatePassword()) isValid = false

      return isValid
    }

    // 清除特定字段的错误信息
    const clearError = (field) => {
      if (errors.value[field]) {
        delete errors.value[field]
      }
    }

    // 页面加载时检查是否有记住的用户名
    const initializeForm = () => {
      const rememberedUsername = localStorage.getItem('rememberedUsername')
      if (rememberedUsername) {
        loginForm.value.username = rememberedUsername
        rememberMe.value = true
      }
    }

    // 调用初始化函数
    initializeForm()

    const handleLogin = async () => {
      // 验证表单
      if (!validateForm()) {
        return
      }

      isLoading.value = true
      errors.value.form = ''
      successMessage.value = ''

      try {
        const response = await authApi.login(
          loginForm.value.username,
          loginForm.value.password
        )
        console.log('登录成功:', response)

        // 如果勾选了"记住我"，可以将相关信息存储在localStorage中
        if (rememberMe.value) {
          localStorage.setItem('rememberedUsername', loginForm.value.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }

        // 显示成功信息
        successMessage.value = '登录成功，正在跳转...'

        // 确认token已正确存储
        const token = localStorage.getItem('auth_token')
        if (token) {
          // 更新全局登录状态
          updateLoginStatus(true)

          // 获取重定向路径，如果没有则默认跳转到主页
          const redirectPath = route.query.redirect || '/home'
          // 跳转到重定向路径或主页
          router.push(redirectPath)
        } else {
          // 如果token未存储成功，显示错误信息
          errors.value.form = '登录成功但令牌未正确记录，请重试'
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 显示友好的错误信息
        if (error.statusCode === 401) {
          errors.value.form = '用户名或密码错误'
        } else {
          errors.value.form = error.message || '登录失败，请重试'
        }
      } finally {
        isLoading.value = false
      }
    }

    const handleForgotPassword = () => {
      // 忘记密码逻辑
      alert('忘记密码功能开发中...')
    }

    // 刷新Token功能
    const handleRefreshToken = async () => {
      isTokenRefreshing.value = true
      errors.value.form = ''
      successMessage.value = ''

      try {
        // 步骤1：刷新Token
        const refreshResponse = await authApi.refreshToken()
        console.log('Token刷新成功:', refreshResponse)

        // 显示成功信息
        successMessage.value = 'Token刷新成功'
      } catch (error) {
        console.error('Token刷新失败:', error)
        // 显示友好的错误信息，但对于没有刷新令牌的情况进行特殊处理
        if (error.errorCode === 'NO_REFRESH_TOKEN') {
          // 不直接显示"没有刷新令牌"，而是显示更友好的提示
          errors.value.form = '请先登录获取令牌后再尝试刷新'
        } else {
          errors.value.form = error.message || 'Token刷新失败，请检查网络或重新登录'
        }
      } finally {
        isTokenRefreshing.value = false
      }
    }

    // 验证Token功能
    const handleVerifyToken = async () => {
      isTokenVerifying.value = true
      errors.value.form = ''
      successMessage.value = ''

      try {
        // 调用验证接口
        const verifyResponse = await authApi.verifyAuth()
        console.log('Token验证成功:', verifyResponse)

        // 显示成功信息
        successMessage.value = 'Token验证成功，当前用户已认证'
      } catch (error) {
        console.error('Token验证失败:', error)
        // 显示友好的错误信息
        if (error.errorCode === 'NETWORK_ERROR') {
          errors.value.form = '网络错误，请检查您的网络连接'
        } else if (error.statusCode === 401) {
          errors.value.form = 'Token已过期或无效，请重新登录'
        } else {
          errors.value.form = error.message || 'Token验证失败'
        }
      } finally {
        isTokenVerifying.value = false
      }
    }

    // 以游客身份进入
    const handleGuestAccess = () => {
      // 更新全局登录状态为未登录
      updateLoginStatus(false)
      // 跳转至主页
      router.push('/home')
    }

    return {
      loginForm,
      showPassword,
      isLoading,
      isTokenRefreshing,
      isTokenVerifying,
      rememberMe,
      errors,
      successMessage,
      togglePasswordVisibility,
      handleLogin,
      handleForgotPassword,
      handleRefreshToken,
      handleVerifyToken,
      handleGuestAccess,
      validateUsername,
      validatePassword,
      clearError,
      initializeForm
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
  background-color: var(--sapphire-blue);
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
  color: var(--text-color);
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
  color: var(--text-color);
  outline: none;
  transition: all 0.3s ease;
}

/* 输入框聚焦 */
.input-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

/* 错误输入框 */
.input-group input.input-error {
  border-color: #e53e3e;
  box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.2);
}

/* 错误信息 */
.error-message {
  color: var(--error-color);
  font-size: 12px;
  margin-top: 4px;
  margin-left: 45px;
}

/* 表单级错误 */
.form-error {
  color: var(--error-color);
  text-align: center;
  margin-bottom: 16px;
  padding: 10px;
  background-color: rgba(var(--error-color), 0.1);
  border-radius: 4px;
}

/* 成功信息 */
.success-message {
  color: var(--success-color);
  text-align: center;
  margin-bottom: 16px;
  padding: 10px;
  background-color: rgba(var(--success-color), 0.1);
  border-radius: 4px;
}

/* 输入框图标 */
.input-group i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--light-text-color);
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
  color: var(--primary-color);
}

/* 登录按钮 */
.login-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

/* 刷新Token按钮 */
.refresh-token-button {
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
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

/* 验证Token按钮 */
.verify-token-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, var(--quaternary-color) 0%, var(--info-color) 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.verify-token-button::after {
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

.verify-token-button:hover::after {
  width: 300px;
  height: 300px;
}

.verify-token-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--quaternary-color), 0.4);
}

.verify-token-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.refresh-token-button::after {
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

.refresh-token-button:hover::after {
  width: 300px;
  height: 300px;
}

.refresh-token-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--tertiary-color), 0.4);
}

.refresh-token-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-button::after {
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

.login-button:hover::after {
  width: 300px;
  height: 300px;
}

.login-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--primary-color), 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 记住我选项 */
.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: var(--light-text-color);
}

.remember-me input {
  margin-right: 8px;
  cursor: pointer;
}

/* 忘记密码链接 */
.forgot-password a {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-password a:hover {
  color: var(--secondary-color);
  text-decoration: underline;
}

/* 注册链接 */
/* 以游客身份进入按钮 */
.guest-access-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, var(--quaternary-color) 0%, var(--info-color) 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.guest-access-button::after {
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

.guest-access-button:hover::after {
  width: 300px;
  height: 300px;
}

.guest-access-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--quaternary-color), 0.4);
}

.register-link {
  text-align: center;
  color: var(--light-text-color);
  font-size: 14px;
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: var(--secondary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    width: 90%;
    margin: 20px auto;
    padding: 20px;
  }

  .login-form {
    padding: 20px;
  }

  .login-title {
    font-size: 1.8rem;
    margin-bottom: 15px;
  }

  .login-options {
    flex-direction: column;
    align-items: flex-start;
  }

  .forgot-password {
    margin-top: 10px;
    margin-left: 0;
  }
}

/* 针对小屏幕手机优化 */
@media (max-width: 480px) {
  .login-container {
    width: 95%;
    padding: 15px;
    margin: 10px auto;
  }

  .login-form {
    padding: 15px;
  }

  .login-title {
    font-size: 1.5rem;
    margin-bottom: 15px;
  }

  .input-group {
    margin-bottom: 12px;
  }

  .input-group input {
    padding: 10px 15px 10px 45px;
    font-size: 0.9rem;
  }

  .password-toggle {
    font-size: 0.8rem;
  }

  .login-button,
  .refresh-token-button {
    padding: 10px;
    font-size: 0.9rem;
  }

  .register-link {
    font-size: 0.85rem;
  }

  .error-message, .success-message, .form-error {
    font-size: 0.85rem;
    padding: 8px;
  }
}
</style>
