<template>
  <div class="hero-page" v-loading="loading">
    <!-- 背景纹理 -->
    <div class="bg-pattern"></div>

    <!-- 主内容区 -->
    <section class="hero-section" v-if="!loading">
      <div class="hero-content">
        <!-- 左侧：文字排版 -->
        <div class="text-side">
          <div class="category-tag">数字孪生 / 馆藏档案</div>

          <h1 class="main-title">
            <span class="highlight">白族</span> 非遗
          </h1>

          <p class="description">
            实时监测全站 {{ stats.totalItems ?? 0 }} 项数字化资产，<br />
            传承苍山洱海间的千年文化记忆。
          </p>

          <!-- 统计数据 -->
          <div class="hero-stats">
            <div class="stat-badge">
              <span class="num">{{ stats.totalItems ?? 0 }}</span>
              <span class="lab">展品总数</span>
            </div>
            <div class="stat-badge">
              <span class="num">{{ stats.totalViews ?? 0 }}</span>
              <span class="lab">累计浏览</span>
            </div>
            <div class="stat-badge">
              <span class="num">{{ stats.totalInteractions ?? 0 }}</span>
              <span class="lab">互动数据</span>
            </div>
          </div>

          <div class="cta-area">
            <button class="industrial-btn" @click="router.push('/display/gallery')">
              进入展厅
            </button>
          </div>
        </div>

        <!-- 右侧：3D 核心展示 -->
        <div class="model-side">
          <div class="model-container">
            <div class="model-glow"></div>

            <model-viewer
                v-if="stats.centerModelUrl"
                :src="getAssetUrl(stats.centerModelUrl)"
                auto-rotate
                camera-controls
                disable-zoom
                disable-pan
                interaction-prompt="none"
                shadow-intensity="2"
                exposure="1.15"
                camera-orbit="45deg 74deg 102%"
                class="hero-model"
            >
              <div slot="progress-bar" style="display: none;"></div>
            </model-viewer>

            <div class="stage-ring"></div>

            <div class="model-label">
              <span class="badge">镇馆之宝</span>
              <h3>{{ stats.centerModelName || '白族三合院' }}</h3>
            </div>
          </div>

          <!-- 展品简介 -->
          <div class="model-description" v-if="featuredItem && featuredItem.description">
            <p>{{ featuredItem.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <SiteFooter v-if="!loading" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import SiteFooter from '@/components/SiteFooter.vue';

const router = useRouter();
const loading = ref(true);
const stats = ref({});
const featuredItem = ref(null);

const getAssetUrl = (url) => import.meta.env.VITE_APP_BASE_API + url;

// 获取大屏数据
const getStats = () => {
  request({ url: '/heritage/stats/dashboard', method: 'get' })
      .then(res => {
        stats.value = res.data || {};

        // 获取Top5热门藏品，取第一个作为特色展品
        if (res.data?.top5Items && res.data.top5Items.length > 0) {
          const topItem = res.data.top5Items[0];
          if (topItem.itemId) {
            getFeaturedItem(topItem.itemId);
          }
        }

        loading.value = false;
      })
      .catch(err => {
        console.error('获取首页统计数据失败', err);
        loading.value = false;
      });
};

// 获取特色展品详情
const getFeaturedItem = (itemId) => {
  request({
    url: `/heritage/heritage_manage/${itemId}`,
    method: 'get'
  })
      .then(res => {
        featuredItem.value = res.data;
      })
      .catch(err => {
        console.error('获取展品详情失败', err);
        featuredItem.value = {
          description: '白族传统建筑艺术的杰出代表，凝聚了世代工匠的智慧与技艺，展现了白族文化的独特魅力。'
        };
      });
};

onMounted(() => {
  getStats();
});
</script>

<style lang="scss" scoped>
$bai-cyan: #339af0;
$bai-red: #C62828;
$ink-black: #000000;
$paper-bg: #ffffff;

/* 首页：min-height 与顶栏变量一致，避免顶栏 52px 仍按 60px 扣减出现底部白条 */
.hero-page {
  min-height: calc(100vh - var(--layout-navbar-height, 52px));
  min-height: calc(100dvh - var(--layout-navbar-height, 52px));
  width: 100%;
  background: $paper-bg;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.bg-pattern {
  position: fixed;
  inset: 0;
  background-image:
      radial-gradient(circle at 20% 18%, rgba(51, 154, 240, 0.05) 0%, transparent 38%),
      radial-gradient(circle at 80% 72%, rgba(198, 40, 40, 0.03) 0%, transparent 42%),
      linear-gradient(to bottom, rgba(0, 0, 0, 0.02), transparent 30%);
  pointer-events: none;
  z-index: 0;
}

.hero-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  overflow: hidden;
  min-height: 0;
  padding: 0;
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 clamp(40px, 4.5vw, 80px);
  gap: clamp(24px, 3.5vw, 56px);

  /* 略上移，避免大屏顶空过多；幅度减小减少与底栏挤压感 */
  transform: translateY(-1.5vh);
}

/* 左侧文字区域 */
.text-side {
  flex: 0.85;
  min-width: 0;
  padding-top: 0;

  .category-tag {
    font-size: clamp(11px, 1.1vw, 13px);
    font-weight: 900;
    color: #8a8a8a;
    letter-spacing: 3px;
    margin-bottom: clamp(10px, 1.3vh, 14px);
    display: inline-block;
    padding: 4px 10px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.65);
  }

  .main-title {
    font-size: clamp(52px, 6.5vw, 96px);
    font-weight: 900;
    line-height: 0.92;
    letter-spacing: -3px;
    color: $ink-black;
    margin-bottom: clamp(14px, 1.5vh, 20px);

    .highlight {
      color: $bai-cyan;
    }
  }

  .description {
    font-size: clamp(13px, 1.2vw, 15px);
    color: #666;
    line-height: 1.8;
    margin-bottom: clamp(16px, 2vh, 22px);
    max-width: 420px;
  }

  .hero-stats {
    display: flex;
    gap: clamp(22px, 3vw, 42px);
    margin-bottom: clamp(20px, 2.5vh, 28px);
    flex-wrap: wrap;

    .stat-badge {
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 86px;

      .num {
        font-size: clamp(32px, 3.6vw, 48px);
        font-weight: 900;
        color: $ink-black;
        line-height: 1;
      }

      .lab {
        font-size: 11px;
        color: #9a9a9a;
        font-weight: 900;
        letter-spacing: 1px;
      }
    }
  }
}

/* 右侧模型区域 */
.model-side {
  flex: 1.15;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  transform: translateY(-1vh);

  .model-container {
    width: clamp(560px, 54vw, 880px);
    height: clamp(440px, 56vh, 680px);
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;
  }

  .model-glow {
    position: absolute;
    inset: 12% 10% 6% 10%;
    background:
        radial-gradient(circle at 50% 44%, rgba(51, 154, 240, 0.18), transparent 58%),
        radial-gradient(circle at 50% 62%, rgba(198, 40, 40, 0.09), transparent 62%);
    filter: blur(20px);
    z-index: 0;
    pointer-events: none;
  }

  .hero-model {
    width: 100%;
    height: 100%;
    outline: none;
    z-index: 10;
    --progress-bar-height: 0px;
    --progress-bar-color: transparent;
    --poster-color: transparent;
  }

  .stage-ring {
    position: absolute;
    bottom: 3%;
    left: 50%;
    transform: translateX(-50%) rotateX(75deg);
    width: clamp(400px, 42vw, 640px);
    height: clamp(400px, 42vw, 640px);
    border: 2px dashed rgba(0, 0, 0, 0.08);
    border-radius: 50%;
    z-index: 1;
  }

  .model-label {
    position: absolute;
    bottom: 18px;
    right: 10px;
    text-align: right;
    z-index: 12;

    h3 {
      font-size: clamp(26px, 2.4vw, 36px);
      font-weight: 900;
      margin: 12px 0 0;
      letter-spacing: 3px;
      color: $ink-black;
    }

    .badge {
      display: inline-block;
      background: $ink-black;
      color: #fff;
      padding: 6px 14px;
      font-size: 11px;
      font-weight: 900;
      letter-spacing: 2px;
    }
  }
}

.model-description {
  max-width: clamp(400px, 42vw, 640px);
  text-align: center;
  padding: 0 16px;

  p {
    font-size: clamp(12px, 1.1vw, 14px);
    color: #666;
    line-height: 1.75;
    margin: 0;
  }
}

/* 按钮：细线框 + 克制位移，与门户窄线框风格一致 */
.cta-area {
  .industrial-btn {
    background: $ink-black;
    color: #fff;
    border: 1px solid $ink-black;
    padding: clamp(12px, 1.5vh, 16px) clamp(34px, 4vw, 52px);
    font-size: clamp(13px, 1.2vw, 15px);
    font-weight: 900;
    letter-spacing: 2px;
    cursor: pointer;
    border-radius: 2px;
    transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
    box-shadow: none;

    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 4px 4px 0 $bai-cyan;
    }

    &:active {
      transform: translate(0, 0);
      box-shadow: none;
    }
  }
}

/* 隐藏 model-viewer 进度条 */
:deep(model-viewer.hero-model) {
  &::part(default-progress-bar) {
    display: none !important;
  }
}

@media (max-width: 1280px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    gap: 28px;
    padding: 28px clamp(28px, 4vw, 64px) 12px;
    transform: none;
  }

  .text-side {
    max-width: 920px;

    .description {
      max-width: 100%;
      margin-left: auto;
      margin-right: auto;
    }

    .hero-stats {
      justify-content: center;
    }
  }

  .model-side {
    width: 100%;
    transform: none;

    .model-container {
      width: min(780px, 88vw);
      height: clamp(420px, 56vh, 620px);
    }

    .model-description {
      max-width: 720px;
    }

    .model-label {
      right: 50%;
      transform: translateX(50%);
      text-align: center;
      bottom: 10px;
    }
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 10px 0 0;
  }

  .hero-content {
    padding: 0 18px;
    gap: 18px;
  }

  .text-side {
    .main-title {
      letter-spacing: -2px;
    }

    .hero-stats {
      flex-direction: column;
      align-items: center;
      gap: 12px !important;
    }
  }

  .model-side {
    .model-container {
      width: 100%;
      max-width: 100%;
      height: 320px;
    }

    .stage-ring {
      width: 280px;
      height: 280px;
    }

    .model-label {
      bottom: 8px;

      h3 {
        font-size: 20px;
      }
    }
  }
}
</style>
