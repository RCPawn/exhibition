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
@import '@/assets/styles/display-archive-toolbar.scss';

// 白族传统色彩
$bai-cyan: #3B82F6;      // 白族青
$bai-red: #C62828;       // 印章红
$ink-black: #1A1A1A;     // 墨黑
$paper-bg: #ffffff;

.gallery-wrapper {
  background-color: $paper-bg;
  min-height: calc(100vh - 70px);
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
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px 40px 120px;
  position: relative;
  z-index: 10;
}

// 瀑布流网格
.masonry-grid {
  column-count: 3;
  column-gap: 30px;
  padding: 10px;
}

// 简洁卡片样式
.gallery-card {
  background: #fff;
  cursor: pointer;
  margin-bottom: 30px;
  break-inside: avoid;
  transition: all 0.3s ease;

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
    padding: 20px 16px;
    text-align: center;

    .card-title {
      font-size: 14px;
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
      font-size: 12px;
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

// 响应式设计
@media (max-width: 1200px) {
  .masonry-grid {
    column-count: 2;
  }
}

@media (max-width: 768px) {
  .content-container {
    padding: 30px 20px 120px;
  }

  .masonry-grid {
    column-count: 2;
    column-gap: 20px;
  }

  .gallery-card {
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .masonry-grid {
    column-count: 1;
  }
}
</style>