<template>
  <div id="app">
    <NavBar
      :user="user"
      @show-login="showLogin = true"
      @logout="handleLogout"
      @search="handleSearch"
    />
    <main class="main-content">
      <router-view
        :search-keyword="searchKeyword"
        :user="user"
        @show-login="showLogin = true"
        @update-user="fetchUserInfo"
      />
    </main>
    <LoginModal
      v-if="showLogin"
      @close="showLogin = false"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'
import NavBar from './components/NavBar.vue'
import LoginModal from './components/LoginModal.vue'
import request from './api'

const showLogin = ref(false)
const user = ref(null)
const searchKeyword = ref('')

// 尝试从 localStorage 恢复用户状态
const savedUser = localStorage.getItem('user')
if (savedUser) {
  try { user.value = JSON.parse(savedUser) } catch { /* ignore */ }
}

onMounted(async () => {
  if (localStorage.getItem('token')) {
    try {
      const res = await request.get('/user/info')
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      user.value = null
    }
  }
})

function handleLoginSuccess(userData) {
  user.value = userData
  showLogin.value = false
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  user.value = null
}

async function fetchUserInfo() {
  try {
    const res = await request.get('/user/info')
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  } catch { /* ignore */ }
}

function handleSearch(keyword) {
  searchKeyword.value = keyword
}

provide('user', user)
provide('showLogin', () => { showLogin.value = true })
</script>

<style scoped>
.main-content {
  min-height: calc(100vh - 60px);
}
</style>
