<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="auth-overlay" @click.self="$emit('close')">
        <div class="auth-card">
          <!-- Close button -->
          <button class="auth-close-btn" @click="$emit('close')">
            <i class="fas fa-times"></i>
          </button>

          <!-- Tab bar -->
          <div class="auth-tabs">
            <button
              class="auth-tab"
              :class="{ active: activeTab === 'login' }"
              @click="switchTab('login')"
            >
              登录
            </button>
            <span class="auth-tab-divider">|</span>
            <button
              class="auth-tab"
              :class="{ active: activeTab === 'register' }"
              @click="switchTab('register')"
            >
              注册
            </button>
          </div>

          <!-- Login form -->
          <div v-if="activeTab === 'login'" class="auth-form">
            <div class="auth-input-group">
              <i class="fas fa-user auth-input-icon"></i>
              <input
                type="text"
                class="auth-input"
                :class="{ 'auth-input-error': loginErrors.username }"
                v-model="loginForm.username"
                placeholder="请输入用户名"
                @input="clearError('login', 'username')"
                @keyup.enter="handleLogin"
              />
              <span v-if="loginErrors.username" class="auth-field-error">{{
                loginErrors.username
              }}</span>
            </div>

            <div class="auth-input-group">
              <i class="fas fa-lock auth-input-icon"></i>
              <input
                :type="showLoginPwd ? 'text' : 'password'"
                class="auth-input"
                :class="{ 'auth-input-error': loginErrors.password }"
                v-model="loginForm.password"
                placeholder="请输入密码"
                @input="clearError('login', 'password')"
                @keyup.enter="handleLogin"
              />
              <button
                class="auth-pwd-toggle"
                @click="showLoginPwd = !showLoginPwd"
                type="button"
              >
                <i
                  :class="showLoginPwd ? 'fas fa-eye-slash' : 'fas fa-eye'"
                ></i>
              </button>
              <span v-if="loginErrors.password" class="auth-field-error">{{
                loginErrors.password
              }}</span>
            </div>

            <div v-if="loginErrors.form" class="auth-form-error">
              {{ loginErrors.form }}
            </div>
            <div v-if="loginSuccess" class="auth-form-success">
              {{ loginSuccess }}
            </div>

            <button
              class="auth-submit-btn"
              @click="handleLogin"
              :disabled="loginLoading"
            >
              {{ loginLoading ? "登录中..." : "登录" }}
            </button>
          </div>

          <!-- Register form -->
          <div v-if="activeTab === 'register'" class="auth-form">
            <div class="auth-input-group">
              <i class="fas fa-user auth-input-icon"></i>
              <input
                type="text"
                class="auth-input"
                :class="{ 'auth-input-error': regErrors.username }"
                v-model="regForm.username"
                placeholder="请输入用户名"
                @input="clearError('reg', 'username')"
              />
              <span v-if="regErrors.username" class="auth-field-error">{{
                regErrors.username
              }}</span>
            </div>

            <div class="auth-input-group">
              <i class="fas fa-lock auth-input-icon"></i>
              <input
                :type="showRegPwd ? 'text' : 'password'"
                class="auth-input"
                :class="{ 'auth-input-error': regErrors.password }"
                v-model="regForm.password"
                placeholder="请输入密码"
                @input="clearError('reg', 'password')"
              />
              <button
                class="auth-pwd-toggle"
                @click="showRegPwd = !showRegPwd"
                type="button"
              >
                <i :class="showRegPwd ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
              <span v-if="regErrors.password" class="auth-field-error">{{
                regErrors.password
              }}</span>
            </div>

            <div class="auth-input-group">
              <i class="fas fa-lock auth-input-icon"></i>
              <input
                :type="showRegConfirmPwd ? 'text' : 'password'"
                class="auth-input"
                :class="{ 'auth-input-error': regErrors.confirmPassword }"
                v-model="regForm.confirmPassword"
                placeholder="请确认密码"
                @input="clearError('reg', 'confirmPassword')"
                @keyup.enter="handleRegister"
              />
              <button
                class="auth-pwd-toggle"
                @click="showRegConfirmPwd = !showRegConfirmPwd"
                type="button"
              >
                <i
                  :class="showRegConfirmPwd ? 'fas fa-eye-slash' : 'fas fa-eye'"
                ></i>
              </button>
              <span v-if="regErrors.confirmPassword" class="auth-field-error">{{
                regErrors.confirmPassword
              }}</span>
            </div>

            <div v-if="regErrors.form" class="auth-form-error">
              {{ regErrors.form }}
            </div>
            <div v-if="regSuccess" class="auth-form-success">
              {{ regSuccess }}
            </div>

            <button
              class="auth-submit-btn"
              @click="handleRegister"
              :disabled="regLoading"
            >
              {{ regLoading ? "注册中..." : "注册" }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, inject } from "vue";
import { authApi } from "../utils/api";

const props = defineProps({
  visible: Boolean,
  initialTab: {
    type: String,
    default: "login",
  },
});

const emit = defineEmits(["close"]);

const updateLoginStatus = inject("updateLoginStatus");

const activeTab = ref(props.initialTab);

watch(
  () => props.initialTab,
  (val) => {
    activeTab.value = val;
  },
);

watch(
  () => props.visible,
  (val) => {
    if (val) {
      activeTab.value = props.initialTab;
      resetForms();
    }
  },
);

const switchTab = (tab) => {
  activeTab.value = tab;
};

// ── Login state ──────────────────────────────────────────────
const loginForm = ref({ username: "", password: "" });
const loginErrors = ref({});
const loginSuccess = ref("");
const loginLoading = ref(false);
const showLoginPwd = ref(false);

// ── Register state ───────────────────────────────────────────
const regForm = ref({ username: "", password: "", confirmPassword: "" });
const regErrors = ref({});
const regSuccess = ref("");
const regLoading = ref(false);
const showRegPwd = ref(false);
const showRegConfirmPwd = ref(false);

const resetForms = () => {
  loginForm.value = { username: "", password: "" };
  loginErrors.value = {};
  loginSuccess.value = "";
  loginLoading.value = false;
  showLoginPwd.value = false;

  regForm.value = { username: "", password: "", confirmPassword: "" };
  regErrors.value = {};
  regSuccess.value = "";
  regLoading.value = false;
  showRegPwd.value = false;
  showRegConfirmPwd.value = false;
};

const clearError = (form, field) => {
  if (form === "login") {
    if (loginErrors.value[field]) delete loginErrors.value[field];
  } else {
    if (regErrors.value[field]) delete regErrors.value[field];
  }
};

const handleLogin = async () => {
  loginErrors.value = {};
  loginSuccess.value = "";

  if (!loginForm.value.username.trim()) {
    loginErrors.value.username = "用户名不能为空";
    return;
  }
  if (!loginForm.value.password) {
    loginErrors.value.password = "密码不能为空";
    return;
  }

  loginLoading.value = true;
  try {
    await authApi.login(loginForm.value.username, loginForm.value.password);
    const token = localStorage.getItem("auth_token");
    if (token) {
      updateLoginStatus(true);
      emit("close");
    } else {
      loginErrors.value.form = "登录成功但令牌未正确记录，请重试";
    }
  } catch (error) {
    loginErrors.value.form =
      error.statusCode === 401
        ? "用户名或密码错误"
        : error.message || "登录失败，请重试";
  } finally {
    loginLoading.value = false;
  }
};

const handleRegister = async () => {
  regErrors.value = {};
  regSuccess.value = "";

  if (!regForm.value.username.trim()) {
    regErrors.value.username = "用户名不能为空";
    return;
  }
  if (!regForm.value.password) {
    regErrors.value.password = "密码不能为空";
    return;
  }
  if (!regForm.value.confirmPassword) {
    regErrors.value.confirmPassword = "请确认密码";
    return;
  }
  if (regForm.value.password !== regForm.value.confirmPassword) {
    regErrors.value.confirmPassword = "两次输入的密码不一致";
    return;
  }

  regLoading.value = true;
  const username = regForm.value.username;
  const password = regForm.value.password;

  try {
    await authApi.register({ username, password });
    regSuccess.value = "注册成功，正在登录...";

    try {
      await authApi.login(username, password);
      const token = localStorage.getItem("auth_token");
      if (token) {
        updateLoginStatus(true);
        setTimeout(() => emit("close"), 600);
      }
    } catch {
      regSuccess.value = "注册成功！请手动登录。";
      setTimeout(() => {
        switchTab("login");
        loginForm.value.username = username;
      }, 1500);
    }
  } catch (error) {
    regErrors.value.form =
      error.statusCode === 409
        ? "用户名已存在"
        : error.message || "注册失败，请重试";
  } finally {
    regLoading.value = false;
  }
};
</script>

<style scoped>
/* Overlay */
.auth-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

/* Card */
.auth-card {
  position: relative;
  background: var(--card-background-color);
  border-radius: 14px;
  padding: 36px 40px 32px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

/* Close button */
.auth-close-btn {
  position: absolute;
  top: 14px;
  right: 16px;
  background: none;
  border: none;
  font-size: 18px;
  color: var(--light-text-color);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.2s;
}

.auth-close-btn:hover {
  color: var(--text-color);
}

/* Tabs */
.auth-tabs {
  display: flex;
  align-items: center;
  gap: 0;
  margin-bottom: 28px;
}

.auth-tab {
  background: none;
  border: none;
  font-size: 18px;
  font-weight: 500;
  color: var(--light-text-color);
  cursor: pointer;
  padding: 0 12px 8px;
  border-bottom: 2px solid transparent;
  transition:
    color 0.2s,
    border-color 0.2s;
}

.auth-tab.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

.auth-tab-divider {
  color: var(--light-text-color);
  font-size: 16px;
  opacity: 0.5;
}

/* Form */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Input group */
.auth-input-group {
  position: relative;
  margin-bottom: 20px;
}

.auth-input-icon {
  position: absolute;
  left: 14px;
  top: 13px;
  color: var(--light-text-color);
  font-size: 15px;
  pointer-events: none;
}

.auth-input {
  width: 100%;
  padding: 11px 42px 11px 42px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  color: var(--text-color);
  background: var(--card-background-color);
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
  box-sizing: border-box;
}

.auth-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.18);
}

.auth-input.auth-input-error {
  border-color: #e53e3e;
}

.auth-field-error {
  display: block;
  font-size: 12px;
  color: #e53e3e;
  margin-top: 4px;
  margin-left: 42px;
}

/* Password toggle */
.auth-pwd-toggle {
  position: absolute;
  right: 12px;
  top: 10px;
  background: none;
  border: none;
  color: var(--light-text-color);
  cursor: pointer;
  font-size: 15px;
  padding: 2px 4px;
  transition: color 0.2s;
}

.auth-pwd-toggle:hover {
  color: var(--primary-color);
}

/* Alerts */
.auth-form-error {
  font-size: 13px;
  color: #e53e3e;
  background: rgba(229, 62, 62, 0.08);
  border-radius: 6px;
  padding: 9px 12px;
  margin-bottom: 14px;
  text-align: center;
}

.auth-form-success {
  font-size: 13px;
  color: #38a169;
  background: rgba(56, 161, 105, 0.1);
  border-radius: 6px;
  padding: 9px 12px;
  margin-bottom: 14px;
  text-align: center;
}

/* Submit button */
.auth-submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition:
    opacity 0.2s,
    transform 0.2s,
    box-shadow 0.2s;
  margin-top: 4px;
}

.auth-submit-btn:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
}

.auth-submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

/* Transition */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.22s ease;
}

.modal-fade-enter-active .auth-card,
.modal-fade-leave-active .auth-card {
  transition:
    transform 0.22s ease,
    opacity 0.22s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .auth-card {
  transform: scale(0.94) translateY(-12px);
  opacity: 0;
}

.modal-fade-leave-to .auth-card {
  transform: scale(0.94) translateY(-8px);
  opacity: 0;
}
</style>

<!-- Dark mode overrides (non-scoped so html.dark selector works) -->
<style>
html.dark .auth-card {
  background-color: #1e1e1e !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5) !important;
}

html.dark .auth-input {
  background-color: #2d2d2d !important;
  border-color: #444 !important;
  color: #e0e0e0 !important;
}

html.dark .auth-input:focus {
  border-color: #7c93ee !important;
  box-shadow: 0 0 0 3px rgba(124, 147, 238, 0.2) !important;
}

html.dark .auth-input::placeholder {
  color: #666 !important;
}

html.dark .auth-input.auth-input-error {
  border-color: #e53e3e !important;
}
</style>
