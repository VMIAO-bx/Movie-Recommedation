<template>
  <div class="home">
    <!-- Hero -->
    <section v-if="!searchKeyword" class="hero">
      <div class="container">
        <h1 class="hero-title">发现你的下一部<br />心爱电影</h1>
        <p class="hero-sub">浏览热门影片，收藏你喜欢的，标记你想看的</p>
      </div>
    </section>

    <!-- Search results heading -->
    <section v-if="searchKeyword" class="search-heading container">
      <h2>搜索 "{{ searchKeyword }}" 的结果</h2>
      <span v-if="!loading && displayMovies.length" class="count">{{ displayMovies.length }} 部影片</span>
    </section>

    <!-- Content -->
    <section class="container">
      <!-- Loading -->
      <div v-if="loading" class="loading-center">
        <el-icon :size="36" class="is-loading"><Loading /></el-icon>
        <p style="margin-top:12px;color:var(--el-text-color-secondary)">正在加载电影数据...</p>
      </div>

      <!-- Network error with retry -->
      <div v-else-if="networkError" class="empty-state">
        <el-icon :size="48"><WarningFilled /></el-icon>
        <p style="margin-bottom:16px">无法连接到 TMDB 服务器</p>
        <p class="hint">请检查 VPN 连接，或使用本地数据</p>
        <div class="retry-actions">
          <el-button type="primary" :loading="loading" @click="retryLoad">
            重试
          </el-button>
          <el-button text @click="loadMockData">
            加载本地示例数据
          </el-button>
        </div>
      </div>

      <!-- Empty (no results) -->
      <div v-else-if="!movies.length" class="empty-state">
        <el-icon :size="48"><VideoCamera /></el-icon>
        <p v-if="searchKeyword">没有找到相关电影</p>
        <p v-else>暂无电影数据</p>
      </div>

      <!-- Grid -->
      <div v-else class="movie-grid">
        <MovieCard
          v-for="movie in displayMovies"
          :key="movie.id"
          :movie="movie"
          :is-favorited="favoriteIds.has(movie.id)"
          :watch-status="watchStatusMap.get(movie.id) || ''"
          @favorite="handleFavorite(movie)"
          @watch="handleWatch(movie, $event)"
          @detail="openDetail"
        />
      </div>

      <!-- TMDB notice -->
      <div v-if="!loading && movies.length" class="tmdb-notice">
        <p>{{ isMockData ? '当前为本地示例数据' : '数据来源：The Movie Database (TMDB)' }}</p>
      </div>
    </section>

    <!-- ── Detail modal ── -->
    <MovieDetail
      :visible="showDetail"
      :movie="detailMovie"
      :is-favorited="detailMovie ? favoriteIds.has(detailMovie.id) : false"
      :watch-status="detailMovie ? (watchStatusMap.get(detailMovie.id) || '') : ''"
      @close="showDetail = false"
      @favorite="movie => { handleFavorite(movie); showDetail = false }"
      @watch="({ movie, status }) => { handleWatch(movie, { status }); showDetail = false }"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, VideoCamera, WarningFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import request from '../api'
import MovieCard from '../components/MovieCard.vue'
import MovieDetail from '../components/MovieDetail.vue'

const props = defineProps({
  searchKeyword: { type: String, default: '' },
  user: { type: Object, default: null }
})
const emit = defineEmits(['show-login'])

const TMDB_KEY = import.meta.env.VITE_TMDB_API_KEY || '4014001a79aab6f7bb3ff811e3574e53'
const TMDB_IMG = 'https://image.tmdb.org/t/p/w500'

const movies = ref([])
// 裁剪为 6 的倍数，确保每行填满
const GRID_COLS = 6
const displayMovies = computed(() => {
  const list = movies.value
  if (list.length < GRID_COLS) return list
  const trim = list.length % GRID_COLS
  return trim === 0 ? list : list.slice(0, -trim)
})
const loading = ref(false)
const networkError = ref(false)
const isMockData = ref(false)
const detailMovie = ref(null)
const showDetail = ref(false)
const favoriteIds = ref(new Set())
const watchStatusMap = ref(new Map())

// Axios for TMDB (via Vite proxy)
const tmdb = axios.create({
  baseURL: '/tmdb/3',
  timeout: 25000
})

// ── Mock 模板（提取复用，36 部 = 6×6 满屏） ──
const ALL_MOCK = [
  { id: 278,    title: '肖申克的救赎',     release_date: '1994-09-23', vote_average: 9.7, poster_path: '/9gj6k7wN8qE3Tn4E4bB1n0pN0TK.jpg' },
  { id: 10998,  title: '霸王别姬',          release_date: '1993-01-01', vote_average: 9.6, poster_path: '/oIllrTOEoHq7n4r4Vx1QCsjRshs.jpg' },
  { id: 13,     title: '阿甘正传',          release_date: '1994-07-06', vote_average: 9.5, poster_path: '/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg' },
  { id: 597,    title: '泰坦尼克号',        release_date: '1997-12-19', vote_average: 9.4, poster_path: '/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg' },
  { id: 129,    title: '千与千寻',          release_date: '2001-07-20', vote_average: 9.4, poster_path: '/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg' },
  { id: 637,    title: '这个杀手不太冷',    release_date: '1994-09-14', vote_average: 9.4, poster_path: '/r4UsAneUYmCmJ76yvFMaCOv1ntn.jpg' },
  { id: 11023,  title: '忠犬八公的故事',    release_date: '2009-06-13', vote_average: 9.4, poster_path: '/mK4adBObGOlSs1q2RlMl1HmV2qL.jpg' },
  { id: 37165,  title: '楚门的世界',        release_date: '1998-06-05', vote_average: 9.3, poster_path: '/wTjHOHjV5A5NQ3xA7m4QhMq8O4d.jpg' },
  { id: 14337,  title: '海上钢琴师',        release_date: '1998-10-28', vote_average: 9.3, poster_path: '/y7LcLQyZgKqHfQrvE2d7dGfI3FZ.jpg' },
  { id: 20453,  title: '三傻大闹宝莱坞',    release_date: '2009-12-25', vote_average: 9.2, poster_path: '/r2NwVtMpJG9zDjyyDZCwZ0nXgGm.jpg' },
  { id: 127,    title: '放牛班的春天',      release_date: '2004-03-17', vote_average: 9.3, poster_path: '/7rVX9jJqYdX6UqKv0OQb9gPQDEg.jpg' },
  { id: 157336, title: '星际穿越',          release_date: '2014-11-12', vote_average: 9.4, poster_path: '/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg' },
  { id: 550,    title: '搏击俱乐部',        release_date: '1999-10-15', vote_average: 9.0, poster_path: '/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg' },
  { id: 238,    title: '教父',              release_date: '1972-03-24', vote_average: 9.2, poster_path: '/3bhkrj58Vtu7enYsRolD1fZdja1.jpg' },
  { id: 769,    title: '蝙蝠侠：黑暗骑士',  release_date: '2008-07-18', vote_average: 9.0, poster_path: '/qJ2tW6WMUDux911BytEMq8NkLc2.jpg' },
  { id: 155,    title: '盗梦空间',          release_date: '2010-07-16', vote_average: 8.8, poster_path: '/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg' },
  { id: 122,    title: '指环王：王者无敌',  release_date: '2003-12-17', vote_average: 8.8, poster_path: '/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg' },
  { id: 424,    title: '辛德勒的名单',      release_date: '1993-12-15', vote_average: 9.0, poster_path: '/sF1U4EUQS8YHUYjNl3pMGNIQyr0.jpg' },
  { id: 497,    title: '绿里奇迹',          release_date: '1999-12-10', vote_average: 8.5, poster_path: '/velWPhVMQeQKcxggNEU8YmIo52R.jpg' },
  { id: 680,    title: '低俗小说',          release_date: '1994-10-14', vote_average: 8.9, poster_path: '/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg' },
  { id: 311,    title: '美国往事',          release_date: '1984-02-17', vote_average: 8.5, poster_path: '/zTGSYKp2pPzCjOmwO8pKq2E0b8j.jpg' },
  { id: 539,    title: '惊魂记',            release_date: '1960-06-22', vote_average: 9.0, poster_path: '/nR4LD4ZJg2n6LZQpcOrZFshBQ8a.jpg' },
  { id: 240,    title: '教父2',             release_date: '1974-12-20', vote_average: 9.1, poster_path: '/bVq65HLhx0LWmJxmy1fJC5RQITj.jpg' },
  { id: 346,    title: '七武士',            release_date: '1954-04-26', vote_average: 9.2, poster_path: '/8OKmBV5BUFzmozIC3pjaKzC0FEk.jpg' },
  { id: 78,     title: '银翼杀手',          release_date: '1982-06-25', vote_average: 8.1, poster_path: '/63N9uy8nd9j7Eog2axPQ8lbr3Wj.jpg' },
  { id: 274,    title: '沉默的羔羊',        release_date: '1991-02-14', vote_average: 8.6, poster_path: '/uS9m8OBk1VVNYMA2e9DqFnrE6Ia.jpg' },
  { id: 539,    title: '飞越疯人院',        release_date: '1975-11-19', vote_average: 8.7, poster_path: '/2vAL9dQYaTN7QVgLrY8hJ6XK0js.jpg' },
  { id: 600,    title: '现代启示录',        release_date: '1979-08-15', vote_average: 8.3, poster_path: '/gQB8Y5RCMkv2zwzFHbUJXBMIIZx.jpg' },
  { id: 489,    title: '心灵捕手',          release_date: '1997-12-05', vote_average: 8.3, poster_path: '/bABCBKYBK7A5G1x0FzoeXJx5BhO.jpg' },
  { id: 694,    title: '闪灵',              release_date: '1980-05-23', vote_average: 8.4, poster_path: '/nRjCqCg1LuAYjw5ZIVJqPx3QhGq.jpg' },
  { id: 807,    title: '七宗罪',            release_date: '1995-09-22', vote_average: 8.3, poster_path: '/6yogHTyEahEtYeeX0qUj9eE5e4x.jpg' },
  { id: 603,    title: '黑客帝国',          release_date: '1999-03-30', vote_average: 8.2, poster_path: '/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg' },
  { id: 862,    title: '玩具总动员',        release_date: '1995-10-30', vote_average: 7.9, poster_path: '/uXDfjJbdP4ijW5hWSBrPrlKpxab.jpg' },
  { id: 11,     title: '星球大战',          release_date: '1977-05-25', vote_average: 8.2, poster_path: '/6FfCtAuVAW8XJjZ7eWeLibRLWTw.jpg' },
  { id: 98,     title: '角斗士',            release_date: '2000-05-01', vote_average: 8.2, poster_path: '/ty8TGRuvJLPUmAR1H1nRIsgwCLK.jpg' },
  { id: 500,    title: '落水狗',            release_date: '1992-09-02', vote_average: 8.2, poster_path: '/AjKbT1W1aVRdE1xVQOqiXhFxMh.jpg' },
]

// ── TMDB helpers ──
async function fetchPopular() {
  loading.value = true
  networkError.value = false
  isMockData.value = false
  try {
    const { data } = await tmdb.get('/movie/popular', {
      params: { api_key: TMDB_KEY, language: 'zh-CN', page: 1 }
    })
    movies.value = data.results || []
  } catch (e) {
    console.error('TMDB popular fetch failed:', e.message)
    movies.value = []
    networkError.value = true
  } finally {
    loading.value = false
  }
}

async function searchMovies(keyword) {
  if (!keyword) return
  loading.value = true
  networkError.value = false
  isMockData.value = false
  try {
    const { data } = await tmdb.get('/search/movie', {
      params: { api_key: TMDB_KEY, query: keyword, language: 'zh-CN' }
    })
    movies.value = data.results || []
    if (!movies.value.length) {
      ElMessage.info('TMDB 未找到结果，尝试本地匹配...')
      searchLocally(keyword)
    }
  } catch (e) {
    console.error('TMDB search failed:', e.message)
    ElMessage.warning('TMDB 搜索失败，使用本地数据匹配')
    searchLocally(keyword)
  } finally {
    loading.value = false
  }
}

function searchLocally(keyword) {
  isMockData.value = true
  const kw = keyword.toLowerCase()
  movies.value = ALL_MOCK.filter(m =>
    m.title.includes(keyword) || m.title.toLowerCase().includes(kw)
  )
}

// 降级 fallback
function loadMockData() {
  networkError.value = false
  isMockData.value = true
  movies.value = [...ALL_MOCK]
  ElMessage.success('已加载本地示例数据')
}

function retryLoad() {
  if (props.searchKeyword) {
    searchMovies(props.searchKeyword)
  } else {
    fetchPopular()
  }
}

// ── User status ──
async function loadUserStatus() {
  if (!props.user || !localStorage.getItem('token')) return
  try {
    const [favRes, watchRes] = await Promise.all([
      request.get('/favorite/list'),
      request.get('/watch/list')
    ])
    favoriteIds.value = new Set((favRes.data || []).map(m => m.movieId))
    const map = new Map()
    ;(watchRes.data || []).forEach(m => { map.set(m.movieId, m.status) })
    watchStatusMap.value = map
  } catch { /* ignore */ }
}

// ── Watchers ──
watch(() => props.searchKeyword, (kw) => {
  if (kw) {
    searchMovies(kw)
  } else {
    // 清空搜索 → 回到 TMDB 热门
    fetchPopular()
  }
})

watch(() => props.user, loadUserStatus)

onMounted(() => {
  fetchPopular()
  loadUserStatus()
})

// ── Actions (keep inline for reactivity) ──
function needLogin() {
  if (!props.user) {
    emit('show-login')
    return true
  }
  return false
}

function openDetail(movie) {
  detailMovie.value = movie
  showDetail.value = true
}

async function handleFavorite(movie) {
  if (needLogin()) return
  const id = movie.id
  if (favoriteIds.value.has(id)) {
    try {
      await request.post('/favorite/remove', { movieId: id })
      favoriteIds.value.delete(id)
      favoriteIds.value = new Set(favoriteIds.value)
      ElMessage.success('已取消收藏')
    } catch { /* */ }
  } else {
    try {
      await request.post('/favorite/add', {
        movieId: id,
        movieTitle: movie.title,
        moviePoster: movie.poster_path ? TMDB_IMG + movie.poster_path : ''
      })
      favoriteIds.value.add(id)
      favoriteIds.value = new Set(favoriteIds.value)
      ElMessage.success('已添加收藏')
    } catch { /* */ }
  }
}

async function handleWatch(movie, { status }) {
  if (needLogin()) return
  const id = movie.id
  const current = watchStatusMap.value.get(id)
  try {
    if (current === status) {
      // 再次点击相同状态 → 取消标记，调用 remove 接口从数据库删除
      await request.post('/watch/remove', { movieId: id })
      watchStatusMap.value.delete(id)
    } else if (current) {
      await request.put('/watch/update', { movieId: id,movieTitle: movie.title, status })
      watchStatusMap.value.set(id, status)
    } else {
      await request.post('/watch/add', {
        movieId: id,
        movieTitle: movie.title,
        moviePoster: movie.poster_path ? TMDB_IMG + movie.poster_path : '',
        status
      })
      watchStatusMap.value.set(id, status)
    }
    watchStatusMap.value = new Map(watchStatusMap.value)
    ElMessage.success(current === status ? '已取消标记' : `已标记为「${status}」`)
  } catch { /* */ }
}
</script>

<style scoped>
.hero {
  padding: 56px 0 32px;
  text-align: center;
}
.hero-title {
  font-family: var(--font-display);
  font-size: 2.6rem;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.02em;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}
.hero-sub {
  font-size: 1.05rem;
  color: var(--el-text-color-secondary);
  max-width: 480px;
  margin: 0 auto;
}

.search-heading {
  padding: 32px 0 8px;
  display: flex;
  align-items: baseline;
  gap: 14px;
}
.search-heading h2 {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 600;
}
.search-heading .count {
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(185px, 1fr));
  gap: 22px;
  padding: 28px 0;
}

.tmdb-notice {
  text-align: center;
  padding: 32px 0 48px;
  color: var(--el-text-color-placeholder);
  font-size: 0.8rem;
  opacity: 0.6;
}

.hint {
  color: var(--el-text-color-placeholder);
  font-size: 0.85rem;
  margin-bottom: 20px;
}
.retry-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 640px) {
  .hero { padding: 36px 0 24px; }
  .hero-title { font-size: 1.8rem; }
  .hero-sub { font-size: 0.95rem; }
  .movie-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 14px;
  }
}
</style>
