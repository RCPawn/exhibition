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

      <!-- 主图展示区 -->
      <div class="main-gallery">
        <div class="gallery-frame">
          <button class="nav-btn prev-btn" @click="prevImage">
            <el-icon><ArrowLeft /></el-icon>
          </button>

          <div class="main-image-wrapper">
            <img :src="getAssetUrl(currentImage)" class="main-image" />
            <div class="image-caption">{{ currentCaption }}</div>
          </div>

          <button class="nav-btn next-btn" @click="nextImage">
            <el-icon><ArrowRight /></el-icon>
          </button>

          <div class="image-counter">{{ currentIndex + 1 }}/{{ images.length }}</div>
        </div>
      </div>

      <!-- 缩略图列表 -->
      <div class="thumbnail-list" v-if="images.length > 0">
        <div
            v-for="(img, index) in images"
            :key="index"
            class="thumb-item"
            :class="{ active: index === currentIndex }"
            @click="selectImage(index)"
        >
          <img :src="getAssetUrl(img.imageUrl)" />
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
              @click="router.push(`/display/gallery/detail/${item.galleryId}`)"
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
  background-color: $paper-bg;
  min-height: calc(100vh - 70px);
  min-height: calc(100dvh - 70px);
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

.detail-container {
  width: 100%;
  max-width: min(1440px, 96vw);
  margin: 0 auto;
  padding: clamp(72px, 10vw, 100px) clamp(14px, 3.5vw, 40px) clamp(96px, 12vh, 140px);
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
    font-size: 16px;
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
  margin-bottom: clamp(24px, 3.5vw, 44px);
  padding-inline: clamp(4px, 2vw, 24px);

  .main-title {
    font-size: clamp(1.25rem, 1.2vw + 0.85rem, 2rem);
    font-weight: 600;
    color: $ink-black;
    margin: 0 0 12px 0;
    line-height: 1.35;
    word-break: break-word;
  }

  .meta-info {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: clamp(12px, 2.5vw, 36px);
    margin-bottom: 20px;

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
  margin-bottom: clamp(28px, 4vw, 48px);

  .gallery-frame {
    background: #2d4a3e;
    padding: clamp(28px, 4.5vw, 56px) clamp(16px, 5vw, 88px);
    position: relative;
    border-radius: clamp(4px, 0.4vw, 8px);

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
        max-height: min(72vh, 920px);
        max-height: min(72dvh, 920px);
        object-fit: contain;
        display: block;
        margin: 0 auto;
      }

      .image-caption {
        text-align: center;
        color: rgba(255,255,255,0.85);
        font-size: clamp(12px, 0.35vw + 11px, 15px);
        margin-top: clamp(10px, 1.5vw, 18px);
        line-height: 1.5;
        padding-inline: clamp(8px, 2vw, 24px);
      }
    }

    .image-counter {
      position: absolute;
      bottom: clamp(10px, 2vw, 22px);
      right: clamp(12px, 2.5vw, 32px);
      color: rgba(255,255,255,0.75);
      font-size: clamp(12px, 0.3vw + 11px, 14px);
    }
  }
}

.thumbnail-list {
  display: flex;
  gap: clamp(10px, 1.4vw, 18px);
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: clamp(40px, 6vw, 72px);
  padding-inline: clamp(4px, 1vw, 12px);

  .thumb-item {
    width: clamp(72px, 12vw, 132px);
    height: clamp(48px, 8vw, 88px);
    flex-shrink: 0;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      border-color: #2d4a3e;
    }

    &.active {
      border-color: #2d4a3e;
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
    margin-bottom: 24px;
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
        padding: 12px;

        h4 {
          font-size: 14px;
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

@media (min-width: 769px) and (max-width: 1199px) {
  .main-gallery .gallery-frame .main-image-wrapper .main-image {
    max-height: min(68vh, 720px);
    max-height: min(68dvh, 720px);
  }
}

@media (min-width: 1920px) {
  .detail-container {
    max-width: min(1560px, 94vw);
  }

  .main-gallery .gallery-frame .main-image-wrapper .main-image {
    max-height: min(78vh, 1000px);
    max-height: min(78dvh, 1000px);
  }
}
</style>
