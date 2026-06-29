<template>
  <Teleport to="body">
    <Transition name="detail">
      <div v-if="visible" class="detail-overlay" @click.self="$emit('close')">
        <!-- Glass card -->
        <div class="detail-card">
          <button class="detail-close" @click="$emit('close')">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>

          <!-- Loading skeleton -->
          <template v-if="loading">
            <div class="detail-skeleton">
              <div class="skeleton-poster" />
              <div class="skeleton-info">
                <div class="skeleton-line w60" />
                <div class="skeleton-line w40" />
                <div class="skeleton-line w80" />
                <div class="skeleton-line w100" />
                <div class="skeleton-line w100" />
              </div>
            </div>
          </template>

          <!-- Content -->
          <template v-else-if="detail">
            <div class="detail-layout">
              <!-- Left: Poster -->
              <div class="detail-poster-col">
                <div class="detail-poster-wrap">
                  <img
                    v-if="posterUrl"
                    :src="posterUrl"
                    :alt="detail.title"
                    class="detail-poster"
                  />
                  <div v-else class="detail-no-poster">{{ detail.title?.slice(0, 4) }}</div>
                </div>
              </div>

              <!-- Right: Info -->
              <div class="detail-info-col">
                <h2 class="detail-title">{{ detail.title }}</h2>
                <p v-if="detail.tagline" class="detail-tagline">{{ detail.tagline }}</p>

                <div class="detail-meta">
                  <span v-if="detail.release_date" class="meta-item">
                    {{ detail.release_date.slice(0, 4) }}
                  </span>
                  <span v-if="detail.runtime" class="meta-item">
                    {{ Math.floor(detail.runtime / 60) }}h {{ detail.runtime % 60 }}m
                  </span>
                  <span v-if="detail.genres?.length" class="meta-item">
                    {{ detail.genres.map(g => g.name).join(' / ') }}
                  </span>
                </div>

                <!-- Rating bar -->
                <div v-if="detail.vote_average" class="detail-rating-row">
                  <div class="rating-bar-bg">
                    <div
                      class="rating-bar-fill"
                      :style="{ width: (detail.vote_average * 10) + '%' }"
                    />
                  </div>
                  <span class="rating-number">{{ detail.vote_average.toFixed(1) }}</span>
                  <span class="rating-total">/ 10</span>
                </div>

                <!-- Overview -->
                <div class="detail-overview">
                  <h4>剧情简介</h4>
                  <p>{{ detail.overview || '暂无简介' }}</p>
                </div>

                <!-- Actions -->
                <div class="detail-actions">
                  <button
                    :class="['detail-action-btn', { active: isFavorited }]"
                    @click.stop="$emit('favorite', movie)"
                  >
                    {{ isFavorited ? '♥ 已收藏' : '♡ 收藏' }}
                  </button>
                  <button
                    :class="['detail-action-btn', { active: watchStatus === '想看' }]"
                    @click.stop="$emit('watch', { movie, status: '想看' })"
                  >
                    {{ watchStatus === '想看' ? '✦ 已标记想看' : '✦ 想看' }}
                  </button>
                  <button
                    :class="['detail-action-btn', { active: watchStatus === '看过' }]"
                    @click.stop="$emit('watch', { movie, status: '看过' })"
                  >
                    {{ watchStatus === '看过' ? '✓ 已看过' : '✓ 看过' }}
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  visible: Boolean,
  movie: Object,
  isFavorited: Boolean,
  watchStatus: String
})
defineEmits(['close', 'favorite', 'watch'])

const TMDB_KEY = import.meta.env.VITE_TMDB_API_KEY || '4014001a79aab6f7bb3ff811e3574e53'
const IMG_BASE = 'https://image.tmdb.org/t/p'
const detail = ref(null)
const loading = ref(false)

const tmdb = axios.create({ baseURL: '/tmdb/3', timeout: 15000 })

// Fetch movie details when visible changes
watch(() => props.visible, async (v) => {
  if (v && props.movie) {
    loading.value = true
    detail.value = null
    try {
      const { data } = await tmdb.get(`/movie/${props.movie.id}`, {
        params: { api_key: TMDB_KEY, language: 'zh-CN' }
      })
      detail.value = data
    } catch {
      // Fallback: use whatever we have from the card
      detail.value = {
        ...props.movie,
        overview: props.movie.overview || '暂无简介',
        genres: [],
        runtime: 0,
        tagline: ''
      }
    } finally {
      loading.value = false
    }
  }
})

const posterUrl = computed(() =>
  detail.value?.poster_path ? IMG_BASE + '/w500' + detail.value.poster_path : ''
)

</script>

<style scoped>
/* ── Overlay ── */
.detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(6px);
}

/* ── Card ── */
.detail-card {
  position: relative;
  width: 100%;
  max-width: 860px;
  max-height: 85vh;
  overflow-y: auto;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.5);
}

/* ── Close button ── */
.detail-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.detail-close:hover {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
}

/* ── Layout ── */
.detail-layout {
  display: flex;
  gap: 36px;
}

/* ── Poster ── */
.detail-poster-col {
  flex-shrink: 0;
  width: 260px;
}
.detail-poster-wrap {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0,0,0,0.5);
}
.detail-poster {
  width: 100%;
  display: block;
}
.detail-no-poster {
  aspect-ratio: 2/3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  color: var(--el-text-color-placeholder);
}

/* ── Info ── */
.detail-info-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-title {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.15;
  color: var(--el-text-color-primary);
}
.detail-tagline {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 1rem;
  color: var(--el-text-color-secondary);
}

/* Meta row */
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.meta-item {
  padding: 4px 12px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
  font-size: 0.82rem;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* Rating bar */
.detail-rating-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rating-bar-bg {
  flex: 1;
  max-width: 180px;
  height: 6px;
  background: var(--el-fill-color-light);
  border-radius: 3px;
  overflow: hidden;
}
.rating-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-gold), var(--el-color-primary));
  border-radius: 3px;
  transition: width 0.6s ease;
}
.rating-number {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.rating-total {
  font-size: 0.85rem;
  color: var(--el-text-color-placeholder);
}

/* Overview */
.detail-overview h4 {
  font-size: 0.82rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--el-text-color-placeholder);
  margin-bottom: 6px;
}
.detail-overview p {
  font-size: 0.92rem;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

/* Actions */
.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: auto;
  padding-top: 8px;
}
.detail-action-btn {
  padding: 10px 18px;
  border-radius: 20px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color);
  color: var(--el-text-color-regular);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.detail-action-btn:hover {
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-dark);
}
.detail-action-btn.active {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}

/* ── Skeleton loading ── */
.detail-skeleton {
  display: flex;
  gap: 36px;
}
.skeleton-poster {
  width: 260px;
  aspect-ratio: 2/3;
  background: var(--el-fill-color-light);
  border-radius: 12px;
  flex-shrink: 0;
  animation: pulse 1.4s ease-in-out infinite;
}
.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 8px;
}
.skeleton-line {
  height: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  animation: pulse 1.4s ease-in-out infinite;
}
.skeleton-line.w100 { width: 100%; }
.skeleton-line.w80  { width: 80%; }
.skeleton-line.w60  { width: 60%; }
.skeleton-line.w40  { width: 40%; }

@keyframes pulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* ── Transitions ── */
.detail-enter-active { transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1); }
.detail-leave-active { transition: all 0.25s ease-in; }
.detail-enter-from,
.detail-leave-to {
  opacity: 0;
}
.detail-enter-from .detail-card {
  transform: scale(0.92) translateY(20px);
}
.detail-enter-active .detail-card {
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

/* ── Mobile ── */
@media (max-width: 700px) {
  .detail-card { padding: 24px 20px; }
  .detail-layout { flex-direction: column; align-items: center; }
  .detail-poster-col { width: 180px; }
  .detail-title { font-size: 1.4rem; text-align: center; }
  .detail-tagline { text-align: center; }
  .detail-meta { justify-content: center; }
  .detail-actions { justify-content: center; }
  .detail-skeleton { flex-direction: column; align-items: center; }
  .skeleton-poster { width: 180px; }
}
</style>
