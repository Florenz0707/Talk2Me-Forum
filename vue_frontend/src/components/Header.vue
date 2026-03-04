<template>
  <header class="page-header">
    <div class="container">
      <h1 class="page-title">{{ title }}</h1>
      <div class="user-actions">
        <router-link v-if="showAuthButtons" to="/login" class="btn btn-primary">登录</router-link>
        <router-link v-if="showAuthButtons" to="/register" class="btn btn-secondary">注册</router-link>
        <template v-else>
          <router-link v-if="showCreateThreadBtn" to="/create-thread" class="btn btn-primary">发帖</router-link>
          <router-link to="/user" class="btn btn-primary">用户中心</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, inject } from 'vue'
import { useRouter } from 'vue-router'

// 接收props
const props = defineProps({
  title: {
    type: String,
    default: '论坛首页'
  },
  showCreateThreadBtn: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()

// 从全局注入获取登录状态
const isLoggedIn = inject('isLoggedIn')

// 计算是否显示登录/注册按钮
const showAuthButtons = computed(() => !isLoggedIn.value)
</script>

<style scoped>
.page-header {
  background-color: var(--forum-dark-color);
  color: white;
  padding: var(--header-padding);
  box-shadow: var(--shadow-medium);
}

.page-header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: var(--container-max-width);
  margin: 0 auto;
  padding: var(--container-padding);
}

.page-title {
  margin: 0;
  font-size: var(--page-title-size);
  font-weight: 600;
}

.user-actions {
  display: flex;
  gap: var(--spacing-md);
}
</style>
