<template>
  <header class="navbar">
    <div class="navbar-inner container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <span class="logo-mark">◈</span>
        <span class="logo-text">MovieRec</span>
      </router-link>

      <!-- Search with autocomplete -->
      <div class="search-wrap">
        <el-autocomplete
          ref="autocompleteRef"
          v-model="keyword"
          :fetch-suggestions="fetchSuggestions"
          :trigger-on-focus="false"
          :highlight-first-item="true"
          :debounce="350"
          placeholder="搜索电影..."
          clearable
          value-key="title"
          @select="onSelect"
          @keyup.enter="onEnter"
          @clear="onClear"
        >
          <template #prefix>
            <el-icon class="el-input__icon"><Search /></el-icon>
          </template>
          <template #default="{ item }">
            <div class="suggestion-item">
              <img
                v-if="item.poster_path"
                :src="IMG_BASE + item.poster_path"
                class="suggestion-poster"
              />
              <div v-else class="suggestion-no-poster">🎬</div>
              <div class="suggestion-info">
                <span class="suggestion-title">{{ item.title }}</span>
                <span class="suggestion-year">{{ item.release_date?.slice(0, 4) || '' }}</span>
              </div>
            </div>
          </template>
        </el-autocomplete>
      </div>

      <!-- Theme toggle -->
      <button class="theme-toggle" :title="isDark ? '切换亮色模式' : '切换暗色模式'" @click="toggleTheme">
        {{ isDark ? '☀️' : '🌙' }}
      </button>

      <!-- Right side -->
      <div class="nav-actions">
        <template v-if="user">
          <router-link to="/profile" class="user-badge">
            <el-avatar :size="32" :src="user.avatar" />
            <span class="username">{{ user.nickname || user.username }}</span>
          </router-link>
          <el-button text size="small" @click="handleLogout">退出</el-button>
        </template>
        <el-button v-else type="primary" size="small" round @click="$emit('show-login')">
          登录
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

defineProps({ user: { type: Object, default: null } })
const emit = defineEmits(['show-login', 'logout', 'search'])

const TMDB_KEY = import.meta.env.VITE_TMDB_API_KEY || '4014001a79aab6f7bb3ff811e3574e53'
const IMG_BASE = 'https://image.tmdb.org/t/p/w92'

const keyword = ref('')
const autocompleteRef = ref(null)
const isDark = ref(true)

// Theme
function applyTheme(dark) {
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
  localStorage.setItem('theme', dark ? 'dark' : 'light')
  isDark.value = dark
}

function toggleTheme() {
  applyTheme(!isDark.value)
}

onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved === 'light') {
    applyTheme(false)
  } else {
    applyTheme(true)
  }
})

const tmdbSuggest = axios.create({
  baseURL: '/tmdb/3',
  timeout: 10000
})

// ── Autocomplete suggestion fetcher ──
async function fetchSuggestions(queryString, cb) {
  if (!queryString || queryString.trim().length < 1) {
    return cb([])
  }
  try {
    const { data } = await tmdbSuggest.get('/search/movie', {
      params: {
        api_key: TMDB_KEY,
        query: queryString.trim(),
        language: 'zh-CN',
        page: 1
      }
    })
    cb((data.results || []).slice(0, 6))
  } catch {
    cb([])
  }
}

// ── Events ──
function onSelect(item) {
  emit('search', item.title)
  // 选中后收起下拉
  autocompleteRef.value?.close()
}

function onEnter() {
  const val = keyword.value.trim()
  if (val) {
    emit('search', val)
    autocompleteRef.value?.close()
  }
}

function onClear() {
  keyword.value = ''
  emit('search', '')
}

function handleLogout() {
  emit('logout')
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 200;
  height: 64px;
  background: var(--navbar-glass);
  backdrop-filter: saturate(180%) blur(16px);
  -webkit-backdrop-filter: saturate(180%) blur(16px);
  border-bottom: 1px solid var(--el-border-color);
}

.navbar-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  text-decoration: none;
}
.logo-mark {
  font-size: 1.6rem;
  color: var(--el-color-primary);
  line-height: 1;
}
.logo-text {
  font-family: var(--font-display);
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
  letter-spacing: -0.02em;
}

/* Search */
.search-wrap {
  flex: 1;
  max-width: 440px;
}

/* ---- Override el-autocomplete internals ---- */
.search-wrap :deep(.el-input__wrapper) {
  border-radius: 24px;
  background: var(--el-fill-color);
  border-color: var(--el-border-color);
  padding: 0 16px;
}
.search-wrap :deep(.el-input__wrapper:hover) {
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-dark);
}
.search-wrap :deep(.el-input__wrapper.is-focus) {
  background: var(--el-fill-color-light);
  border-color: var(--el-color-primary);
}
.search-wrap :deep(.el-input__inner) {
  font-size: 0.9rem;
}

/* ---- Suggestion dropdown ---- */
.search-wrap :deep(.el-autocomplete__suggestions) {
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  box-shadow: var(--el-box-shadow);
  margin-top: 6px;
  max-height: 420px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}
.suggestion-poster {
  width: 36px;
  height: 54px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}
.suggestion-no-poster {
  width: 36px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1c1c28;
  border-radius: 4px;
  flex-shrink: 0;
  font-size: 1rem;
}
.suggestion-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.suggestion-title {
  font-size: 0.9rem;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.suggestion-year {
  font-size: 0.78rem;
  color: var(--el-text-color-placeholder);
}

/* Hover highlight on suggestion items */
.search-wrap :deep(.el-autocomplete-suggestion__list li:hover) {
  background: var(--el-fill-color-light);
}

/* User area */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 20px;
  text-decoration: none;
  color: var(--el-text-color-primary);
  transition: background 0.2s;
}
.user-badge:hover {
  background: var(--el-fill-color);
}
.username {
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 640px) {
  .logo-text { display: none; }
  .search-wrap { max-width: 200px; }
  .username { display: none; }
}
</style>
