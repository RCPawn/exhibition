<template>
  <div class="detail-wrapper" v-loading="loading">
    <div class="paper-texture"></div>

    <div class="detail-container">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeftBold /></el-icon>
        <span>返回</span>
      </button>

      <!-- 头部信息 -->
      <div class="detail-header">
        <h1 class="main-title">{{ gallery.title }}</h1>
        <div class="meta-info">
          <span class="meta-item">
            <el-icon><Document /></el-icon>
            来源：{{ gallery.source || '白族非遗档案馆' }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            创建时间：{{ parseTime(gallery.createTime, '{y}.{m}.{d}') }}
          </span>
        </div>
        <div class="header-divider"></div>
      </div>

      <!-- 主图展示区（说明叠在画框下缘，省纵向空间，接近常见非遗/文博站） -->
      <div class="main-gallery">
        <div class="gallery-frame">
          <button type="button" class="nav-btn prev-btn" @click="prevImage">
            <el-icon><ArrowLeft /></el-icon>
          </button>

          <div class="main-image-wrapper">
            <img :src="getAssetUrl(currentImage)" class="main-image" alt="" />
            <div v-if="currentCaption" class="image-caption">{{ currentCaption }}</div>
          </div>

          <button type="button" class="nav-btn next-btn" @click="nextImage">
            <el-icon><ArrowRight /></el-icon>
          </button>

          <div v-if="images.length" class="image-counter">{{ currentIndex + 1 }}/{{ images.length }}</div>
        </div>
      </div>

      <!-- 缩略图：固定高度单行横滑，避免多行占高 -->
      <div class="thumbnail-list" v-if="images.length > 0">
        <div
          v-for="(img, index) in images"
          :key="index"
          class="thumb-item"
          :class="{ active: index === currentIndex }"
          role="button"
          tabindex="0"
          @click="selectImage(index)"
          @keydown.enter.prevent="selectImage(index)"
          @keydown.space.prevent="selectImage(index)"
        >
          <img :src="getAssetUrl(img.imageUrl)" alt="" />
        </div>
      </div>

      <!-- 其他资源 -->
      <div class="other-resources" v-if="relatedItems && relatedItems.length > 0">
        <h3 class="section-title">其他资源</h3>
        <div class="resource-divider"></div>
        <div class="resource-grid">
          <div
              v-for="item in relatedItems"
              :key="item.galleryId"
              class="resource-card"
              @click="router.push(`/display/images/detail/${item.galleryId}`)"
          >
            <img :src="getAssetUrl(item.coverUrl)" />
            <div class="resource-info">
              <h4>{{ item.title }}</h4>
            </div>
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
import { useRoute, useRouter } from 'vue-router';
import { getGallery } from "@/api/heritage/gallery";
import InteractiveBar from "@/components/InteractiveBar";
import { Document, Clock, ArrowLeft, ArrowRight, ArrowLeftBold } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const gallery = ref({});
const images = ref([]);
const currentIndex = ref(0);
const relatedItems = ref([]);

const getAssetUrl = (url) => {
  if (!url) return '';
  const base = import.meta.env.VITE_APP_BASE_API;
  return url.startsWith('http') ? url : base + url;
};

const currentImage = computed(() => {
  return images.value[currentIndex.value]?.imageUrl || '';
});

const currentCaption = computed(() => {
  return images.value[currentIndex.value]?.caption || '';
});

const selectImage = (index) => {
  currentIndex.value = index;
};

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const nextImage = () => {
  if (currentIndex.value < images.value.length - 1) {
    currentIndex.value++;
  }
};

const getDetail = async () => {
  loading.value = true;
  try {
    const res = await getGallery(route.params.id);
    gallery.value = res.data;
    images.value = res.data.heritageGalleryImageList || [];
    // TODO: 获取相关图集列表
    relatedItems.value = [];
  } catch (error) {
    console.error("Failed to fetch gallery detail:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(getDetail);

const goBack = () => {
  router.back();
};
</script>

<style scoped lang="scss">
$ink-black: #1A1A1A;
$paper-bg: #FAFAFA;

.detail-wrapper {
  --detail-pad-x: clamp(14px, 3.2vw, 36px);
  --detail-pad-top: clamp(46px, 5vw, 72px);
  --detail-bottom-space: calc(clamp(76px, 8.5vh, 112px) + env(safe-area-inset-bottom, 0px));
  --detail-section-gap: clamp(10px, 1.5vw, 20px);
  /*
   * 一屏纵览：说明叠在画框内、缩略图为单行固定高，故预留主要含顶栏 + 页头 + 画框边 + 缩略条 + 底栏。
   */
  --detail-image-max: min(
    54vh,
    calc(100vh - var(--layout-navbar-height, 70px) - clamp(188px, 22vh, 268px)),
    680px
  );
  background-color: $paper-bg;
  min-height: calc(100vh - var(--layout-navbar-height, 70px));
  min-height: calc(100dvh - var(--layout-navbar-height, 70px));
  position: relative;
  overflow-x: hidden;
}

@supports (height: 1dvh) {
  .detail-wrapper {
    --detail-image-max: min(
      54dvh,
      calc(100dvh - var(--layout-navbar-height, 70px) - clamp(188px, 22dvh, 268px)),
      680px
    );
  }
}

.paper-texture {
  position: absolute;
  inset: 0;
  opacity: 0.3;
  pointer-events: none;
  background-image: url('https://www.transparenttextures.com/patterns/cream-paper.png');
}

.detail-container {
  width: 100%;
  max-width: min(1440px, 96vw);
  margin: 0 auto;
  padding: var(--detail-pad-top) var(--detail-pad-x) var(--detail-bottom-space);
  position: relative;
  z-index: 10;
  box-sizing: border-box;
}

// 返回按钮
.back-btn {
  position: absolute;
  top: clamp(12px, 2vw, 24px);
  left: clamp(12px, 2.5vw, 28px);
  z-index: 100;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: clamp(6px, 1vw, 10px) clamp(12px, 1.8vw, 18px);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: clamp(12px, 0.35vw + 11px, 14px);
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);

  .el-icon {
    font-size: clamp(14px, 0.5vw + 12px, 17px);
  }

  &:hover {
    color: #409eff;
    border-color: #c6e2ff;
    background: #ecf5ff;
  }

  &:active {
    transform: scale(0.98);
  }
}

.detail-header {
  text-align: center;
  margin-bottom: var(--detail-section-gap);
  padding-inline: clamp(4px, 2vw, 20px);

  .main-title {
    font-size: clamp(1.05rem, 0.85vw + 0.75rem, 1.5rem);
    font-weight: 600;
    color: $ink-black;
    margin: 0 0 clamp(4px, 1vw, 10px) 0;
    line-height: 1.25;
    word-break: break-word;
  }

  .meta-info {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: clamp(8px, 1.5vw, 20px);
    margin-bottom: clamp(6px, 1.2vw, 12px);

    .meta-item {
      font-size: clamp(12px, 0.25vw + 11px, 14px);
      color: #666;
      display: flex;
      align-items: center;
      gap: 6px;
      text-align: left;

      .el-icon {
        font-size: clamp(13px, 0.3vw + 12px, 15px);
        flex-shrink: 0;
      }
    }
  }

  .header-divider {
    height: 1px;
    background: linear-gradient(to right, transparent, #ddd, transparent);
    margin: 0 auto;
    max-width: min(640px, 88vw);
  }
}

.main-gallery {
  margin-bottom: var(--detail-section-gap);
  min-width: 0;

  .gallery-frame {
    background: #2d4a3e;
    padding: clamp(12px, 2vw, 28px) clamp(12px, 3vw, 56px);
    position: relative;
    border-radius: clamp(4px, 0.35vw, 8px);

    .nav-btn {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: clamp(36px, 3.5vw, 52px);
      height: clamp(36px, 3.5vw, 52px);
      background: rgba(255,255,255,0.1);
      border: none;
      border-radius: 50%;
      color: #fff;
      font-size: clamp(18px, 1.8vw, 26px);
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2;

      &:hover {
        background: rgba(255,255,255,0.2);
      }

      &.prev-btn {
        left: clamp(8px, 1.8vw, 28px);
      }

      &.next-btn {
        right: clamp(8px, 1.8vw, 28px);
      }
    }

    .main-image-wrapper {
      position: relative;
      margin-inline: auto;
      max-width: 100%;

      .main-image {
        width: 100%;
        max-height: var(--detail-image-max);
        object-fit: contain;
        display: block;
        margin: 0 auto;
      }

      /* 叠在画框下缘（文博站常见），不占主纵向流 */
      .image-caption {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 1;
        padding: clamp(20px, 4vw, 36px) clamp(10px, 2vw, 20px) clamp(8px, 1.5vw, 12px);
        text-align: center;
        font-size: clamp(11px, 0.3vw + 10px, 14px);
        line-height: 1.45;
        color: rgba(255, 255, 255, 0.95);
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.45);
        background: linear-gradient(to top, rgba(0, 0, 0, 0.55), transparent);
        max-height: 38%;
        overflow-y: auto;
        pointer-events: none;
      }
    }

    .image-counter {
      position: absolute;
      top: clamp(8px, 1.5vw, 14px);
      right: clamp(10px, 2vw, 20px);
      z-index: 2;
      color: rgba(255,255,255,0.75);
      font-size: clamp(12px, 0.3vw + 11px, 14px);
    }
  }
}

.thumbnail-list {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  height: 52px;
  padding: 4px 0;
  margin-bottom: clamp(14px, 2vh, 24px);
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scroll-snap-type: x mandatory;
  scrollbar-width: thin;
  scroll-behavior: smooth;

  .thumb-item {
    flex: 0 0 72px;
    width: 72px;
    height: 44px;
    flex-shrink: 0;
    cursor: pointer;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
    scroll-snap-align: start;

    &:focus-visible {
      outline: 2px solid #409eff;
      outline-offset: 2px;
    }

    &:hover {
      border-color: #2d4a3e;
    }

    &.active {
      border-color: #2d4a3e;
      box-shadow: 0 0 0 1px rgba(45, 74, 62, 0.35);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }
}

.other-resources {
  margin-top: clamp(40px, 5vw, 72px);

  .section-title {
    font-size: clamp(1rem, 0.6vw + 0.85rem, 1.35rem);
    font-weight: 600;
    color: $ink-black;
    margin: 0 0 12px 0;
  }

  .resource-divider {
    height: 1px;
    background: #ddd;
    margin-bottom: clamp(16px, 2vw, 24px);
  }

  .resource-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(min(100%, clamp(200px, 42vw, 320px)), 1fr));
    gap: clamp(14px, 2vw, 24px);

    .resource-card {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      }

      img {
        width: 100%;
        aspect-ratio: 16/9;
        object-fit: cover;
        display: block;
      }

      .resource-info {
        padding: clamp(10px, 1.2vw, 14px);

        h4 {
          font-size: clamp(13px, 0.35vw + 11px, 15px);
          font-weight: 500;
          color: #333;
          margin: 0;
        }
      }
    }
  }
}

.bottom-decor {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 100;
}

@media (max-width: 768px) {
  .detail-container {
    padding-top: clamp(56px, 14vw, 72px);
  }

  .back-btn {
    .el-icon {
      font-size: 14px;
    }
  }

  .main-gallery .gallery-frame {
    padding: clamp(20px, 5vw, 36px) clamp(10px, 3vw, 20px);
    /* 底部翻页钮贴底时给主图区留白，避免与说明文字重叠 */
    padding-bottom: clamp(52px, 16vw, 72px);

    .nav-btn {
      &.prev-btn,
      &.next-btn {
        top: auto;
        bottom: clamp(8px, 2vw, 14px);
        transform: none;
      }

      &.prev-btn {
        left: clamp(8px, 2vw, 14px);
      }

      &.next-btn {
        right: clamp(8px, 2vw, 14px);
      }
    }

    .image-counter {
      bottom: auto;
      top: clamp(8px, 2vw, 12px);
      right: clamp(8px, 2vw, 12px);
    }
  }
}

/* 笔记本常见高度：略减预留，主图仍可读 */
@media (max-height: 820px) {
  .detail-wrapper {
    --detail-image-max: min(58vh, calc(100vh - clamp(232px, 24vh, 300px)), 720px);
    --detail-pad-top: clamp(48px, 6vh, 72px);
    --detail-section-gap: clamp(12px, 1.8vh, 22px);
  }
}

@supports (height: 1dvh) {
  @media (max-height: 820px) {
    .detail-wrapper {
      --detail-image-max: min(58dvh, calc(100dvh - clamp(232px, 24dvh, 300px)), 720px);
      --detail-pad-top: clamp(48px, 6dvh, 72px);
      --detail-section-gap: clamp(12px, 1.8dvh, 22px);
    }
  }
}

/* 大屏：略放宽主图上限，仍用 calc 约束一屏，不单独堆高 vh% */
@media (min-width: 1920px) {
  .detail-container {
    max-width: min(1560px, 94vw);
  }

  .detail-wrapper {
    --detail-image-max: min(56vh, calc(100vh - clamp(240px, 22vh, 300px)), 720px);
    --detail-pad-top: clamp(44px, 4.5vw, 64px);
  }
}

@supports (height: 1dvh) {
  @media (min-width: 1920px) {
    .detail-wrapper {
      --detail-image-max: min(56dvh, calc(100dvh - clamp(240px, 22dvh, 300px)), 720px);
    }
  }
}

/* 矮屏（笔记本横条窗、分屏）：压缩主图可视高度 */
@media (max-height: 560px) and (min-width: 480px) {
  .main-gallery .gallery-frame .main-image-wrapper .main-image {
    max-height: min(48vh, 380px);
    max-height: min(48dvh, 380px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .back-btn,
  .thumb-item {
    transition: none;
  }

  .back-btn:active {
    transform: none;
  }
}
</style>
