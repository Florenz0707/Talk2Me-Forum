<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 全局登录状态
const isLoggedIn = ref(false)

// 检查用户登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('auth_token')
  isLoggedIn.value = token !== null && token !== ''
}

// 更新登录状态
const updateLoginStatus = (status) => {
  isLoggedIn.value = status
}

// 提供全局状态和方法
provide('isLoggedIn', isLoggedIn)
provide('checkLoginStatus', checkLoginStatus)
provide('updateLoginStatus', updateLoginStatus)

// 组件挂载时检查登录状态
onMounted(() => {
  checkLoginStatus()

  // 监听localStorage变化，实现多标签页同步
  window.addEventListener('storage', (event) => {
    if (event.key === 'auth_token') {
      checkLoginStatus()
    }
  })
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

body {
  background-color: var(--background-color);
}

#app {
  min-height: 100vh;
}
</style>
