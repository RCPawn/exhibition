<!--
<template>
  <div class="exhibit-container">
    &lt;!&ndash; 1. 顶部搜索栏：吸顶效果 &ndash;&gt;
    <div class="search-section">
      <div class="search-bar">
        <el-input
            v-model="queryParams.itemName"
            placeholder="搜索白族非遗瑰宝..."
            prefix-icon="Search"
            clearable
            @keyup.enter="handleQuery"
            class="poizon-input"
        />
        <el-button type="primary" class="poizon-btn" @click="handleQuery">探索</el-button>
      </div>
    </div>

    &lt;!&ndash; 2. 沉浸式轮播图：占据大比例视觉 &ndash;&gt;
    <div class="hero-carousel">
      <el-carousel :interval="5000" height="450px" indicator-position="outside">
        <el-carousel-item v-for="item in bannerList" :key="item.id">
          <div class="carousel-content">
            <img :src="getAssetUrl(item.coverImage)" class="carousel-img" />
            <div class="carousel-info">
              <span class="tag">今日推荐</span>
              <h2 class="title">{{ item.itemName }}</h2>
              <p class="desc">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    &lt;!&ndash; 3. 非遗文化列表：极简工业风卡片 &ndash;&gt;
    <div class="content-section">
      <div class="section-header">
        <h3 class="section-title">白族非遗名录 / <span>HERITAGE LIST</span></h3>
        <div class="filter-tabs">
          <span :class="{ active: !queryParams.categoryId }" @click="filterByCategory(null)">全部</span>
          &lt;!&ndash; 分类：1-技艺, 2-服饰, 3-建筑, 4-活动 &ndash;&gt;
          <span v-for="cat in categoryList" :key="cat.id"
                :class="{ active: queryParams.categoryId === cat.id }"
                @click="filterByCategory(cat.id)">
            {{ cat.name }}
          </span>
        </div>
      </div>

      <el-row :gutter="25" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in heritageList" :key="item.itemId">
          <div class="poizon-card" @click="goToDetail(item.itemId)">
            <div class="card-image-wrapper">
              <el-image :src="getAssetUrl(item.coverImage)" fit="cover" lazy>
                <template #placeholder>
                  <div class="image-slot">加载中...</div>
                </template>
              </el-image>
              <div class="card-tag">{{ item.categoryName }}</div>
            </div>
            <div class="card-content">
              <h4 class="item-name">{{ item.itemName }}</h4>
              <p class="item-intro">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
              <div class="card-footer">
                <span class="view-more">查看详情</span>
                <el-icon><Right /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      &lt;!&ndash; 分页 &ndash;&gt;
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
  </div>
</template>

<script setup name="Exhibit">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listHeritage_manage } from "@/api/heritage/heritage_manage";
import { Plus, Right, Search } from "@element-plus/icons-vue"; // 补充图标引入

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

// -&#45;&#45; 数据响应式变量 -&#45;&#45;
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

// -&#45;&#45; 修改部分：添加排序参数 -&#45;&#45;
const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  itemName: undefined,
  categoryId: undefined,
  // 核心修改：指定排序字段和顺序
  orderByColumn: 'sort_order', // 数据库中的排序字段名
  isAsc: 'asc'                 // 'asc'=升序(小到大), 'desc'=降序
});

// -&#45;&#45; 方法 -&#45;&#45;

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

/** 跳转详情 */
const goToDetail = (id) => {
  router.push("/exhibit/detail/" + id);
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
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 50px;

  .search-section {
    position: sticky;
    top: 0;
    z-index: 100;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    padding: 20px 0;
    display: flex;
    justify-content: center;
    border-bottom: 1px solid #eee;

    .search-bar {
      display: flex;
      width: 600px;
      gap: 10px;

      .poizon-input {
        :deep(.el-input__wrapper) {
          border-radius: 0;
          box-shadow: none;
          background-color: #f5f5f5;
          border: 1px solid transparent;
          transition: all 0.3s;
          &.is-focus {
            border-color: #000;
            background-color: #fff;
          }
        }
      }

      .poizon-btn {
        background-color: #000;
        border: none;
        border-radius: 0;
        padding: 0 30px;
        font-weight: bold;
      }
    }
  }

  .hero-carousel {
    max-width: 1400px;
    margin: 20px auto;
    padding: 0 20px;

    .carousel-content {
      position: relative;
      height: 100%;
      overflow: hidden;

      .carousel-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 1s;
      }

      .carousel-info {
        position: absolute;
        left: 50px;
        bottom: 50px;
        color: #fff;
        z-index: 2;

        .tag {
          background: #00ffcc;
          color: #000;
          padding: 4px 12px;
          font-weight: 900;
          font-size: 12px;
          text-transform: uppercase;
        }

        .title {
          font-size: 42px;
          margin: 15px 0;
          text-shadow: 0 2px 10px rgba(0,0,0,0.5);
        }

        .desc {
          font-size: 16px;
          opacity: 0.9;
        }
      }

      &::after {
        content: '';
        position: absolute;
        top: 0; left: 0; width: 100%; height: 100%;
        background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
      }
    }
  }

  .content-section {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 30px;
      border-bottom: 2px solid #000;
      padding-bottom: 10px;

      .section-title {
        font-size: 24px;
        font-weight: 900;
        span {
          font-size: 14px;
          color: #999;
          margin-left: 10px;
        }
      }

      .filter-tabs {
        span {
          margin-left: 20px;
          cursor: pointer;
          font-size: 14px;
          color: #666;
          transition: color 0.3s;
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
    margin-bottom: 30px;
    cursor: pointer;
    transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    border: 1px solid #f0f0f0;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 15px 30px rgba(0,0,0,0.1);

      .card-image-wrapper img {
        transform: scale(1.05);
      }
    }

    .card-image-wrapper {
      position: relative;
      height: 280px;
      overflow: hidden;
      background-color: #f9f9f9;

      .el-image {
        width: 100%;
        height: 100%;
      }

      .card-tag {
        position: absolute;
        top: 15px;
        right: 15px;
        background: rgba(0,0,0,0.7);
        color: #fff;
        padding: 4px 10px;
        font-size: 11px;
        backdrop-filter: blur(5px);
      }
    }

    .card-content {
      padding: 20px;

      .item-name {
        font-size: 18px;
        margin: 0 0 10px 0;
        color: #000;
      }

      .item-intro {
        font-size: 13px;
        color: #888;
        line-height: 1.6;
        height: 42px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .card-footer {
        margin-top: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #f5f5f5;
        padding-top: 15px;

        .view-more {
          font-size: 12px;
          font-weight: bold;
          letter-spacing: 1px;
        }
      }
    }
  }

  .pagination-wrapper {
    margin-top: 40px;
    display: flex;
    justify-content: center;
  }
}
</style>-->
<template>
  <div class="exhibit-container">
    <!-- 1. 顶部搜索栏：吸顶效果 -->
    <div class="search-section">
      <div class="search-bar">
        <el-input
            v-model="queryParams.itemName"
            placeholder="搜索白族非遗瑰宝..."
            prefix-icon="Search"
            clearable
            @keyup.enter="handleQuery"
            class="poizon-input"
        />
        <el-button type="primary" class="poizon-btn" @click="handleQuery">探索</el-button>
      </div>
    </div>

    <!-- 2. 沉浸式轮播图：高度自适应 -->
    <div class="hero-carousel">
      <!--
         修改点：height 使用动态绑定 bannerHeight
         根据屏幕高度自动调整，防止在笔记本上占满全屏
      -->
      <el-carousel :interval="5000" :height="bannerHeight" indicator-position="outside">
        <el-carousel-item v-for="item in bannerList" :key="item.id">
          <div class="carousel-content">
            <img :src="getAssetUrl(item.coverImage)" class="carousel-img" />
            <div class="carousel-info">
              <span class="tag">今日推荐</span>
              <h2 class="title">{{ item.itemName }}</h2>
              <p class="desc">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 3. 非遗文化列表 -->
    <div class="content-section">
      <div class="section-header">
        <h3 class="section-title">白族非遗名录 / <span>HERITAGE LIST</span></h3>
        <div class="filter-tabs">
          <span :class="{ active: !queryParams.categoryId }" @click="filterByCategory(null)">全部</span>
          <span v-for="cat in categoryList" :key="cat.id"
                :class="{ active: queryParams.categoryId === cat.id }"
                @click="filterByCategory(cat.id)">
            {{ cat.name }}
          </span>
        </div>
      </div>

      <el-row :gutter="25" v-loading="loading">
        <!-- 响应式栅格：在不同尺寸下显示不同数量 -->
        <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4" v-for="item in heritageList" :key="item.itemId">
          <div class="poizon-card" @click="goToDetail(item.itemId)">
            <div class="card-image-wrapper">
              <!--
                 修改点：移除 lazy，防止在某些内嵌滚动容器中出现白图
                 直接加载图片以保证稳定性
              -->
              <el-image :src="getAssetUrl(item.coverImage)" fit="cover">
                <template #placeholder>
                  <div class="image-slot">加载中...</div>
                </template>
              </el-image>
              <div class="card-tag">{{ item.categoryName }}</div>
            </div>
            <div class="card-content">
              <h4 class="item-name">{{ item.itemName }}</h4>
              <p class="item-intro">{{ item.description || '白族传统文化艺术瑰宝，传承百年的非物质文化遗产...' }}</p>
              <div class="card-footer">
                <span class="view-more">查看详情</span>
                <el-icon><Right /></el-icon>
              </div>
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
  </div>
</template>

<script setup name="Exhibit">
import { ref, reactive, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listHeritage_manage } from "@/api/heritage/heritage_manage";
import { Plus, Right, Search } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

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

// 响应式轮播高度
const bannerHeight = ref('450px');

const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  itemName: undefined,
  categoryId: undefined,
  orderByColumn: 'sort_order',
  isAsc: 'asc',
  status: 0
});

// --- 方法 ---

const getAssetUrl = (url) => {
  if (!url) return '';
  return import.meta.env.VITE_APP_BASE_API + url;
};

// 监听窗口大小，动态调整轮播图高度
const handleResize = () => {
  const w = window.innerWidth;
  const h = window.innerHeight;
  // 如果是笔记本屏幕 (宽度<1440 或 高度<800)，减小轮播图高度
  if (w < 1440 || h < 800) {
    bannerHeight.value = '320px';
  } else {
    bannerHeight.value = '450px';
  }
};

const getList = async () => {
  loading.value = true;
  // queryParams.value.status = 0;
  try {
    const res = await listHeritage_manage(queryParams);
    heritageList.value = res.rows;
    total.value = res.total;

    if (bannerList.value.length === 0 && res.rows.length > 0) {
      bannerList.value = res.rows.slice(0, 3);
    }
  } catch (error) {
    console.error("数据加载失败", error);
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const filterByCategory = (id) => {
  queryParams.categoryId = id;
  handleQuery();
};

const goToDetail = (id) => {
  router.push("/display/detail/" + id);
};

onMounted(() => {
  handleResize(); // 初始化执行一次
  window.addEventListener('resize', handleResize);
  getList();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style lang="scss" scoped>
.exhibit-container {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 50px;

  .search-section {
    position: sticky;
    top: 0;
    z-index: 100;
    background: rgba(255, 255, 255, 0.95); /* 加深背景防止透视杂乱 */
    backdrop-filter: blur(10px);
    padding: 20px 0;
    display: flex;
    justify-content: center;
    border-bottom: 1px solid #eee;

    .search-bar {
      display: flex;
      width: 100%;
      max-width: 600px; /* 限制最大宽度 */
      padding: 0 20px; /* 移动端防溢出 */
      gap: 10px;

      .poizon-input {
        :deep(.el-input__wrapper) {
          border-radius: 0;
          box-shadow: none;
          background-color: #f5f5f5;
          border: 1px solid transparent;
          transition: all 0.3s;
          &.is-focus {
            border-color: #000;
            background-color: #fff;
          }
        }
      }

      .poizon-btn {
        background-color: #000;
        border: none;
        border-radius: 0;
        padding: 0 30px;
        font-weight: bold;
      }
    }
  }

  .hero-carousel {
    max-width: 1400px;
    margin: 20px auto;
    padding: 0 20px;

    .carousel-content {
      position: relative;
      height: 100%;
      overflow: hidden;

      .carousel-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 1s;
      }

      .carousel-info {
        position: absolute;
        left: 50px;
        bottom: 50px;
        color: #fff;
        z-index: 2;
        /* 增加文字阴影，防止图片过亮看不清字 */
        text-shadow: 0 2px 8px rgba(0,0,0,0.6);

        .tag {
          background: #00ffcc;
          color: #000;
          padding: 4px 12px;
          font-weight: 900;
          font-size: 12px;
          text-transform: uppercase;
        }

        .title {
          font-size: 42px;
          margin: 15px 0;
          font-weight: 800;
        }

        .desc {
          font-size: 16px;
          opacity: 0.95;
          max-width: 600px;
        }
      }

      &::after {
        content: '';
        position: absolute;
        top: 0; left: 0; width: 100%; height: 100%;
        background: linear-gradient(to top, rgba(0,0,0,0.5), transparent);
      }
    }
  }

  .content-section {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 30px;
      border-bottom: 2px solid #000;
      padding-bottom: 10px;
      flex-wrap: wrap; /* 允许小屏换行 */

      .section-title {
        font-size: 24px;
        font-weight: 900;
        span {
          font-size: 14px;
          color: #999;
          margin-left: 10px;
        }
      }

      .filter-tabs {
        margin-top: 10px;
        span {
          margin-left: 20px;
          cursor: pointer;
          font-size: 14px;
          color: #666;
          transition: color 0.3s;
          &.active {
            color: #000;
            font-weight: bold;
            text-decoration: underline;
          }
          /* 适配小屏点击区域 */
          &:first-child { margin-left: 0; }
        }
      }
    }
  }

  .poizon-card {
    background: #fff;
    margin-bottom: 30px;
    cursor: pointer;
    transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    border: 1px solid #f0f0f0;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 15px 30px rgba(0,0,0,0.1);

      .card-image-wrapper img {
        transform: scale(1.05);
      }
    }

    .card-image-wrapper {
      position: relative;
      /* 默认高度 280px，下面通过 media query 调整 */
      height: 280px;
      overflow: hidden;
      background-color: #f9f9f9;

      .el-image {
        width: 100%;
        height: 100%;
        display: block; /* 消除间隙 */
      }

      .card-tag {
        position: absolute;
        top: 15px;
        right: 15px;
        background: rgba(0,0,0,0.7);
        color: #fff;
        padding: 4px 10px;
        font-size: 11px;
        backdrop-filter: blur(5px);
        z-index: 2;
      }

      .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        color: #999;
        font-size: 12px;
      }
    }

    .card-content {
      padding: 20px;

      .item-name {
        font-size: 18px;
        margin: 0 0 10px 0;
        color: #000;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-intro {
        font-size: 13px;
        color: #888;
        line-height: 1.6;
        height: 42px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .card-footer {
        margin-top: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #f5f5f5;
        padding-top: 15px;

        .view-more {
          font-size: 12px;
          font-weight: bold;
          letter-spacing: 1px;
        }
      }
    }
  }

  .pagination-wrapper {
    margin-top: 40px;
    display: flex;
    justify-content: center;
  }
}

/* === 笔记本屏幕适配 (核心) === */
@media (max-width: 1440px) {
  /* 调整轮播图字体 */
  .hero-carousel .carousel-info {
    left: 30px; bottom: 30px;
    .title { font-size: 32px; margin: 10px 0; }
    .desc { font-size: 14px; max-width: 500px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
  }

  /* 调整卡片图片高度，防止看起来太长 */
  .poizon-card .card-image-wrapper {
    height: 220px;
  }

  .content-section {
    padding-top: 20px;
  }
}
</style>