<template>
  <div class="app-container collection-container">
    <div class="page-header">
      <h2 class="title">我的收藏</h2>
      <span class="sub-title">共 {{ total }} 件藏品</span>
    </div>

    <div v-loading="loading" class="collection-grid">
      <el-row :gutter="24" v-if="collectionList.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in collectionList" :key="item.itemId">
          <div class="heritage-card" @click="handleDetail(item.itemId)">
            <div class="card-image">
              <img :src="getAssetUrl(item.coverImage)" alt="cover" />
              <div class="hover-overlay">
                <span class="view-btn">VIEW DETAIL</span>
              </div>
            </div>
            <div class="card-info">
              <h3 class="item-name">{{ item.itemName }}</h3>
              <div class="item-stats">
                <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                <span class="highlight"><el-icon><StarFilled /></el-icon> {{ item.favoriteCount }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无收藏记录" :image-size="200"></el-empty>
        <el-button type="primary" color="#000" @click="router.push('/heritage/gallery')">去展厅逛逛</el-button>
      </div>
    </div>

    <!-- 分页：增加外层容器以实现居中 -->
    <div class="pagination-wrapper">
      <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { listMyCollection } from "@/api/heritage/heritage_manage.js";
import { View, StarFilled } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(true);
const collectionList = ref([]);
const total = ref(0);
const queryParams = ref({
  pageNum: 1,
  pageSize: 12,
});

const getAssetUrl = (url) => {
  if (!url) return '';
  return import.meta.env.VITE_APP_BASE_API + url;
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listMyCollection(queryParams.value);
    collectionList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleDetail = (id) => {
  router.push("/display/detail/" + id);
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>

/* 强制底部分页容器居中 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

/* 收藏列表 */
.collection-container {
  padding: 30px;
  background-color: #f9f9f9;
  min-height: calc(100vh - var(--layout-navbar-height, 60px) - var(--layout-tags-height, 34px));
}

.page-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  display: flex;
  align-items: baseline;
  gap: 15px;

  .title {
    font-size: 28px;
    font-weight: 900;
    letter-spacing: 1px;
    margin: 0;
  }
  .sub-title {
    font-size: 14px;
    color: #666;
    font-weight: bold;
  }
}

.heritage-card {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.08);
    border-color: #eee;

    .card-image .hover-overlay { opacity: 1; }
    .card-image img { transform: scale(1.05); }
  }

  .card-image {
    height: 200px;
    position: relative;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    .hover-overlay {
      position: absolute; inset: 0;
      background: rgba(0,0,0,0.4);
      display: flex; align-items: center; justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;

      .view-btn {
        color: #fff; border: 1px solid #fff; padding: 8px 16px;
        font-size: 12px; font-weight: 900; letter-spacing: 2px;
      }
    }
  }

  .card-info {
    padding: 15px;
    .item-name {
      font-size: 16px; font-weight: bold; margin: 0 0 10px 0;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
    .item-stats {
      display: flex; justify-content: space-between; font-size: 12px; color: #999;
      .highlight { color: #ffd700; display: flex; align-items: center; gap: 4px; }
    }
  }
}

.empty-state {
  display: flex; flex-direction: column; align-items: center; padding-top: 50px;
}
</style>