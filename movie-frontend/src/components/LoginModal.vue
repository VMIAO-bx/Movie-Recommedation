<template>
  <el-dialog
    v-model="visible"
    :title="activeTab === 'login' ? '欢迎回来' : '创建账号'"
    width="420px"
    :close-on-click-modal="false"
    align-center
    @close="$emit('close')"
  >
    <!-- Tab switcher -->
    <div class="tab-switch">
      <button
        :class="['tab-btn', { active: activeTab === 'login' }]"
        @click="activeTab = 'login'"
      >
        登录
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'register' }]"
        @click="activeTab = 'register'"
      >
        注册
      </button>
    </div>

    <!-- Login form -->
    <form v-if="activeTab === 'login'" class="auth-form" @submit.prevent="handleLogin">
      <el-input
        v-model="loginForm.username"
        placeholder="用户名"
        size="large"
        :prefix-icon="User"
        clearable
      />
      <el-input
        v-model="loginForm.password"
        type="password"
        placeholder="密码"
        size="large"
        :prefix-icon="Lock"
        show-password
      />
      <el-button
        type="primary"
        size="large"
        native-type="submit"
        :loading="loading"
        round
        class="submit-btn"
      >
        登录
      </el-button>
    </form>

    <!-- Register form -->
    <form v-else class="auth-form" @submit.prevent="handleRegister">
      <el-input
        v-model="registerForm.username"
        placeholder="用户名"
        size="large"
        :prefix-icon="User"
        clearable
      />
      <el-input
        v-model="registerForm.nickname"
        placeholder="昵称"
        size="large"
        :prefix-icon="UserFilled"
        clearable
      />
      <el-input
        v-model="registerForm.password"
        type="password"
        placeholder="密码"
        size="large"
        :prefix-icon="Lock"
        show-password
      />
      <el-input
        v-model="registerForm.confirmPassword"
        type="password"
        placeholder="确认密码"
        size="large"
        :prefix-icon="Lock"
        show-password
      />
      <el-button
        type="primary"
        size="large"
        native-type="submit"
        :loading="loading"
        round
        class="submit-btn"
      >
        注册
      </el-button>
    </form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const emit = defineEmits(['close', 'login-success'])

const visible = ref(true)
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/user/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    ElMessage.success('登录成功')
    emit('login-success', res.data.user)
    visible.value = false
  } catch { /* 拦截器已处理 */ }
  finally { loading.value = false }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.password || !registerForm.nickname) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/user/register', {
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname
    })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    ElMessage.success('注册成功')
    emit('login-success', res.data.user)
    visible.value = false
  } catch { /* 拦截器已处理 */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.tab-switch {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  background: var(--el-fill-color);
  border-radius: 10px;
  padding: 4px;
}
.tab-btn {
  flex: 1;
  padding: 10px 0;
  border-radius: 8px;
  background: transparent;
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
  cursor: pointer;
}
.tab-btn.active {
  background: var(--el-bg-color-overlay);
  color: var(--el-text-color-primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.submit-btn {
  margin-top: 4px;
  width: 100%;
  font-weight: 600;
  letter-spacing: 0.04em;
}
</style>
