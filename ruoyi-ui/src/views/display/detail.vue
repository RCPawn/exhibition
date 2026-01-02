<template>
  <div class="trinity-detail-container" v-loading="loading">
    <!-- 顶部导航 -->
    <header class="minimal-nav">
      <div class="nav-back-block" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回展厅</span>
      </div>

      <div class="nav-status">
        <transition name="el-fade-in">
          <span v-if="isCollected" class="collected-status-tag">已收藏展品</span>
        </transition>
      </div>
    </header>

    <main class="trinity-main">
      <!-- 第一轴：3D 展示区 -->
      <section class="axis-3d">
        <model-viewer
            v-if="form.modelFile"
            :src="getAssetUrl(form.modelFile)"
            auto-rotate
            camera-controls
            shadow-intensity="1.5"
            environment-image="neutral"
            exposure="1"
            camera-orbit="0deg 75deg 280%"
            min-camera-orbit="auto auto 150%"
            max-camera-orbit="auto auto 400%"
            class="viewer-instance"
        >
          <div slot="poster" class="model-loading">
            <div class="spinner"></div>
            <p class="loading-text">SYNCING DATA</p>
          </div>
        </model-viewer>

        <div v-else class="no-model-state">
          <div class="fallback-content">
            <div class="placeholder-icon-wrap">
              <img v-if="form.coverImage" :src="getAssetUrl(form.coverImage)" class="cover-hint-img" />
              <div class="icon-overlay"><el-icon><View /></el-icon></div>
            </div>
            <div class="error-msg">
              <span class="main-msg">暂无可用的 3D 模型</span>
            </div>
          </div>
        </div>

        <div class="interaction-guide">
          <span class="pulse-dot"></span>
          <span>拖拽旋转 | 滚动放缩</span>
        </div>
      </section>

      <!-- 第二轴：图文介绍区 -->
      <section class="axis-info">
        <div class="scroll-wrapper">
          <div class="hero-title-box">
            <!-- 修改点：使用 categoryFormat 函数根据 ID 获取名称 -->
            <span class="category-label">/ {{ categoryFormat(form.categoryId) }}</span>
            <h1 class="main-title">{{ form.itemName }}</h1>
          </div>

          <!-- 控制台 -->
          <div class="power-console">
            <div class="stats-container">
              <div class="stat-item">
                <span class="num">{{ form.viewCount || 0 }}</span>
                <span class="lab">浏览</span>
              </div>
              <div class="stat-divider"></div>

              <div class="stat-item">
                <span class="num">{{ form.likeCount || 0 }}</span>
                <span class="lab">点赞</span>
              </div>
              <button class="elegant-action-btn like-btn" :class="{ 'is-active': isLiked }" @click="handleAction(2)">
                <el-icon class="icon"><Pointer /></el-icon>
              </button>

              <div class="stat-divider"></div>

              <div class="stat-item">
                <span class="num">{{ form.favoriteCount || 0 }}</span>
                <span class="lab">收藏</span>
              </div>
              <button class="elegant-action-btn collect-btn" :class="{ 'is-active': isCollected }" @click="handleAction(3)">
                <el-icon class="icon"><StarFilled v-if="isCollected" /><Star v-else /></el-icon>
              </button>
            </div>
          </div>

          <div class="article-content">
            <div class="info-block">
              <h4 class="block-label">展品简介</h4>
              <p class="p-desc">{{ form.description || '暂无描述内容。' }}</p>
            </div>
            <div class="info-block" v-if="form.history">
              <h4 class="block-label">历史渊源</h4>
              <div class="rich-text" v-html="form.history"></div>
            </div>
            <div class="info-block" v-if="form.craft">
              <h4 class="block-label">制作工艺</h4>
              <div class="rich-text" v-html="form.craft"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 第三轴：讨论区 -->
      <section class="axis-social">
        <div class="social-wrapper">
          <h4 class="social-header">讨论区 ({{ totalComments }})</h4>

          <div class="input-container">
            <el-input
                v-model="newComment"
                type="textarea"
                placeholder="分享你的见解..."
                :rows="4"
                resize="none"
                class="poizon-input"
                maxlength="200"
                show-word-limit
            />
            <button class="post-button" @click="handlePostComment" :disabled="submitting">
              {{ submitting ? 'POSTING...' : 'POST' }}
            </button>
          </div>

          <div class="comment-scroller" v-loading="commentLoading">
            <div v-if="commentList.length > 0">
              <div class="comment-item" v-for="item in commentList" :key="item.commentId">
                <div class="c-top">
                  <div class="user-info">
                    <img :src="getAvatarUrl(item.avatar)" class="u-avatar" alt="avatar"/>
                    <span class="u-name">{{ item.nickName || '匿名用户' }}</span>
                  </div>
                  <span class="c-time">{{ parseTime(item.createTime, '{m}-{d} {h}:{i}') }}</span>
                </div>
                <div class="c-content-box">
                  <p>{{ item.content }}</p>
                </div>
              </div>
            </div>
            <div v-else class="empty-comments">
              <span>暂无评论，快来抢沙发</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getHeritage_manage, updateHeritageCount } from "@/api/heritage/heritage_manage";
import { listComment, addComment } from "@/api/heritage/comment";
import { listCategory } from "@/api/heritage/category"; // 引入分类列表API
import { ArrowLeft, Star, StarFilled, Pointer, View } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import defAva from '@/assets/images/profile.jpg';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

// --- 状态定义 ---
const loading = ref(true);
const commentLoading = ref(false);
const submitting = ref(false);
const form = ref({});
const categoryOptions = ref([]); // 存放全部分类

const isLiked = ref(false);
const isCollected = ref(false);

const newComment = ref('');
const commentList = ref([]);
const totalComments = ref(0);

// --- 工具函数 ---
const getAssetUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http') || url.startsWith('blob')) return url;
  return import.meta.env.VITE_APP_BASE_API + url;
};

const getAvatarUrl = (url) => {
  if (!url) return defAva;
  return import.meta.env.VITE_APP_BASE_API + url;
};

/** 【新增】格式化分类：根据 categoryId 匹配列表中的名称 */
function categoryFormat(categoryId) {
  if (!categoryId || categoryOptions.value.length === 0) return 'GENERAL';
  const category = categoryOptions.value.find(item => String(item.categoryId) === String(categoryId));
  return category ? category.categoryName : 'GENERAL';
}

// --- 核心逻辑 ---

/** 获取全部分类列表（用于前端匹配） */
const getCategoryList = async () => {
  try {
    // 传一个较大的 pageSize 确保拿全分类
    const res = await listCategory({ pageNum: 1, pageSize: 100 });
    categoryOptions.value = res.rows;
  } catch (err) {
    console.error("获取分类失败", err);
  }
};

/** 加载详情 */
const loadDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  loading.value = true;
  try {
    const res = await getHeritage_manage(id);
    if (res.data) {
      form.value = res.data;
      isLiked.value = res.data.isLiked !== undefined ? !!res.data.isLiked : !!res.data.liked;
      isCollected.value = res.data.isCollected !== undefined ? !!res.data.isCollected : !!res.data.collected;
      await updateHeritageCount(1, id);
      form.value.viewCount++;
    }
  } finally {
    loading.value = false;
  }
};

/** 加载评论列表 */
const loadComments = async () => {
  const id = route.params.id;
  if(!id) return;
  commentLoading.value = true;
  try {
    const res = await listComment({ itemId: id });
    if(res.rows) {
      commentList.value = res.rows;
      totalComments.value = res.total;
    }
  } finally {
    commentLoading.value = false;
  }
};

/** 提交评论 */
const handlePostComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  submitting.value = true;
  try {
    await addComment({ itemId: route.params.id, content: newComment.value });
    ElMessage.success("评论发表成功");
    newComment.value = '';
    loadComments();
  } finally {
    submitting.value = false;
  }
};

/** 点赞/收藏 */
const handleAction = async (type) => {
  const id = route.params.id;
  try {
    await updateHeritageCount(type, id);
    if (type === 2) {
      isLiked.value = !isLiked.value;
      isLiked.value ? form.value.likeCount++ : form.value.likeCount = Math.max(0, form.value.likeCount - 1);
    } else if (type === 3) {
      isCollected.value = !isCollected.value;
      isCollected.value ? form.value.favoriteCount++ : form.value.favoriteCount = Math.max(0, form.value.favoriteCount - 1);
    }
  } catch (err) { console.error(err); }
};

onMounted(() => {
  getCategoryList(); // 先获取列表用于匹配
  loadDetail();
  loadComments();
});
</script>
<style lang="scss" scoped>
/* 容器布局锁死 */
.trinity-detail-container {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
  margin: -20px;
}

/* 导航条 */
.minimal-nav {
  height: 68px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 35px;
  border-bottom: 2px solid #000;
  background: #fff;
  z-index: 2500;

  .nav-back-block {
    background: #000;
    color: #fff;
    height: 40px;
    padding: 0 22px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    font-size: 11px;
    font-weight: 900;
    letter-spacing: 2px;
    margin-top: 2px;
    transition: background 0.2s;
    &:hover { background: #333; }
  }

  .nav-status .collected-status-tag {
    background: #ffd700;
    color: #000;
    font-size: 10px;
    padding: 5px 14px;
    font-weight: 900;
    letter-spacing: 1.5px;
    box-shadow: 4px 4px 0 #000;
  }
}

.trinity-main {
  flex: 1;
  display: flex;
  overflow: hidden;

  /* 3D区 */
  .axis-3d {
    flex: 0 0 38%;
    background: #f8f8f8;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    .viewer-instance { width: 100%; height: 100%; outline: none; }

    .model-loading {
      width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
      .loading-box { display: flex; flex-direction: column; align-items: center; gap: 15px;
        .loading-text { font-size: 10px; letter-spacing: 3px; color: #999; font-weight: 900; white-space: nowrap; }
      }
    }

    .no-model-state {
      width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
      .fallback-content {
        display: flex; flex-direction: column; align-items: center; gap: 20px;
        .placeholder-icon-wrap {
          position: relative; width: 180px; height: 180px;
          .cover-hint-img { width: 100%; height: 100%; object-fit: cover; filter: grayscale(1) opacity(0.3); border: 1px solid #f0f0f0; }
          .icon-overlay { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 35px; color: #ddd; }
        }
        .error-msg { text-align: center; .main-msg { font-size: 13px; color: #bbb; font-weight: 900; letter-spacing: 2px; } }
      }
    }

    .interaction-guide {
      position: absolute; bottom: 25px; left: 25px; display: flex; align-items: center; gap: 8px;
      span { font-size: 9px; color: #ccc; letter-spacing: 1px; font-weight: bold; }
      .pulse-dot { width: 6px; height: 6px; background: #000; border-radius: 50%; animation: pulse 2s infinite; }
    }
  }

  /* 信息区 */
  .axis-info {
    flex: 0 0 37%;
    border-right: 1px solid #eee;
    overflow: hidden;
    .scroll-wrapper {
      height: 100%;
      overflow-y: auto;
      padding: 50px 45px;
      &::-webkit-scrollbar { width: 4px; }
      &::-webkit-scrollbar-thumb { background: #000; }
    }
  }

  /* 讨论区 */
  .axis-social {
    flex: 1;
    min-width: 320px;
    background: #fcfcfc;
    overflow: hidden;
    .social-wrapper {
      height: 100%;
      overflow-y: auto;
      padding: 50px 35px;
      &::-webkit-scrollbar { width: 0; }
    }
    .social-header { font-size: 12px; font-weight: 900; letter-spacing: 2px; margin-bottom: 30px; color: #000; }

    .input-container {
      margin-bottom: 45px;
      .poizon-input :deep(.el-textarea__inner) { border-radius: 0; border: 2px solid #eee; font-size: 13px; &:focus { border-color: #000; } }
      .post-button {
        width: 100%; height: 50px; background: #000; color: #fff; border: none; font-weight: 900; letter-spacing: 3px; margin-top: 10px; cursor: pointer; transition: opacity 0.2s;
        &:hover { opacity: 0.8; }
        &:disabled { opacity: 0.5; cursor: not-allowed; }
      }
    }

    /* 评论项美化 */
    .comment-scroller {
      .comment-item {
        margin-bottom: 30px;
        .c-top {
          display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding: 0 4px;
          .user-info {
            display: flex; align-items: center; gap: 8px;
            .u-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; border: 1px solid #eee; }
            .u-name { font-size: 12px; font-weight: 900; color: #000; }
          }
          .c-time { font-size: 10px; color: #ccc; font-weight: bold; letter-spacing: 0.5px; }
        }
        .c-content-box {
          font-size: 14px; color: #333; line-height: 1.8; padding: 16px;
          background: #fff; border: 1px solid #f0f0f0; border-radius: 2px;
          box-shadow: 0 4px 12px rgba(0,0,0,0.02);
          word-break: break-all; /* 防止长文本撑破布局 */
        }
      }
      .empty-comments {
        text-align: center; color: #ccc; font-size: 12px; margin-top: 50px;
      }
    }
  }
}

/* 标题 & 控制台 */
.hero-title-box {
  margin-bottom: 45px;
  .category-label { font-size: 12px; color: #bbb; font-weight: 900; letter-spacing: 2px; }
  .main-title { font-size: 52px; font-weight: 900; margin: 15px 0; line-height: 1.1; letter-spacing: -3px; }
}

.power-console {
  margin-bottom: 55px;
  .stats-container {
    display: flex; align-items: center; gap: 20px; padding: 22px 0; border-top: 1px solid #eee; border-bottom: 1px solid #eee;
    .stat-item { display: flex; flex-direction: column; min-width: 60px;
      .num { font-size: 26px; font-weight: 900; color: #000; line-height: 1; }
      .lab { font-size: 9px; color: #ccc; font-weight: 900; margin-top: 5px; }
    }
    .stat-divider { width: 1px; height: 30px; background: #eee; }
    .elegant-action-btn {
      width: 42px; height: 42px; background: #fff; border: 2px solid #000; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s;
      .icon { font-size: 18px; color: #000; }
      &:hover { transform: translateY(-2px); }
      &.is-active { background: #000; .icon { color: #fff; } }
      &.collect-btn.is-active .icon { color: #ffd700; }
    }
  }
}

.info-block {
  margin-bottom: 55px;
  .block-label { font-size: 17px; font-weight: 900; letter-spacing: 1.5px; margin-bottom: 25px; display: flex; align-items: center;
    &::before { content: ''; width: 4px; height: 18px; background: #000; margin-right: 14px; }
  }
  .p-desc, .rich-text { font-size: 15px; line-height: 2.2; color: #444; padding-left: 18px; }
}

@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(0,0,0,0.1); } 70% { box-shadow: 0 0 0 10px rgba(0,0,0,0); } 100% { box-shadow: 0 0 0 0 rgba(0,0,0,0); } }
@keyframes rotate { to { transform: rotate(360deg); } }
.spinner { width: 24px; height: 24px; border: 2px solid #eee; border-top-color: #000; border-radius: 50%; animation: rotate 1s linear infinite; }
</style>