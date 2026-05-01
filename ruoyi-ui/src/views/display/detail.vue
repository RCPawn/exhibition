<template>
  <div class="trinity-detail-container" v-loading="loading">
    <main class="trinity-main" :class="{ 'social-collapsed': commentsCollapsed }">
      <!-- 第一轴：3D 展示区 -->
      <section class="axis-3d">
        <!-- 【修复点 1】将原本突兀的顶部栏重构为悬浮控件，消除后台“双导航栏”的错觉 -->
        <div class="floating-nav">
          <div class="floating-nav__start">
            <AppBackButton variant="floating" preset="portal" fallback="/display/home" />
          </div>
          <div class="floating-nav__end">
            <transition name="el-fade-in">
              <span v-if="isCollected" class="collected-status-tag">★ 已收藏展品</span>
            </transition>
          </div>
        </div>

        <model-viewer
            v-if="form.modelFile"
            ref="modelViewerRef"
            :src="getAssetUrl(form.modelFile)"
            auto-rotate
            :auto-rotate-delay="2000"
            camera-controls
            shadow-intensity="1.75"
            environment-image="neutral"
            exposure="1.12"
            :camera-orbit="defaultCameraOrbit"
            :min-camera-orbit="minOrbit"
            :max-camera-orbit="maxOrbit"
            field-of-view="30deg"
            interaction-prompt="none"
            class="viewer-instance"
            @load="onModelViewerLoad"
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

      <!-- 第三轴：讨论区（可向右折叠为窄条） -->
      <section class="axis-social">
        <button
          type="button"
          class="social-collapse-handle"
          :title="commentsCollapsed ? '展开讨论区' : '收起讨论区'"
          :aria-expanded="!commentsCollapsed"
          @click="commentsCollapsed = !commentsCollapsed"
        >
          <el-icon class="handle-chevron"><DArrowLeft v-if="commentsCollapsed" /><DArrowRight v-else /></el-icon>
          <span class="handle-label">{{ commentsCollapsed ? '讨论' : '收起' }}</span>
        </button>

        <div class="social-wrapper" :class="{ 'is-hidden': commentsCollapsed }">
          <header class="social-head-row">
            <h4 class="social-header">讨论区 <span class="count-badge">{{ totalComments }}</span></h4>
          </header>

          <div class="comment-composer">
            <el-input
              v-model="newComment"
              type="textarea"
              :placeholder="composerPlaceholder"
              :rows="3"
              resize="none"
              class="composer-input"
              maxlength="200"
            />
            <div class="comment-composer__overlay-tools">
              <span class="comment-composer__count">{{ newComment.length }}/200</span>
              <button
                type="button"
                class="comment-composer__submit"
                @click="handlePostComment"
                :disabled="submitting"
              >
                {{ submitting ? '\u53d1\u9001\u4e2d\u2026' : '\u53d1\u8868\u8bc4\u8bba' }}
              </button>
            </div>
          </div>

          <div class="comment-scroller" v-loading="commentLoading">
            <div v-if="commentList.length > 0" class="comment-list">
              <div class="comment-item" v-for="item in commentList" :key="item.commentId">
                <div class="c-avatar-col">
                  <img :src="getAvatarUrl(item.avatar)" class="u-avatar" alt="" />
                </div>
                <div class="c-body-col">
                  <div class="c-meta">
                    <span class="u-name">{{ item.nickName || '匿名用户' }}</span>
                    <time class="c-time">{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</time>
                  </div>
                  <div class="c-content-box">
                    <p>{{ item.content }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-comments">
              <p class="empty-title">还没有评论</p>
              <p class="empty-hint">欢迎留下第一条看法</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getHeritage_manage, updateHeritageCount } from "@/api/heritage/heritage_manage";
import { listComment, addComment } from "@/api/heritage/comment";
import { listCategory } from "@/api/heritage/category";
import { getToken } from '@/utils/auth';
import { View, DArrowLeft, DArrowRight } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import defAva from '@/assets/images/profile.jpg';
import AppBackButton from '@/components/AppBackButton.vue';

/** 第三段为视距，数值越小画面越近（旧版 250% 过远；与门户 hero 约 102% 同量级） */
const defaultCameraOrbit = '0deg 70deg 105%';
const minOrbit = 'auto auto 68%';
const maxOrbit = 'auto auto 480%';

const route = useRoute();
const router = useRouter();

const composerPlaceholder = '\u5206\u4eab\u4f60\u7684\u89c1\u89e3\u2026'

const modelViewerRef = ref(null);
/** 讨论区向右折叠为窄条时，前两列均分剩余宽度 */
const commentsCollapsed = ref(false);

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
  if (!getToken()) {
    ElMessage.warning('请先登录后再发表评论');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
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
  if ((type === 2 || type === 3) && !getToken()) {
    ElMessage.warning('请先登录后再进行点赞或收藏');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
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

function applyDefaultCamera() {
  const el = modelViewerRef.value;
  if (!el) return;
  el.cameraOrbit = defaultCameraOrbit;
  el.fieldOfView = '30deg';
}

function onModelViewerLoad() {
  nextTick(() => {
    requestAnimationFrame(() => {
      applyDefaultCamera();
    });
  });
}

onMounted(() => {
  getCategoryList();
  loadDetail();
  loadComments();
});
</script>

<style lang="scss" scoped>
/* 门户/后台顶栏一致；不再减 --layout-tags-height，避免门户底部留白 */
.trinity-detail-container {
  --detail-top: var(--layout-navbar-height, 60px);
  position: relative;
  width: 100%;
  min-height: 0;
  height: calc(100vh - var(--detail-top));
  height: calc(100dvh - var(--detail-top));
  max-height: calc(100dvh - var(--detail-top));
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
  box-sizing: border-box;
}

.trinity-main {
  flex: 1;
  min-height: 0;
  width: 100%;
  display: flex;
  overflow: hidden;
  align-items: stretch;

  /* 3D区：与信息区均分剩余空间，评论展开/收起时自适应占满视口 */
  .axis-3d {
    flex: 1 1 0;
    min-height: 0;
    min-width: 0;
    width: 0;
    /* 展厅模型区：纯色底，略偏暖灰，衬托模型、不抢视线 */
    background: #e8e6e3;
    position: relative;
    display: flex;
    align-items: stretch;
    justify-content: center;

    /* 仅左右两块可点，中间区域不拦截 3D 操作，避免误触缩放 */
    .floating-nav {
      position: absolute;
      top: 25px;
      left: 25px;
      right: 25px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      z-index: 10000;
      pointer-events: none;
    }

    .floating-nav__start,
    .floating-nav__end {
      pointer-events: auto;
      flex: 0 0 auto;
      max-width: min(100%, 320px);
    }

    .floating-nav__end {
      display: flex;
      justify-content: flex-end;
    }

    .floating-nav .collected-status-tag {
      background: #ffd700;
      color: #000;
      font-size: 10px;
      padding: 6px 14px;
      font-weight: 900;
      letter-spacing: 1.5px;
      box-shadow: 4px 4px 0 #000;
    }

    .viewer-instance {
      position: relative;
      z-index: 1;
      width: 100%;
      height: 100%;
      outline: none;
      --poster-color: transparent;
    }

    .model-loading {
      width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
      .loading-box { display: flex; flex-direction: column; align-items: center; gap: 15px;
        .loading-text { font-size: 10px; letter-spacing: 3px; color: #999; font-weight: 900; white-space: nowrap; }
      }
    }

    .no-model-state {
      position: relative;
      z-index: 2;
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
      position: absolute;
      z-index: 3;
      bottom: 25px;
      left: 25px;
      display: flex;
      align-items: center;
      gap: 8px;
      pointer-events: none;
      user-select: none;

      span { font-size: 9px; color: #ccc; letter-spacing: 1px; font-weight: bold; }
      .pulse-dot { width: 6px; height: 6px; background: #000; border-radius: 50%; animation: pulse 2s infinite; }
    }
  }

  /* 信息区（中间）：与右侧讨论区同一套底色与分隔线 */
  .axis-info {
    flex: 1 1 0;
    min-height: 0;
    min-width: 0;
    width: 0;
    background: linear-gradient(180deg, #fafbfc 0%, #f3f4f6 100%);
    border-right: 1px solid rgba(15, 23, 42, 0.08);
    overflow: hidden;
    .scroll-wrapper {
      height: 100%;
      overflow-y: auto;
      -webkit-font-smoothing: antialiased;
      padding: clamp(28px, 4vh, 48px) clamp(22px, 3vw, 42px);
      &::-webkit-scrollbar {
        width: 5px;
      }
      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.12);
        border-radius: 4px;
      }
      &::-webkit-scrollbar-track {
        background: transparent;
      }
    }
  }

  /* 讨论区 */
  .axis-social {
    --social-handle-w: 36px;
    flex: 0 0 min(400px, 32vw);
    max-width: 440px;
    min-height: 0;
    min-width: 0;
    background: linear-gradient(180deg, #fafbfc 0%, #f3f4f6 100%);
    border-left: 1px solid rgba(15, 23, 42, 0.08);
    overflow: hidden;
    position: relative;
    display: flex;
    flex-direction: column;
    transition: flex-basis 0.38s cubic-bezier(0.33, 1, 0.68, 1), max-width 0.38s cubic-bezier(0.33, 1, 0.68, 1);

    .social-collapse-handle {
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: var(--social-handle-w);
      z-index: 5;
      border: none;
      padding: 0 4px;
      margin: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      background: rgba(255, 255, 255, 0.55);
      border-right: 1px solid rgba(15, 23, 42, 0.1);
      color: #334155;
      transition: background 0.2s, color 0.2s;

      .handle-chevron {
        font-size: 13px;
      }

      .handle-label {
        font-size: 10px;
        font-weight: 700;
        letter-spacing: 0.18em;
        writing-mode: vertical-rl;
        text-orientation: mixed;
        user-select: none;
      }

      &:hover {
        background: #fff;
        color: #0f172a;
      }
    }

    .social-wrapper {
      flex: 1;
      min-height: 0;
      min-width: 0;
      margin-left: var(--social-handle-w);
      overflow-y: auto;
      overflow-x: hidden;
      padding: clamp(22px, 3.5vh, 40px) clamp(16px, 2vw, 28px) clamp(22px, 3.5vh, 40px) clamp(12px, 1.5vw, 20px);
      transition: opacity 0.28s ease, transform 0.28s ease;

      &::-webkit-scrollbar {
        width: 5px;
      }
      &::-webkit-scrollbar-thumb {
        background: rgba(15, 23, 42, 0.15);
        border-radius: 4px;
      }

      &.is-hidden {
        opacity: 0;
        transform: translateX(12px);
        pointer-events: none;
        visibility: hidden;
        position: absolute;
        inset: 0 0 0 var(--social-handle-w);
        height: 100%;
      }
    }

    .social-head-row {
      margin-bottom: 20px;
    }

    .social-header {
      font-size: 15px;
      font-weight: 700;
      letter-spacing: 0.03em;
      margin: 0;
      color: #1a1a1a;
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .count-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 20px;
        height: 20px;
        padding: 0 6px;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0;
        color: #334155;
        background: transparent;
        border: 1px solid rgba(15, 23, 42, 0.14);
        border-radius: 4px;
      }
    }

    .input-container,
    .comment-composer {
      margin-bottom: 24px;
    }

    .comment-composer {
      position: relative;
      border: 1px solid rgba(15, 23, 42, 0.12);
      border-radius: 8px;
      overflow: hidden;
      background: #fff;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
    }

    .comment-composer .composer-input :deep(.el-textarea__inner) {
      border: none;
      border-radius: 0;
      font-size: 14px;
      line-height: 1.6;
      padding: 12px 14px 46px;
      background: #fff;
      box-shadow: none;
      min-height: 96px;

      &:focus {
        box-shadow: none;
      }
    }

    .comment-composer__overlay-tools {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 10px;
      padding: 6px 8px 8px 10px;
      background: linear-gradient(
        to top,
        rgba(255, 255, 255, 0.98) 40%,
        rgba(255, 255, 255, 0.88) 72%,
        rgba(255, 255, 255, 0)
      );
      pointer-events: none;
    }

    .comment-composer__count {
      font-size: 12px;
      color: #94a3b8;
      font-variant-numeric: tabular-nums;
      margin-right: auto;
      padding-left: 2px;
      pointer-events: auto;
    }

    .comment-composer__submit {
      appearance: none;
      border: none;
      cursor: pointer;
      padding: 5px 14px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      border-radius: 6px;
      background: var(--el-color-primary, #409eff);
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.12);
      transition: filter 0.2s ease, opacity 0.2s ease, transform 0.15s ease;
      pointer-events: auto;

      &:hover:not(:disabled) {
        filter: brightness(1.06);
      }

      &:active:not(:disabled) {
        transform: scale(0.98);
      }

      &:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
    }

    .comment-scroller {
      min-height: 120px;
    }

    .comment-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .comment-item {
      display: flex;
      gap: 10px;
      align-items: flex-start;
      padding: 10px 10px 10px 8px;
      background: #fff;
      border: 1px solid rgba(15, 23, 42, 0.1);
      border-radius: 4px;
      box-shadow: none;
    }

    .c-avatar-col {
      flex-shrink: 0;
    }

    .u-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      object-fit: cover;
      border: 1px solid rgba(15, 23, 42, 0.1);
      display: block;
    }

    .c-body-col {
      flex: 1;
      min-width: 0;
    }

    .c-meta {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: 10px;
      margin-bottom: 8px;
    }

    .u-name {
      font-size: 14px;
      font-weight: 600;
      color: #0f172a;
    }

    .c-time {
      font-size: 12px;
      color: #94a3b8;
      white-space: nowrap;
    }

    .c-content-box {
      font-size: 14px;
      color: #334155;
      line-height: 1.75;
      word-break: break-word;

      p {
        margin: 0;
      }
    }

    .empty-comments {
      text-align: center;
      padding: 28px 14px;
      border-radius: 4px;
      border: 1px dashed rgba(15, 23, 42, 0.14);
      background: rgba(255, 255, 255, 0.5);
    }

    .empty-title {
      margin: 0 0 6px;
      font-size: 14px;
      font-weight: 600;
      color: #64748b;
    }

    .empty-hint {
      margin: 0;
      font-size: 13px;
      color: #94a3b8;
    }
  }

  &.social-collapsed .axis-social {
    flex: 0 0 var(--social-handle-w);
    max-width: var(--social-handle-w);
    min-width: var(--social-handle-w);
    border-left: 1px solid rgba(15, 23, 42, 0.08);
  }
}

.hero-title-box {
  margin-bottom: clamp(25px, 3vh, 35px);
  .category-label { 
    font-size: clamp(10px, 1vw, 11px); // 响应式字体
    color: #6b6b6b; 
    font-weight: 500; 
    letter-spacing: 1px;
    display: inline-block;
    padding: 4px 11px;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(0, 0, 0, 0.06);
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