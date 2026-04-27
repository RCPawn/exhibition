<template>
  <div class="exhibit-container">
    <!-- 1. 顶部搜索：与轮播/名录同宽轨道；半透明浮在轮播之上（B 站式重叠） -->
    <div class="search-section">
      <div class="search-strip-inner">
        <div class="search-bar">
          <el-input
              v-model="queryParams.itemName"
              placeholder="搜索白族非遗瑰宝..."
              clearable
              @keyup.enter="handleQuery"
              class="poizon-input"
          />
          <button type="button" class="search-icon-btn" aria-label="搜索" @click="handleQuery">
            <el-icon><Search /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 2. 轮播推荐（保留全屏轮播体验，高度随视口微调） -->
    <div v-if="bannerList.length" class="hero-carousel">
      <el-carousel
        :interval="5000"
        :height="`${carouselHeightPx}px`"
        :indicator-position="carouselIndicatorPosition"
      >
        <el-carousel-item v-for="item in bannerList" :key="item.itemId">
          <div class="carousel-content">
            <img :src="getAssetUrl(item.coverImage)" class="carousel-img" alt="" />
            <div class="carousel-info">
              <span class="tag">今日推荐</span>
              <h2 class="title">{{ item.itemName }}</h2>
              <p class="desc">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 3. 非遗文化列表：极简工业风卡片 -->
    <div class="content-section">
      <div class="section-header">
        <h3 class="section-title">
          白族非遗名录
          <span class="section-title-en">HERITAGE LIST</span>
        </h3>
        <div class="filter-tabs" role="tablist" aria-label="分类筛选">
          <span
            role="tab"
            :class="{ active: !queryParams.categoryId }"
            @click="filterByCategory(null)"
          >全部</span>
          <span
            v-for="cat in categoryList"
            :key="cat.id"
            role="tab"
            :class="{ active: queryParams.categoryId === cat.id }"
            @click="filterByCategory(cat.id)"
          >{{ cat.name }}</span>
        </div>
      </div>

      <el-row :gutter="25" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in heritageList" :key="item.itemId">
          <div
            class="poizon-card"
            tabindex="0"
            role="button"
            :aria-label="`查看 ${item.itemName} 详情`"
            @click="goToDetail(item.itemId)"
            @keydown.enter.prevent="goToDetail(item.itemId)"
            @keydown.space.prevent="goToDetail(item.itemId)"
          >
            <div class="card-image-wrapper">
              <el-image :src="getAssetUrl(item.coverImage)" fit="cover">
                <template #placeholder>
                  <div class="image-slot">加载中...</div>
                </template>
                <template #error>
                  <div class="image-slot">加载失败</div>
                </template>
              </el-image>
              <div class="card-tag">{{ item.categoryName }}</div>
              <!-- 仅悬停/键盘聚焦时：中央遮罩 + 查看详情 -->
              <div class="card-hover-hint" aria-hidden="true">
                <span class="card-hover-hint__text">查看详情</span>
              </div>
            </div>
            <div class="card-content">
              <h4 class="item-name">{{ item.itemName }}</h4>
              <p class="item-intro">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
        />
      </div>
    </div>

    <!-- 页脚 -->
    <SiteFooter />
  </div>
</template>

<script setup name="Exhibit">
import { ref, reactive, onMounted, getCurrentInstance, computed } from 'vue';
import { useWindowSize } from '@vueuse/core';
import { useRoute, useRouter } from 'vue-router';
import { listHeritage_manage } from "@/api/heritage/heritage_manage";
import { Search } from "@element-plus/icons-vue";
import SiteFooter from "@/components/SiteFooter.vue";

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();
const { height: windowHeight, width: windowWidth } = useWindowSize();

/**
 * 轮播高度：按「视口 − 顶栏 − 搜索区」后的可用首屏空间取比例。
 * 大屏仍可较高；15.6 寸常见 768～900 高时压低比例与上限，让名录区同一屏内多露一些，避免「只剩冰山一角」。
 */
const carouselHeightPx = computed(() => {
  const h = windowHeight.value || 800;
  const w = windowWidth.value || 1366;
  const layoutNav = 52;
  /** 搜索行已压扁，少扣高度，把像素让给轮播 */
  /** 卡片已去掉「查看详情」行，首屏可多让给轮播 */
  const searchBlock = Math.min(62, Math.max(36, Math.round(h * 0.052)));
  const usable = Math.max(260, h - layoutNav - searchBlock);
  /** 笔记本一屏：轮播尽量高，仍预留名录标题 + 一行卡片（含标题+简介） */
  const laptopish = w <= 1680 && h <= 960;

  let ratio = 0.53;
  if (h < 760) ratio = 0.39;
  else if (h < 820) ratio = 0.42;
  else if (h < 900) ratio = 0.45;
  else if (h < 1000) ratio = 0.48;
  else if (h < 1080) ratio = 0.50;
  else if (h < 1200) ratio = 0.52;

  if (laptopish) {
    ratio = Math.min(0.60, ratio + 0.09);
  }

  if (w <= 1440 && h <= 900) {
    ratio = Math.min(ratio, laptopish ? 0.55 : 0.48);
  }

  let px = Math.round(usable * ratio);
  const cap = h < 820 ? 515 : h < 900 ? 590 : h < 1080 ? 635 : 715;
  const floor = h < 760 ? 278 : 300;
  px = Math.min(cap, Math.max(floor, px));
  return px;
});

/** 矮屏指示器放外侧会多占一截高度，改为默认（叠在图内）减轻纵向挤压 */
const carouselIndicatorPosition = computed(() =>
  (windowHeight.value || 800) < 860 ? 'none' : 'outside'
);

// --- 数据响应式变量 ---
const loading = ref(true);
const total = ref(0);
const heritageList = ref([]);
const bannerList = ref([]);
const categoryList = ref([
  { id: 1, name: '传统技艺' },
  { id: 2, name: '民族服饰' },
  { id: 3, name: '建筑艺术' },
  { id: 4, name: '民俗活动' }
]);

// --- 修改部分：添加排序参数 ---
const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  itemName: undefined,
  categoryId: undefined,
  // 核心修改：指定排序字段和顺序
  orderByColumn: 'sort_order', // 数据库中的排序字段名
  isAsc: 'asc'                 // 'asc'=升序(小到大), 'desc'=降序
});

// --- 方法 ---

/** 获取资源完整路径 */
const getAssetUrl = (url) => {
  if (!url) return '';
  return import.meta.env.VITE_APP_BASE_API + url;
};

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listHeritage_manage(queryParams);
    heritageList.value = res.rows;
    total.value = res.total;

    // 首次加载时，拿前3个作为轮播图 (也可以单独写个接口取推荐)
    if (bannerList.value.length === 0 && res.rows.length > 0) {
      bannerList.value = res.rows.slice(0, 3);
    }
  } catch (error) {
    console.error("数据加载失败", error);
  } finally {
    loading.value = false;
  }
};

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

/** 按分类筛选 */
const filterByCategory = (id) => {
  queryParams.categoryId = id;
  handleQuery();
};

/** 跳转详情 - 智能判断前后端环境 */
const goToDetail = (id) => {
  // 检查当前是否在后台 Layout 中（通过检测 .app-wrapper 是否存在）
  const isInAdmin = document.querySelector('.app-wrapper') !== null;
  
  if (isInAdmin) {
    // 后台环境：使用后台管理路由
    router.push("/heritage-manage-detail/" + id);
  } else {
    // 前台环境：使用门户路由
    router.push("/display/detail/" + id);
  }
};

onMounted(() => {
  if (route.query.itemId) {
    console.log("当前定位 ID:", route.query.itemId);
  }
  getList();
});
</script>

<style lang="scss" scoped>
/* 保持原有样式不变 */
.exhibit-container {
  /* 轮播 + 名录 + 搜索轨道统一宽度与左右留白 */
  --exhibit-inner-max: 1400px;
  --exhibit-inner-pad: clamp(14px, 2.4vw, 28px);
  /* 仅与「药丸搜索条」视觉重叠，略小于原整行，减少挡住轮播主体 */
  --exhibit-search-overlap: clamp(40px, 7vh, 54px);

  background-color: #fff;
  min-height: 100vh;

  .search-section {
    position: sticky;
    top: 0;
    z-index: 30;
    padding: clamp(6px, 1vh, 10px) 0;
    /* 整行透明，轮播可从两侧透出；毛玻璃只落在搜索药丸上 */
    background: transparent;
    border-bottom: none;
    pointer-events: none;

    .search-strip-inner {
      max-width: var(--exhibit-inner-max);
      margin: 0 auto;
      padding: 0 var(--exhibit-inner-pad);
      width: 100%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      pointer-events: none;
    }

    .search-bar {
      pointer-events: auto;
      display: flex;
      width: 100%;
      max-width: min(520px, 92vw);
      gap: 0;
      align-items: stretch;
      padding: 4px 4px 4px 12px;
      /* 保留搜索条圆角；无外描边，避免与输入聚焦态叠出「双边框」 */
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.42);
      backdrop-filter: blur(22px) saturate(180%);
      -webkit-backdrop-filter: blur(22px) saturate(180%);
      border: none;
      box-shadow:
        0 1px 1px rgba(255, 255, 255, 0.55) inset,
        0 4px 18px rgba(0, 0, 0, 0.07);

      &:focus-within {
        background: rgba(255, 255, 255, 0.5);
        box-shadow:
          0 1px 1px rgba(255, 255, 255, 0.6) inset,
          0 4px 22px rgba(0, 0, 0, 0.09);
      }

      .poizon-input {
        flex: 1;
        min-width: 0;
        align-self: center;
        :deep(.el-input__wrapper) {
          min-height: 34px;
          border-radius: 0;
          box-shadow: none !important;
          outline: none;
          background-color: transparent;
          border: none !important;
          padding-left: 2px;
          padding-right: 4px;
          transition: background 0.2s ease;
          &.is-focus,
          &:hover {
            box-shadow: none !important;
            border: none !important;
          }
          &.is-focus {
            /* 不再单独铺一层底，整条由 .search-bar:focus-within 统一提亮 */
            background-color: transparent;
          }
        }

        :deep(.el-input__inner) {
          font-size: 14px;
        }
      }

      /* 与输入同一毛玻璃条，仅悬停轻微反馈（B 站式融合） */
      .search-icon-btn {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        align-self: center;
        width: 38px;
        height: 30px;
        margin: 0 2px 0 0;
        padding: 0;
        border: none;
        border-radius: 6px;
        background: transparent;
        color: rgba(0, 0, 0, 0.42);
        cursor: pointer;
        transition: color 0.2s ease, background 0.2s ease;

        .el-icon {
          font-size: 17px;
        }

        &:hover {
          color: rgba(0, 0, 0, 0.72);
          background: rgba(0, 0, 0, 0.05);
        }

        &:active {
          background: rgba(0, 0, 0, 0.08);
        }
      }
    }
  }

  .hero-carousel {
    position: relative;
    z-index: 1;
    max-width: var(--exhibit-inner-max);
    margin: calc(-1 * var(--exhibit-search-overlap)) auto clamp(8px, 1.2vh, 14px);
    padding: 0 var(--exhibit-inner-pad);
    border-radius: 0;
    overflow: visible;
    box-sizing: border-box;

    :deep(.el-carousel__container) {
      border-radius: 0;
      overflow: hidden;
    }

    .carousel-content {
      position: relative;
      height: 100%;
      overflow: hidden;

      .carousel-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center 28%;
        transition: transform 0.6s ease;
      }

      &:hover .carousel-img {
        transform: scale(1.02);
      }

      .carousel-info {
        position: absolute;
        left: clamp(16px, 3vw, 48px);
        right: clamp(16px, 3vw, 48px);
        bottom: clamp(12px, 2.5vh, 40px);
        max-height: min(58%, calc(100% - 20px));
        overflow: hidden;
        display: flex;
        flex-direction: column;
        color: #fff;
        z-index: 2;
        box-sizing: border-box;

        .tag {
          align-self: flex-start;
          width: fit-content;
          background: #00ffcc;
          color: #000;
          padding: 4px 12px;
          font-weight: 900;
          font-size: clamp(10px, 1vw, 12px); // 响应式字体
          text-transform: uppercase;
          flex-shrink: 0;
        }

        .title {
          font-size: clamp(20px, 3.2vw, 40px);
          margin: 10px 0 8px;
          line-height: 1.2;
          text-shadow: 0 2px 10px rgba(0,0,0,0.5);
          flex-shrink: 0;
        }

        .desc {
          flex: 1 1 auto;
          min-height: 0;
          overflow-x: hidden;
          overflow-y: auto;
          overscroll-behavior: contain;
          -webkit-overflow-scrolling: touch;
          padding-right: 4px;
          font-size: clamp(13px, 1.35vw, 16px);
          line-height: 1.55;
          opacity: 0.95;
          margin: 0;
          white-space: pre-wrap;
          word-break: break-word;

          scrollbar-width: thin;
          scrollbar-color: rgba(255, 255, 255, 0.35) transparent;
          &::-webkit-scrollbar {
            width: 6px;
          }
          &::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.35);
            border-radius: 3px;
          }
        }
      }

      &::after {
        content: '';
        position: absolute;
        top: 0; left: 0; width: 100%; height: 100%;
        background: linear-gradient(to top, rgba(0,0,0,0.72) 0%, rgba(0,0,0,0.25) 45%, transparent 100%);
        pointer-events: none;
        z-index: 1;
      }
    }
  }

  .content-section {
    max-width: var(--exhibit-inner-max);
    margin: 0 auto;
    padding: clamp(10px, 1.5vh, 18px) var(--exhibit-inner-pad);
    box-sizing: border-box;

    .section-header {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: flex-end;
      gap: 8px 16px;
      margin-bottom: 10px;
      padding-bottom: 6px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);

      .section-title {
        margin: 0;
        font-size: clamp(17px, 1.35vw, 22px);
        font-weight: 900;
        line-height: 1.2;
        color: #000;
      }

      .section-title-en {
        display: inline-block;
        margin-left: 8px;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 0.1em;
        color: #888;
        vertical-align: middle;
      }

      /* 分类筛选：恢复原文本链 + 下划线激活态 */
      .filter-tabs {
        span {
          margin-left: 20px;
          cursor: pointer;
          font-size: 14px;
          color: #666;
          transition: color 0.3s;

          &:hover {
            color: #000;
          }

          &.active {
            color: #000;
            font-weight: bold;
            text-decoration: underline;
          }
        }
      }
    }
  }

  .poizon-card {
    background: #fff;
    margin-bottom: 22px;
    cursor: pointer;
    transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    border: 1px solid #f0f0f0;

    &:hover,
    &:focus-visible {
      transform: translateY(-6px);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);

      .card-image-wrapper :deep(img) {
        transform: scale(1.05);
      }
    }

    &:hover .card-image-wrapper .card-hover-hint,
    &:focus-visible .card-image-wrapper .card-hover-hint {
      opacity: 1;
      visibility: visible;
      transition:
        opacity 0.32s ease,
        visibility 0s linear 0s;
    }

    &:hover .card-image-wrapper .card-hover-hint__text,
    &:focus-visible .card-image-wrapper .card-hover-hint__text {
      opacity: 1;
      transform: translateY(0) scale(1);
      transition:
        opacity 0.34s ease 0.05s,
        transform 0.38s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.05s;
    }

    &:focus-visible {
      outline: 2px solid rgba(0, 0, 0, 0.55);
      outline-offset: 2px;
    }

    &:focus:not(:focus-visible) {
      outline: none;
    }

    .card-image-wrapper {
      position: relative;
      height: 248px;
      overflow: hidden;
      background-color: #f9f9f9;
      isolation: isolate;

      /* 封面图压在底层，避免盖住叠在上面的提示层 */
      .el-image,
      :deep(.el-image__wrapper) {
        position: relative;
        z-index: 0;
        display: block;
        width: 100%;
        height: 100%;
      }

      :deep(.el-image__inner) {
        position: relative;
        z-index: 0;
        transition: transform 0.35s ease;
      }

      .card-tag {
        position: absolute;
        top: 12px;
        right: 12px;
        z-index: 4;
        background: rgba(0, 0, 0, 0.7);
        color: #fff;
        padding: 3px 8px;
        font-size: 10px;
        backdrop-filter: blur(5px);
      }

      /* 默认隐藏；悬停/聚焦时中央遮罩 +「查看详情」 */
      .card-hover-hint {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        z-index: 3;
        pointer-events: none;
        opacity: 0;
        visibility: hidden;
        transition:
          opacity 0.28s ease,
          visibility 0s linear 0.28s;
        background: rgba(12, 12, 14, 0.48);
        backdrop-filter: blur(6px) saturate(120%);
        -webkit-backdrop-filter: blur(6px) saturate(120%);
        /* 边缘略收光，中间略亮，更柔和 */
        box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
      }

      .card-hover-hint__text {
        color: #fff;
        font-size: clamp(14px, 1.1vw, 16px);
        font-weight: 600;
        letter-spacing: 0.22em;
        padding-left: 0.12em;
        line-height: 1.5;
        opacity: 0;
        transform: translateY(10px) scale(0.96);
        text-shadow: 0 2px 16px rgba(0, 0, 0, 0.45);
        padding-bottom: 6px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.35);
        transition:
          opacity 0.22s ease,
          transform 0.22s ease;
      }
    }

    .card-content {
      padding: 10px 14px 12px;

      .item-name {
        font-size: 16px;
        font-weight: 800;
        margin: 0 0 6px 0;
        color: #000;
        line-height: 1.35;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .item-intro {
        font-size: 12px;
        color: #777;
        line-height: 1.5;
        height: 36px;
        margin: 0;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }
  }

  .pagination-wrapper {
    margin-top: 40px;
    display: flex;
    justify-content: center;
  }
}

/* 15.6 寸级笔记本：收紧纵向留白、略降卡片图高，让「搜索 + 轮播 + 名录标题 + 至少一行卡片」更易同屏出现 */
@media (max-width: 1680px) and (max-height: 940px) {
  .exhibit-container {
    --exhibit-inner-pad: clamp(12px, 2vw, 20px);
    --exhibit-search-overlap: clamp(36px, 6.5vh, 48px);

    .search-section {
      padding: clamp(4px, 0.8vh, 8px) 0;

      .search-bar {
        max-width: min(480px, 94vw);
        padding: 3px 3px 3px 10px;

        .poizon-input :deep(.el-input__wrapper) {
          min-height: 32px;
        }

        .search-icon-btn {
          width: 36px;
          height: 28px;

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }

    .hero-carousel {
      margin-top: calc(-1 * var(--exhibit-search-overlap));
      margin-bottom: clamp(8px, 1vh, 12px);
    }

    .content-section {
      padding: clamp(10px, 1.5vh, 18px) var(--exhibit-inner-pad);

      .section-header {
        margin-bottom: 8px;
        padding-bottom: 5px;
      }
    }

    .poizon-card {
      margin-bottom: 16px;

      .card-image-wrapper {
        height: 200px;
      }

      .card-content {
        padding: 10px 12px 8px;
      }
    }

    .pagination-wrapper {
      margin-top: 24px;
    }
  }
}

@media (max-width: 1440px) and (max-height: 820px) {
  .exhibit-container {
    .hero-carousel .carousel-content .carousel-info {
      bottom: clamp(8px, 1.5vh, 20px);
      max-height: min(52%, calc(100% - 12px));

      .title {
        font-size: clamp(18px, 2.8vw, 28px);
        margin: 6px 0 4px;
      }

      .desc {
        font-size: clamp(12px, 1.2vw, 14px);
        line-height: 1.45;
      }
    }

    .poizon-card .card-image-wrapper {
      height: 200px;
    }

  }
}
</style>

