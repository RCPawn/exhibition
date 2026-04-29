<template>
  <div class="gallery-wrapper">
    <div class="paper-texture"></div>

    <div class="content-container">
      <header class="archive-toolbar is-gallery">
        <div class="archive-toolbar__row">
          <div class="archive-toolbar__identity">
            <span class="seal-mark">白族</span>
            <h2 class="toolbar-title">纸上乾坤</h2>
            <span class="toolbar-divider" aria-hidden="true"></span>
            <p class="toolbar-lede">记录白族非遗的视觉映像与生活碎片</p>
          </div>
          <div class="archive-toolbar__actions">
            <div class="search-box" :class="{ 'is-focused': isSearchFocused }">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索标题..."
                clearable
                prefix-icon="Search"
                @input="handleSearch"
                @focus="isSearchFocused = true"
                @blur="isSearchFocused = false"
                class="search-input"
              />
            </div>
            <div class="count-pill">共 {{ filteredList.length }} 条</div>
          </div>
        </div>
      </header>

      <!-- 瀑布流布局 -->
      <div class="masonry-grid" v-loading="loading">
        <div
          v-for="(item, index) in filteredList"
          :key="item.galleryId"
          class="gallery-card"
          :style="{ order: getOrder(index) }"
          @click="goToDetail(item.galleryId)"
        >
          <div class="card-image" :class="getImageClass(index)">
            <img :src="getAssetUrl(item.coverUrl)" alt="gallery cover" />
          </div>
          <div class="card-info">
            <h3 class="card-title">{{ item.title }}</h3>
            <span class="card-date">{{ parseTime(item.createTime, '{y}.{m}.{d}') }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-decor">
      <InteractiveBar />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { listGallery } from "@/api/heritage/gallery";
import InteractiveBar from "@/components/InteractiveBar";

const router = useRouter();
const loading = ref(true);
const galleryList = ref([]);
const searchKeyword = ref(''); // 搜索关键词
const isSearchFocused = ref(false); // 搜索框焦点状态

const getAssetUrl = (url) => {
  if (!url) return '';
  const base = import.meta.env.VITE_APP_BASE_API;
  return url.startsWith('http') ? url : base + url;
};

const getOrder = (index) => {
  const orders = [1, 3, 2, 4, 2, 1, 3, 4, 1, 2];
  return orders[index % orders.length];
};

const getImageClass = (index) => {
  const classes = ['img-normal', 'img-wide', 'img-tall', 'img-extra-tall'];
  return classes[index % 4];
};

/** 搜索处理 */
const handleSearch = () => {
  // 搜索逻辑在计算属性中实现
};

/** 过滤后的列表 */
const filteredList = computed(() => {
  if (!searchKeyword.value) return galleryList.value;
  const keyword = searchKeyword.value.toLowerCase();
  return galleryList.value.filter(item =>
    item.title?.toLowerCase().includes(keyword)
  );
});

/** 跳转详情 - 智能判断前后端环境 */
const goToDetail = (id) => {
  // 检查当前是否在后台 Layout 中（通过检测 .app-wrapper 是否存在）
  const isInAdmin = document.querySelector('.app-wrapper') !== null;

  if (isInAdmin) {
    // 后台环境：使用后台管理路由
    router.push("/gallery-manage-detail/" + id);
  } else {
    // 前台环境：使用门户路由
    router.push("/display/images/detail/" + id);
  }
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listGallery();
    galleryList.value = res.rows || [];
  } catch (error) {
    console.error("Failed to fetch gallery:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(getList);
</script>

<style scoped lang="scss">
@use '@/assets/styles/display-archive-toolbar.scss' as *;

// 白族传统色彩
$bai-cyan: #3B82F6;      // 白族青
$bai-red: #C62828;       // 印章红
$ink-black: #1A1A1A;     // 墨黑
$paper-bg: #ffffff;

.gallery-wrapper {
  --gallery-pad-x: clamp(14px, 3.2vw, 48px);
  --gallery-pad-top: clamp(14px, 2.2vw, 28px);
  --gallery-bottom-space: calc(clamp(96px, 12vh, 140px) + env(safe-area-inset-bottom, 0px));
  --masonry-gap: clamp(16px, 1.8vw, 32px);
  background-color: $paper-bg;
  min-height: calc(100vh - var(--layout-navbar-height, 70px));
  min-height: calc(100dvh - var(--layout-navbar-height, 70px));
  position: relative;
  overflow-x: hidden;
}

.paper-texture {
  position: absolute;
  inset: 0;
  opacity: 0.3;
  pointer-events: none;
  background-image: url('https://www.transparenttextures.com/patterns/cream-paper.png');
}

.content-container {
  width: 100%;
  max-width: min(1760px, 100%);
  margin: 0 auto;
  padding: var(--gallery-pad-top) var(--gallery-pad-x) var(--gallery-bottom-space);
  position: relative;
  z-index: 10;
  box-sizing: border-box;
}

/* 与内容区对齐，避免工具栏与网格左右缩进不一致 */
:deep(.archive-toolbar) {
  max-width: 100%;
  margin-left: 0;
  margin-right: 0;
  padding-left: 0;
  padding-right: 0;
  margin-bottom: clamp(14px, 1.6vw, 24px);
}

/* 工具栏标题随屏宽略缩放，与门户顶栏协调 */
:deep(.archive-toolbar .toolbar-title) {
  font-size: clamp(1.125rem, 0.6vw + 1rem, 1.5rem);
}

:deep(.archive-toolbar .toolbar-lede) {
  max-width: min(560px, 100%);
}

@media (min-width: 1100px) {
  :deep(.archive-toolbar .toolbar-lede) {
    max-width: min(640px, 48vw);
  }
}

:deep(.archive-toolbar .search-box .search-input) {
  width: clamp(160px, 22vw, 220px);
}

:deep(.archive-toolbar .search-box.is-focused .search-input) {
  width: clamp(200px, 28vw, 300px);
}

// 瀑布流网格：列数随视口递增，兼顾 15.6 寸与 27 寸
.masonry-grid {
  column-count: 2;
  column-gap: var(--masonry-gap);
  padding: clamp(4px, 0.8vw, 12px) 0;
}

@media (min-width: 900px) {
  .masonry-grid {
    column-count: 3;
  }
}

@media (min-width: 1440px) {
  .masonry-grid {
    column-count: 4;
  }
}

@media (min-width: 2000px) {
  .masonry-grid {
    column-count: 5;
  }
}

// 简洁卡片样式
.gallery-card {
  background: #fff;
  cursor: pointer;
  margin-bottom: var(--masonry-gap);
  break-inside: avoid;
  transition: all 0.3s ease;
  border-radius: clamp(2px, 0.2vw, 6px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);

  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.1);
    transform: translateY(-4px);

    .card-image img {
      opacity: 0.9;
    }
  }

  .card-image {
    width: 100%;
    overflow: hidden;
    background: #f5f5f5;

    &.img-normal {
      aspect-ratio: 4/3;
    }

    &.img-wide {
      aspect-ratio: 16/9;
    }

    &.img-tall {
      aspect-ratio: 3/4;
    }

    &.img-extra-tall {
      aspect-ratio: 2/3;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      transition: opacity 0.3s ease;
    }
  }

  .card-info {
    padding: clamp(12px, 1.5vw, 20px) clamp(12px, 1.2vw, 16px);
    text-align: center;

    .card-title {
      font-size: clamp(13px, 0.85vw + 10px, 15px);
      font-weight: 500;
      color: #333;
      margin: 0 0 8px 0;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-date {
      font-size: clamp(11px, 0.35vw + 10px, 13px);
      color: #999;
      display: block;
    }
  }
}

// 底部装饰保留
.bottom-decor {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 100;
}

@media (max-width: 768px) {
  .masonry-grid {
    column-gap: clamp(12px, 3vw, 20px);
  }

  .gallery-card {
    margin-bottom: clamp(14px, 3vw, 22px);
  }
}

/* 最窄单列，须放在其它 max-width 规则之后以免被覆盖 */
@media (max-width: 599px) {
  .masonry-grid {
    column-count: 1;
  }
}
</style>