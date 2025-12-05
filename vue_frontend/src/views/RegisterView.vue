<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>
      
      <div class="input-group">
        <i class="fas fa-user"></i>
        <input 
          type="text" 
          v-model="registerForm.username" 
          placeholder="请输入用户名" 
          required
        >
      </div>
      
      <div class="input-group">
        <i class="fas fa-envelope"></i>
        <input 
          type="email" 
          v-model="registerForm.email" 
          placeholder="请输入邮箱" 
          required
        >
      </div>
      
      <div class="input-group">
        <i class="fas fa-lock"></i>
        <input 
          :type="showPassword ? 'text' : 'password'" 
          v-model="registerForm.password" 
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
      
      <div class="input-group">
        <i class="fas fa-lock"></i>
        <input 
          :type="showConfirmPassword ? 'text' : 'password'" 
          v-model="registerForm.confirmPassword" 
          placeholder="请确认密码" 
          required
        >
        <button 
          class="password-toggle" 
          @click="toggleConfirmPasswordVisibility"
        >
          <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
        </button>
      </div>
      
      <button class="register-button" @click="handleRegister" :disabled="isLoading">
        {{ isLoading ? '注册中...' : '注册' }}
      </button>
      
      <div class="login-link">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../utils/api'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const registerForm = ref({
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    })
    const showPassword = ref(false)
    const showConfirmPassword = ref(false)
    const isLoading = ref(false)
    
    const togglePasswordVisibility = () => {
      showPassword.value = !showPassword.value
    }
    
    const toggleConfirmPasswordVisibility = () => {
      showConfirmPassword.value = !showConfirmPassword.value
    }
    
    const handleRegister = async () => {
      if (registerForm.value.password !== registerForm.value.confirmPassword) {
        alert('两次输入的密码不一致')
        return
      }
      
      isLoading.value = true
      try {
        const response = await authApi.register({
          username: registerForm.value.username,
          email: registerForm.value.email,
          password: registerForm.value.password
        })
        console.log('注册成功:', response)
        alert('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
        alert(error.message || '注册失败，请重试')
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      registerForm,
      showPassword,
      showConfirmPassword,
      isLoading,
      togglePasswordVisibility,
      toggleConfirmPasswordVisibility,
      handleRegister
    }
  }
}
</script>

<style scoped>
/* 注册容器 */
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 注册卡片 */
.register-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 450px;
}

/* 注册标题 */
.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #2d3748;
  font-size: 24px;
  font-weight: 600;
}

/* 输入组 */
.input-group {
  position: relative;
  margin-bottom: 20px;
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

/* 注册按钮 */
.register-button {
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

.register-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.register-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 登录链接 */
.login-link {
  text-align: center;
  color: #718096;
  font-size: 14px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #764ba2;
}
</style>
