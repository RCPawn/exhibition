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
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 120px;
  position: relative;
  z-index: 10;
}

// 返回按钮
.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 100;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
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
  margin-bottom: 40px;

  .main-title {
    font-size: 28px;
    font-weight: 600;
    color: $ink-black;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .meta-info {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin-bottom: 20px;

    .meta-item {
      font-size: 13px;
      color: #666;
      display: flex;
      align-items: center;
      gap: 6px;

      .el-icon {
        font-size: 14px;
      }
    }
  }

  .header-divider {
    height: 1px;
    background: linear-gradient(to right, transparent, #ddd, transparent);
    margin: 0 auto;
    max-width: 600px;
  }
}

.main-gallery {
  margin-bottom: 40px;

  .gallery-frame {
    background: #2d4a3e;
    padding: 60px 80px;
    position: relative;
    border-radius: 4px;

    .nav-btn {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 48px;
      height: 48px;
      background: rgba(255,255,255,0.1);
      border: none;
      border-radius: 50%;
      color: #fff;
      font-size: 24px;
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(255,255,255,0.2);
      }

      &.prev-btn {
        left: 20px;
      }

      &.next-btn {
        right: 20px;
      }
    }

    .main-image-wrapper {
      position: relative;

      .main-image {
        width: 100%;
        max-height: 600px;
        object-fit: contain;
        display: block;
      }

      .image-caption {
        text-align: center;
        color: rgba(255,255,255,0.8);
        font-size: 14px;
        margin-top: 16px;
      }
    }

    .image-counter {
      position: absolute;
      bottom: 20px;
      right: 30px;
      color: rgba(255,255,255,0.7);
      font-size: 14px;
    }
  }
}

.thumbnail-list {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 60px;

  .thumb-item {
    width: 120px;
    height: 80px;
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
  margin-top: 60px;

  .section-title {
    font-size: 20px;
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
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;

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
  .back-btn {
    top: 10px;
    left: 10px;
    padding: 6px 12px;
    font-size: 12px;
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  .main-gallery .gallery-frame {
    padding: 40px 20px;

    .nav-btn {
      width: 36px;
      height: 36px;
      font-size: 18px;

      &.prev-btn { left: 10px; }
      &.next-btn { right: 10px; }
    }
  }

  .thumbnail-list .thumb-item {
    width: 80px;
    height: 60px;
  }
}
</style>
