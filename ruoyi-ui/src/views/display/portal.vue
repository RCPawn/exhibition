<template>
  <div class="hero-page" v-loading="loading">
    <div class="bg-grid"></div>

    <main class="hero-content" v-if="!loading">
      <!-- 左侧：文字排版 -->
      <section class="text-side">
        <div class="category-tag">数字孪生 / 馆藏档案</div>
        <h1 class="main-title">
          <span class="highlight">白族</span> 非遗
        </h1>
        <p class="description">
          实时监测全站 {{ stats.totalItems }} 项数字化资产。<br />
          当前为您呈现的是累计热度 {{ stats.totalViews }} 次的镇馆之宝。
        </p>
        <div class="cta-area">
          <button class="industrial-btn" @click="router.push('/display/gallery')">进入展厅</button>
        </div>
      </section>

      <!-- 右侧：3D 核心展示 (观看模式) -->
      <section class="model-side">
        <div class="model-container">
          <!-- 核心设置：disable-zoom(禁缩放), disable-pan(禁平移), interaction-prompt="none" -->
          <model-viewer
              v-if="stats.centerModelUrl"
              :src="getAssetUrl(stats.centerModelUrl)"
              auto-rotate
              camera-controls
              disable-zoom
              disable-pan
              interaction-prompt="none"
              shadow-intensity="2"
              exposure="1.2"
              camera-orbit="45deg 75deg 105%"
              class="hero-model"
          >
            <!-- 隐藏加载进度条 -->
            <div slot="progress-bar" style="display: none;"></div>
          </model-viewer>

          <div class="stage-ring"></div>

          <div class="model-label">
            <span class="badge">热度最高</span>
            <h3>{{ stats.centerModelName }}</h3>
          </div>
        </div>
      </section>
    </main>

    <footer class="hero-footer">
      <div class="stat-group">
        <div class="stat-item"><span class="num">{{ stats.totalItems }}</span><span class="lab">核心门类</span></div>
        <div class="stat-divider"></div>
        <div class="stat-item"><span class="num">{{ stats.totalViews }}</span><span class="lab">数据采集</span></div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

const router = useRouter();
const loading = ref(true);
const stats = ref({});

const getAssetUrl = (url) => import.meta.env.VITE_APP_BASE_API + url;

// 获取大屏数据，同步给首页
const getStats = () => {
  request({ url: '/heritage/stats/dashboard', method: 'get' }).then(res => {
    stats.value = res.data;
    loading.value = false;
  });
};

onMounted(getStats);
</script>

<style lang="scss" scoped>
// 核心布局样式
.hero-page {
  height: calc(100vh - 70px);
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

// 背景网格样式
.bg-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(0,0,0,0.06) 1px, transparent 1px);
  background-size: 40px 40px;
}

// 内容区域
.hero-content {
  flex: 1;
  display: flex;
  max-width: 1500px;
  margin: 0 auto;
  align-items: center;
  width: 100%;
  padding: 0 80px;
  min-height: 0;
  z-index: 1;
}

// 文字侧区域
.text-side {
  flex: 1;
  .category-tag {
    font-size: 12px;
    font-weight: 900;
    color: #bbb;
    letter-spacing: 3px;
    margin-bottom: 30px;
  }
  .main-title {
    font-size: 90px;
    font-weight: 900;
    line-height: 1;
    letter-spacing: -4px;
    color: #000;
    margin-bottom: 25px;
    .highlight { color: #339af0; }
  }
  .description {
    font-size: 16px;
    color: #888;
    line-height: 2;
    margin-bottom: 60px;
    max-width: 450px;
  }
}

// 模型侧区域（整合优化后的尺寸）
.model-side {
  flex: 1.4;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;

  .model-container {
    width: 800px;
    height: 600px;
    position: relative;

    .hero-model {
      width: 100%;
      height: 100%;
      outline: none;
      z-index: 10;
      /* 隐藏 model-viewer 内部进度条的关键 CSS 变量 */
      --progress-bar-height: 0px;
      --progress-bar-color: transparent;
      --poster-color: transparent;
    }

    .stage-ring {
      position: absolute;
      bottom: 10%;
      left: 50%;
      transform: translateX(-50%) rotateX(75deg);
      width: 500px;
      height: 500px;
      border: 2px dashed #ddd;
      border-radius: 50%;
      z-index: 1;
    }

    // 模型标签
    .model-label {
      position: absolute;
      bottom: 20px;
      right: 0;
      text-align: right;
      h3 {
        font-size: 28px;
        font-weight: 900;
        margin: 10px 0 0;
        letter-spacing: 2px;
      }
      .badge {
        background: #000;
        color: #fff;
        padding: 2px 10px;
        font-size: 10px;
        font-weight: 900;
      }
    }
  }
}

// 工业风按钮
.industrial-btn {
  background: #000;
  color: #fff;
  border: none;
  padding: 20px 60px;
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s;
  &:hover {
    transform: translate(-5px, -5px);
    box-shadow: 10px 10px 0px #ffd700;
  }
}

// 页脚区域（优化后的高度和间距）
.hero-footer {
  height: 140px;
  flex-shrink: 0;
  border-top: 1px solid #f0f0f0;
  padding: 0 80px;
  display: flex;
  align-items: center;
  background: #fff;

  .stat-group {
    display: flex;
    gap: 60px;
    .stat-item {
      display: flex;
      flex-direction: column;
      .num { font-size: 32px; font-weight: 900; line-height: 1; }
      .lab { font-size: 11px; color: #999; margin-top: 10px; font-weight: 900; }
    }
    .stat-divider { width: 1px; height: 35px; background: #eee; }
  }
}

// 强制隐藏 model-viewer 内部进度条
:deep(model-viewer.hero-model) {
  &::part(default-progress-bar) {
    display: none !important;
  }
}
</style>