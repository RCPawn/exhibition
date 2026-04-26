<template>
  <div class="trinity-detail-container" v-loading="loading">
    <main class="trinity-main">
      <!-- 第一轴：3D 展示区 -->
      <section class="axis-3d">
        <!-- 【修复点 1】将原本突兀的顶部栏重构为悬浮控件，消除后台“双导航栏”的错觉 -->
        <div class="floating-nav">
          <div class="nav-back-block" @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回 / BACK</span>
          </div>
          <transition name="el-fade-in">
            <span v-if="isCollected" class="collected-status-tag">★ 已收藏展品</span>
          </transition>
        </div>

        <model-viewer
            v-if="form.modelFile"
            :src="getAssetUrl(form.modelFile)"
            auto-rotate
            camera-controls
            shadow-intensity="1.5"
            environment-image="neutral"
            exposure="1"
            camera-orbit="0deg 75deg 250%"
            min-camera-orbit="auto auto 150%"
            max-camera-orbit="auto auto 500%"
            class="viewer-instance"
        >
          <div slot="poster" class="model-loading">
            <div class="spinner"></div>
            <p class="loading-text">SYNCING DATA</p>
          </div>
          <div slot="progress-bar" style="display: none;"></div>
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
            <span class="category-label">/ {{ categoryFormat(form.categoryId) }}</span>
            <h1 class="main-title">{{ form.itemName }}</h1>
          </div>

          <!-- 控制台 -->
          <div class="power-console">
            <div class="stats-container">
              <div class="stat-item view-stat">
                <svg viewBox="0 0 24 24" class="eye-icon">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                </svg>
                <span class="num">{{ form.viewCount || 0 }}</span>
              </div>
              
              <button class="action-btn like-btn" :class="{ 'is-active': isLiked }" @click="handleAction(2)">
                <svg viewBox="0 0 24 24" class="heart-icon">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                <span class="count">{{ form.likeCount || 0 }}</span>
              </button>

              <button class="action-btn collect-btn" :class="{ 'is-active': isCollected }" @click="handleAction(3)">
                <svg viewBox="0 0 24 24" class="star-icon">
                  <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                </svg>
                <span class="count">{{ form.favoriteCount || 0 }}</span>
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
import { listCategory } from "@/api/heritage/category";
import { ArrowLeft, Star, StarFilled, Pointer, View } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import defAva from '@/assets/images/profile.jpg';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const loading = ref(true);
const commentLoading = ref(false);
const submitting = ref(false);
const form = ref({});
const categoryOptions = ref([]);

const isLiked = ref(false);
const isCollected = ref(false);

const newComment = ref('');
const commentList = ref([]);
const totalComments = ref(0);

const getAssetUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http') || url.startsWith('blob')) return url;
  return import.meta.env.VITE_APP_BASE_API + url;
};

const getAvatarUrl = (url) => {
  if (!url) return defAva;
  return import.meta.env.VITE_APP_BASE_API + url;
};

function categoryFormat(categoryId) {
  if (!categoryId || categoryOptions.value.length === 0) return 'GENERAL';
  const category = categoryOptions.value.find(item => String(item.categoryId) === String(categoryId));
  return category ? category.categoryName : 'GENERAL';
}

const getCategoryList = async () => {
  try {
    const res = await listCategory({ pageNum: 1, pageSize: 100 });
    categoryOptions.value = res.rows;
  } catch (err) {
    console.error("获取分类失败", err);
  }
};

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
  getCategoryList();
  loadDetail();
  loadComments();
});
</script>

<style lang="scss" scoped>
/* 【修复点 2】锁定容器绝对边界，移除破坏若依布局的 margin: -20px，防止挤压左侧菜单 */
.trinity-detail-container {
  position: relative;
  width: 100%;
  height: calc(100vh - var(--layout-navbar-height, 60px) - var(--layout-tags-height, 34px));
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

.trinity-main {
  flex: 1;
  width: 100%;
  display: flex;
  overflow: hidden;

  /* 3D区 */
  .axis-3d {
    flex: 0 0 clamp(35%, 40vw, 42%); // 响应式宽度,防止窄屏过窄
    background: #f8f8f8;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    /* 悬浮控制组件 */
    .floating-nav {
      position: absolute;
      top: 25px;
      left: 25px;
      right: 25px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      z-index: 100;
      pointer-events: none; /* 让空白处鼠标穿透，不影响 3D 旋转 */

      .nav-back-block {
        pointer-events: auto; /* 按钮恢复点击 */
        background: #000;
        color: #fff;
        height: 38px;
        padding: 0 20px;
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        font-size: 11px;
        font-weight: 900;
        letter-spacing: 2px;
        transition: background 0.2s;
        &:hover { background: #333; }
      }

      .collected-status-tag {
        pointer-events: auto;
        background: #ffd700;
        color: #000;
        font-size: 10px;
        padding: 6px 14px;
        font-weight: 900;
        letter-spacing: 1.5px;
        box-shadow: 4px 4px 0 #000;
      }
    }

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
    flex: 0 0 clamp(32%, 38vw, 40%); // 响应式宽度
    border-right: 1px solid #eee;
    overflow: hidden;
    .scroll-wrapper {
      height: 100%;
      overflow-y: auto;
      padding: clamp(30px, 4vh, 50px) clamp(25px, 3vw, 45px); // 响应式内边距
      &::-webkit-scrollbar { width: 4px; }
      &::-webkit-scrollbar-thumb { background: #000; }
    }
  }

  /* 讨论区 */
  .axis-social {
    flex: 1;
    min-width: 280px; // 降低最小宽度,适配小屏
    background: #fcfcfc;
    overflow: hidden;
    .social-wrapper {
      height: 100%;
      overflow-y: auto;
      padding: clamp(30px, 4vh, 50px) clamp(20px, 2.5vw, 35px); // 响应式内边距
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
          word-break: break-all;
        }
      }
      .empty-comments { text-align: center; color: #ccc; font-size: 12px; margin-top: 50px; }
    }
  }
}

.hero-title-box {
  margin-bottom: clamp(25px, 3vh, 35px);
  .category-label { 
    font-size: clamp(10px, 1vw, 11px); // 响应式字体
    color: #888; 
    font-weight: 500; 
    letter-spacing: 1px;
    display: inline-block;
    padding: 3px 10px;
    background: #f5f5f5;
    border-radius: 12px;
  }
  .main-title { 
    font-size: clamp(24px, 3vw, 36px); // 响应式标题
    font-weight: 700; 
    margin: 15px 0 8px 0; 
    line-height: 1.3; 
    color: #1a1a1a;
  }
}

.power-console {
  margin-bottom: 40px;
  .stats-container {
    display: flex; 
    align-items: center; 
    gap: 20px;
    padding: 16px 0;
    
    .stat-item.view-stat {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-right: 20px;
      border-right: 1px solid #eee;
      
      svg {
        width: 24px;
        height: 24px;
        fill: #999;
        transition: all 0.3s ease;
      }
      
      &:hover svg {
        fill: #666;
        transform: scale(1.1);
      }
      
      .num { 
        font-size: 18px; 
        font-weight: 600; 
        color: #666;
      }
    }
    
    .action-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 14px;
      background: transparent;
      border: none;
      cursor: pointer;
      transition: all 0.3s ease;
      border-radius: 20px;
      
      svg {
        width: 24px;
        height: 24px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }
      
      .count {
        font-size: 16px;
        font-weight: 600;
        color: #666;
        transition: all 0.3s ease;
      }
      
      &:hover {
        background: rgba(0, 0, 0, 0.04);
        transform: scale(1.05);
      }
      
      &.like-btn {
        .heart-icon {
          fill: none;
          stroke: #999;
          stroke-width: 2;
        }
        
        &:hover .heart-icon {
          stroke: #ff4757;
        }
        
        &.is-active {
          .heart-icon {
            fill: #ff4757;
            stroke: #ff4757;
            animation: heartBeat 0.5s ease-in-out;
          }
          
          .count {
            color: #ff4757;
          }
        }
      }
      
      &.collect-btn {
        .star-icon {
          fill: none;
          stroke: #999;
          stroke-width: 2;
        }
        
        &:hover .star-icon {
          stroke: #ffa502;
        }
        
        &.is-active {
          .star-icon {
            fill: #ffa502;
            stroke: #ffa502;
            animation: starTwinkle 0.5s ease-in-out;
          }
          
          .count {
            color: #ffa502;
          }
        }
      }
    }
  }
}

.info-block {
  margin-bottom: clamp(35px, 4vh, 55px); // 响应式间距
  .block-label { 
    font-size: clamp(16px, 1.8vw, 18px); // 响应式字体
    font-weight: 700; 
    letter-spacing: 1.5px; 
    margin-bottom: clamp(18px, 2vh, 25px); // 响应式间距
    display: flex; 
    align-items: center;
    color: #2c3e50;
    
    &::before { 
      content: ''; 
      width: 5px; 
      height: 22px; 
      background: linear-gradient(to bottom, #667eea, #764ba2); 
      margin-right: 14px;
      border-radius: 3px;
    }
  }
  .p-desc, .rich-text { 
    font-size: clamp(14px, 1.4vw, 15px); // 响应式字体
    line-height: 2.2; 
    color: #555; 
    padding-left: 19px;
    text-align: justify;
  }
}

@keyframes heartBeat {
  0%, 100% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(1.1); }
  75% { transform: scale(1.4); }
}

@keyframes starTwinkle {
  0%, 100% { transform: scale(1) rotate(0deg); }
  25% { transform: scale(1.3) rotate(-15deg); }
  50% { transform: scale(1.1) rotate(15deg); }
  75% { transform: scale(1.4) rotate(-10deg); }
}

@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(0,0,0,0.1); } 70% { box-shadow: 0 0 0 10px rgba(0,0,0,0); } 100% { box-shadow: 0 0 0 0 rgba(0,0,0,0); } }
@keyframes rotate { to { transform: rotate(360deg); } }
.spinner { width: 24px; height: 24px; border: 2px solid #eee; border-top-color: #000; border-radius: 50%; animation: rotate 1s linear infinite; }
</style>