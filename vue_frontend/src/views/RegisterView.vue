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
					v-model="registerForm.password"
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

			<div class="input-group">
				<i class="fas fa-lock"></i>
				<input
					:type="showConfirmPassword ? 'text' : 'password'"
					v-model="registerForm.confirmPassword"
					placeholder="请确认密码"
					@blur="validateConfirmPassword"
					@input="clearError('confirmPassword')"
					:class="{ 'input-error': errors.confirmPassword }"
				>
				<button
					class="password-toggle"
					@click="toggleConfirmPasswordVisibility"
				>
					<i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
				</button>
				<div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
			</div>

			<div v-if="errors.form" class="form-error">{{ errors.form }}</div>
			<div v-if="successMessage" class="success-message">{{ successMessage }}</div>

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
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {authApi} from '../utils/api'

export default {
	name: 'RegisterView',
	setup() {
		const router = useRouter()
		const registerForm = ref({
			username: '',
			password: '',
			confirmPassword: ''
		})
		const showPassword = ref(false)
		const showConfirmPassword = ref(false)
		const isLoading = ref(false)
		const errors = ref({})
		const successMessage = ref('')

		const togglePasswordVisibility = () => {
			showPassword.value = !showPassword.value
		}

		const toggleConfirmPasswordVisibility = () => {
			showConfirmPassword.value = !showConfirmPassword.value
		}

		// 验证用户名
		const validateUsername = () => {
			if (!registerForm.value.username.trim()) {
				errors.value.username = '用户名不能为空'
				return false
			}
			return true
		}


		// 验证密码
		const validatePassword = () => {
			if (!registerForm.value.password) {
				errors.value.password = '密码不能为空'
				return false
			}
			return true
		}

		// 验证确认密码
		const validateConfirmPassword = () => {
			if (!registerForm.value.confirmPassword) {
				errors.value.confirmPassword = '请确认密码'
				return false
			}
			if (registerForm.value.password !== registerForm.value.confirmPassword) {
				errors.value.confirmPassword = '两次输入的密码不一致'
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
			if (!validateConfirmPassword()) isValid = false

			return isValid
		}

		// 清除特定字段的错误信息
		const clearError = (field) => {
			if (errors.value[field]) {
				delete errors.value[field]
			}
		}

		const handleRegister = async () => {
			// 验证表单
			if (!validateForm()) {
				return
			}

			isLoading.value = true
			errors.value.form = ''
			successMessage.value = ''

			try {
				const response = await authApi.register({
					username: registerForm.value.username,
					password: registerForm.value.password
				})
				console.log('注册成功:', response)
				successMessage.value = '注册成功，正在跳转到登录页...'

				// 清空表单
				registerForm.value = {
					username: '',
					password: '',
					confirmPassword: ''
				}

				// 3秒后跳转到登录页
				setTimeout(() => {
					router.push('/login')
				}, 3000)
			} catch (error) {
				console.error('注册失败:', error)
				// 显示友好的错误信息
				if (error.statusCode === 409) {
					errors.value.form = '用户名或邮箱已存在'
				} else {
					errors.value.form = error.message || '注册失败，请重试'
				}
			} finally {
				isLoading.value = false
			}
		}

		return {
			registerForm,
			showPassword,
			showConfirmPassword,
			isLoading,
			errors,
			successMessage,
			togglePasswordVisibility,
			toggleConfirmPasswordVisibility,
			handleRegister,
			validateUsername,
			validatePassword,
			validateConfirmPassword,
			clearError
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
	background-color: #203060;
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
	position: relative;
	overflow: hidden;
}

.register-button::after {
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

.register-button:hover::after {
	width: 300px;
	height: 300px;
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

/* 错误输入框 */
.input-group input.input-error {
	border-color: #e53e3e;
	box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.2);
}

/* 错误信息 */
.error-message {
	color: #e53e3e;
	font-size: 12px;
	margin-top: 4px;
	margin-left: 45px;
}

/* 表单级错误 */
.form-error {
	color: #e53e3e;
	text-align: center;
	margin-bottom: 16px;
	padding: 10px;
	background-color: #fed7d7;
	border-radius: 4px;
}

/* 成功信息 */
.success-message {
	color: #38a169;
	text-align: center;
	margin-bottom: 16px;
	padding: 10px;
	background-color: #c6f6d5;
	border-radius: 4px;
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

/* 响应式设计 */
@media (max-width: 768px) {
	.register-container {
		padding: 15px;
	}

	.register-card {
		padding: 30px 20px;
		max-width: 100%;
	}

	.register-title {
		font-size: 22px;
		margin-bottom: 25px;
	}
}

/* 针对小屏幕手机优化 */
@media (max-width: 480px) {
	.register-container {
		padding: 10px;
	}

	.register-card {
		padding: 20px 15px;
	}

	.register-title {
		font-size: 20px;
		margin-bottom: 20px;
	}

	.input-group {
		margin-bottom: 16px;
	}

	.input-group input {
		padding: 10px 12px 10px 40px;
		font-size: 14px;
	}

	.input-group i {
		left: 12px;
		font-size: 16px;
	}

	.password-toggle {
		right: 12px;
		font-size: 16px;
	}

	.register-button {
		padding: 10px;
		font-size: 15px;
	}

	.login-link {
		font-size: 13px;
	}

	.error-message {
		font-size: 11px;
		margin-left: 40px;
	}

	.form-error, .success-message {
		font-size: 13px;
		padding: 8px;
		margin-bottom: 12px;
	}
}
</style>
