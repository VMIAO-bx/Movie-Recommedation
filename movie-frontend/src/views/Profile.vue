<template>
  <div class="profile-page">
    <div class="container">
      <!-- User header -->
      <section class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="72" :src="editAvatar" class="avatar-lg" />
          <div class="avatar-edit" @click="triggerUpload">
            <el-icon :size="14"><Edit /></el-icon>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display:none"
            @change="onFileChange"
          />
        </div>
        <div class="user-meta">
          <h2 class="nickname">{{ user?.nickname || user?.username || '用户' }}</h2>
          <p class="username-tag">@{{ user?.username }}</p>
        </div>
        <el-button text size="small" class="edit-nickname-btn" @click="showNickEdit = true">
          编辑资料
        </el-button>
      </section>

      <!-- Nickname edit inline -->
      <div v-if="showNickEdit" class="nickname-edit-bar">
        <el-input
          v-model="newNickname"
          placeholder="新昵称"
          size="small"
          style="max-width:220px"
        />
        <el-button type="primary" size="small" :loading="savingNick" @click="saveNickname">
          保存
        </el-button>
        <el-button size="small" @click="showNickEdit = false">取消</el-button>
      </div>

      <!-- Tabs -->
      <section class="tabs-section">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="我的收藏" name="favorites">
            <div v-if="loading" class="loading-center">
              <el-icon :size="32" class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="!favorites.length" class="empty-state">
              <el-icon :size="44"><Star /></el-icon>
              <p>还没有收藏电影，去首页发现好片吧</p>
            </div>
            <div v-else class="movie-grid">
              <div v-for="item in favorites" :key="item.movieId" class="list-card" @click="">
                <img
                  v-if="item.moviePoster"
                  :src="item.moviePoster"
                  :alt="item.movieTitle"
                  class="list-poster"
                />
                <div v-else class="list-poster-placeholder">🎬</div>
                <div class="list-info">
                  <h4>{{ item.movieTitle }}</h4>
                </div>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="removeFavorite(item)"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="想看" name="want">
            <div v-if="loading" class="loading-center">
              <el-icon :size="32" class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="!wantList.length" class="empty-state">
              <el-icon :size="44"><Clock /></el-icon>
              <p>还没有"想看"的电影</p>
            </div>
            <div v-else class="movie-grid">
              <div v-for="item in wantList" :key="item.movieId" class="list-card">
                <img
                  v-if="item.moviePoster"
                  :src="item.moviePoster"
                  :alt="item.movieTitle"
                  class="list-poster"
                />
                <div v-else class="list-poster-placeholder">🎬</div>
                <div class="list-info">
                  <h4>{{ item.movieTitle }}</h4>
                  <span class="tag tag-want">想看</span>
                </div>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="removeWatch(item)"
                >
                  移除
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="看过" name="watched">
            <div v-if="loading" class="loading-center">
              <el-icon :size="32" class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="!watchedList.length" class="empty-state">
              <el-icon :size="44"><CircleCheck /></el-icon>
              <p>还没有"看过"的电影</p>
            </div>
            <div v-else class="movie-grid">
              <div v-for="item in watchedList" :key="item.movieId" class="list-card">
                <img
                  v-if="item.moviePoster"
                  :src="item.moviePoster"
                  :alt="item.movieTitle"
                  class="list-poster"
                />
                <div v-else class="list-poster-placeholder">🎬</div>
                <div class="list-info">
                  <h4>{{ item.movieTitle }}</h4>
                  <span class="tag tag-watched">看过</span>
                </div>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="removeWatch(item)"
                >
                  移除
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Loading, Edit, Star, Clock, CircleCheck
} from '@element-plus/icons-vue'
import request from '../api'
import MovieDetail from '../components/MovieDetail.vue'

const props = defineProps({
  user: { type: Object, default: null }
})
const emit = defineEmits(['show-login', 'update-user'])

const router = useRouter()

// Redirect if not logged in
onMounted(() => {
  if (!props.user && !localStorage.getItem('token')) {
    router.push('/')
    emit('show-login')
  }
})
watch(() => props.user, (val) => {
  if (!val && !localStorage.getItem('token')) {
    router.push('/')
    emit('show-login')
  }
})

// ── State ──
const activeTab = ref('favorites')
const loading = ref(false)
const allWatchList = ref([])
const allFavorites = ref([])

const favorites = computed(() => allFavorites.value)
const wantList = computed(() => allWatchList.value.filter(w => w.status === '想看'))
const watchedList = computed(() => allWatchList.value.filter(w => w.status === '看过'))

// ── Load data ──
async function loadData() {
  if (!localStorage.getItem('token')) return
  loading.value = true
  try {
    const [favRes, watchRes] = await Promise.all([
      request.get('/favorite/list'),
      request.get('/watch/list')
    ])
    allFavorites.value = favRes.data || []
    allWatchList.value = watchRes.data || []
  } catch { /* */ }
  finally { loading.value = false }
}

onMounted(loadData)
watch(() => props.user, loadData)

// ── Remove actions ──
async function removeFavorite(item) {
  try {
    await request.post('/favorite/remove', { movieId: item.movieId })
    allFavorites.value = allFavorites.value.filter(f => f.movieId !== item.movieId)
    ElMessage.success('已取消收藏')
  } catch { /* */ }
}

async function removeWatch(item) {
  try {
    await request.post('/watch/remove', { movieId: item.movieId })
    allWatchList.value = allWatchList.value.filter(w => w.movieId !== item.movieId)
    ElMessage.success('已移除')
  } catch { /* */ }
}

// ── Nickname edit ──
const showNickEdit = ref(false)
const newNickname = ref('')
const savingNick = ref(false)

async function saveNickname() {
  if (!newNickname.value.trim()) return
  savingNick.value = true
  try {
    await request.put('/user/info', { nickname: newNickname.value.trim() })
    props.user.nickname = newNickname.value.trim()
    localStorage.setItem('user', JSON.stringify(props.user))
    ElMessage.success('昵称已更新')
    showNickEdit.value = false
    emit('update-user')
  } catch { /* */ }
  finally { savingNick.value = false }
}

// ── Avatar upload ──
const fileInput = ref(null)
const editAvatar = ref(props.user?.avatar || '')

function triggerUpload() {
  fileInput.value?.click()
}

async function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  // 转为 base64（本地模拟头像上传）
  const reader = new FileReader()
  reader.onload = async (ev) => {
    const base64 = ev.target?.result
    editAvatar.value = base64
    try {
      await request.put('/user/info', { avatar: base64 })
      props.user.avatar = base64
      localStorage.setItem('user', JSON.stringify(props.user))
      ElMessage.success('头像已更新')
      emit('update-user')
    } catch { /* */ }
  }
  reader.readAsDataURL(file)
}
</script>

<style scoped>
.profile-page {
  padding: 36px 0 60px;
}

/* Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.avatar-section {
  position: relative;
}
.avatar-lg {
  border: 3px solid var(--el-border-color);
}
.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 26px;
  height: 26px;
  background: var(--el-color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s;
}
.avatar-edit:hover {
  transform: scale(1.15);
}
.user-meta {
  flex: 1;
}
.nickname {
  font-family: var(--font-display);
  font-size: 1.6rem;
  font-weight: 700;
}
.username-tag {
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
  margin-top: 2px;
}
.edit-nickname-btn {
  align-self: flex-start;
}

/* Nickname edit bar */
.nickname-edit-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 14px 18px;
  background: var(--el-fill-color);
  border-radius: var(--el-border-radius-base);
  border: 1px solid var(--el-border-color);
}

/* Tabs */
.profile-tabs {
  --el-tabs-header-height: 44px;
}

/* List cards */
.list-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--el-border-radius-base);
  transition: border-color 0.2s, background 0.2s;
  margin-bottom: 10px;
}
.list-card:hover {
  border-color: var(--el-border-color);
  background: var(--el-fill-color-light);
}
.list-poster {
  width: 48px;
  height: 72px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}
.list-poster-placeholder {
  width: 48px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1c1c28;
  border-radius: 4px;
  flex-shrink: 0;
  font-size: 1.2rem;
}
.list-info {
  flex: 1;
  min-width: 0;
}
.list-info h4 {
  font-size: 0.95rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tag {
  display: inline-block;
  font-size: 0.72rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  margin-top: 4px;
}
.tag-want {
  background: rgba(232, 184, 75, 0.15);
  color: var(--color-gold);
}
.tag-watched {
  background: rgba(77, 171, 122, 0.15);
  color: var(--color-success);
}

.movie-grid {
  display: block;
}

@media (max-width: 640px) {
  .profile-header { flex-wrap: wrap; }
  .nickname { font-size: 1.3rem; }
}
</style>
