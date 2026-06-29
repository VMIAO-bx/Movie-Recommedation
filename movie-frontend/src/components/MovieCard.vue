<template>
  <article class="movie-card" :class="{ 'has-status': !!watchStatus }" @click="$emit('detail', movie)">
    <!-- Poster -->
    <div class="poster-frame">
      <!-- Placeholder always present as background layer -->
      <div v-if="!posterLoaded" class="poster-placeholder">
        <span class="placeholder-title">{{ movie.title.slice(0, 4) }}</span>
      </div>
      <!-- Image on top, invisible until loaded -->
      <img
        v-if="posterUrl"
        :src="posterUrl"
        :alt="movie.title"
        class="poster"
        :class="{ loaded: posterLoaded }"
        loading="lazy"
        @load="posterLoaded = true"
        @error="posterLoaded = false"
      />

      <!-- Letterbox hover matte -->
      <div class="matte matte-top" />
      <div class="matte matte-bottom" />

      <!-- Hover actions -->
      <div class="hover-actions">
        <button
          class="action-chip"
          :class="{ active: isFavorited }"
          title="收藏"
          @click.stop="$emit('favorite', movie)"
        >
          <span class="chip-icon">{{ isFavorited ? '♥' : '♡' }}</span>
          <span class="chip-label">收藏</span>
        </button>
        <button
          class="action-chip"
          :class="{ active: watchStatus === '想看' }"
          title="想看"
          @click.stop="$emit('watch', { movie, status: '想看' })"
        >
          <span class="chip-icon">✦</span>
          <span class="chip-label">想看</span>
        </button>
        <button
          class="action-chip"
          :class="{ active: watchStatus === '看过' }"
          title="看过"
          @click.stop="$emit('watch', { movie, status: '看过' })"
        >
          <span class="chip-icon">✓</span>
          <span class="chip-label">看过</span>
        </button>
      </div>

      <!-- Rating badge -->
      <div v-if="movie.vote_average" class="rating">
        <span class="rating-star">★</span>
        <span class="rating-value">{{ movie.vote_average.toFixed(1) }}</span>
      </div>

      <!-- Watch status badge -->
      <div v-if="watchStatus" class="status-badge" :class="'status-' + watchStatus">
        {{ watchStatus }}
      </div>
    </div>

    <!-- Info -->
    <div class="card-body">
      <h3 class="card-title" :title="movie.title">{{ movie.title }}</h3>
      <p class="card-year">{{ movie.release_date?.slice(0, 4) || '未知' }}</p>
    </div>
  </article>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  movie: { type: Object, required: true },
  isFavorited: { type: Boolean, default: false },
  watchStatus: { type: String, default: '' }
})

defineEmits(['favorite', 'watch', 'detail'])

const posterLoaded = ref(false)
const TMDB_IMG = 'https://image.tmdb.org/t/p/w500'

const posterUrl = computed(() =>
  props.movie.poster_path ? TMDB_IMG + props.movie.poster_path : ''
)
</script>

<style scoped>
.movie-card {
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              box-shadow 0.3s ease,
              border-color 0.3s ease;
}
.movie-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.55);
  border-color: var(--el-border-color);
}

/* Poster */
.poster-frame {
  position: relative;
  aspect-ratio: 2 / 3;
  background: #1a1a24;
  overflow: hidden;
}
.poster {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.35s ease, transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.poster.loaded {
  opacity: 1;
}
.movie-card:hover .poster {
  transform: scale(1.06);
}
.poster-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a28 0%, #202038 50%, #1a1a28 100%);
}
.placeholder-title {
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.18);
  letter-spacing: 0.08em;
  text-align: center;
  line-height: 1.3;
  user-select: none;
}

/* Letterbox mattes */
.matte {
  position: absolute;
  left: 0;
  right: 0;
  height: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(2px);
  transition: height 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  z-index: 2;
}
.matte-top { top: 0; }
.matte-bottom { bottom: 0; }
.movie-card:hover .matte {
  height: 44px;
}

/* Hover actions */
.hover-actions {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s ease 0.05s;
}
.movie-card:hover .hover-actions {
  opacity: 1;
}

.action-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}
.action-chip:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.35);
  transform: scale(1.08);
}
.action-chip.active {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
}
.chip-icon {
  font-size: 1.1rem;
  line-height: 1;
}
.chip-label {
  font-size: 0.7rem;
  font-weight: 500;
  opacity: 0.9;
}

/* Rating */
.rating {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.72);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  font-size: 0.82rem;
  font-weight: 700;
}
.rating-star {
  color: var(--color-gold);
  font-size: 0.85rem;
}
.rating-value {
  color: #fff;
}

/* Status badge */
.status-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 4;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(6px);
}
.status-想看 {
  background: rgba(232, 184, 75, 0.2);
  color: var(--color-gold);
  border: 1px solid rgba(232, 184, 75, 0.35);
}
.status-看过 {
  background: rgba(77, 171, 122, 0.2);
  color: var(--color-success);
  border: 1px solid rgba(77, 171, 122, 0.35);
}

/* Card body */
.card-body {
  padding: 12px 14px;
}
.card-title {
  font-size: 0.92rem;
  font-weight: 600;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}
.card-year {
  font-size: 0.8rem;
  color: var(--el-text-color-secondary);
}

@media (max-width: 640px) {
  .hover-actions { gap: 6px; }
  .action-chip { padding: 6px 10px; }
}
</style>
