<template>

  <div
      class="interactive-bar-container"
      ref="containerRef"
      @mousemove="handleContainerMouseMove"
      @mouseleave="handleContainerMouseLeave"
  >
    <div
        v-for="(bar, index) in bars"
        :key="index"
        class="bar-item"
        :style="{
          height: bar.height + 'px',
          backgroundColor: bar.color,
          flex: 1
        }"
    >
      <div class="grain-overlay"></div>
      <div class="fiber-lines"></div>
    </div>
  </div>

</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 保留非遗矿石配色
const mineralColors = ['#2F3E3F', '#5E2F2F', '#46505C', '#535640', '#1A1A1A', '#3C4856'];

// 显著减少数量，让每一块木板都非常宽
const BAR_COUNT = 18;
const BASE_HEIGHT = 50;
const MAX_HEIGHT = 130;

const bars = ref(Array.from({ length: BAR_COUNT }, (_, i) => ({
  height: BASE_HEIGHT,
  color: mineralColors[i % mineralColors.length]
})));

const containerRef = ref(null);
let animationFrameId = null;
let isMouseInside = ref(false);

// 仅当鼠标在容器内时触发交互
const handleContainerMouseMove = (e) => {
  if (!containerRef.value || !isMouseInside.value) return;
  
  if (animationFrameId) cancelAnimationFrame(animationFrameId);

  animationFrameId = requestAnimationFrame(() => {
    if (!containerRef.value) return;

    const rect = containerRef.value.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;

    // 只有鼠标在容器水平范围内才产生交互
    if (mouseX < 0 || mouseX > rect.width) return;

    // 准确定位鼠标所在的木块索引
    const index = (mouseX / rect.width) * bars.value.length;

    bars.value.forEach((bar, i) => {
      const dist = Math.abs(i - index);
      // 波及范围
      const range = 5;

      if (dist < range) {
        // 使用更平滑的 Cosine 波形
        const scale = Math.cos((dist / range) * (Math.PI / 2));
        bar.height = BASE_HEIGHT + (MAX_HEIGHT - BASE_HEIGHT) * Math.pow(scale, 1.5);
      } else {
        bar.height = BASE_HEIGHT;
      }
    });
  });
};

// 鼠标离开容器时重置
const handleContainerMouseLeave = () => {
  isMouseInside.value = false;
  resetBars();
};

// 鼠标进入容器时标记
const handleContainerMouseEnter = () => {
  isMouseInside.value = true;
};

const resetBars = () => {
  if (animationFrameId) cancelAnimationFrame(animationFrameId);
  bars.value.forEach(bar => {
    bar.height = BASE_HEIGHT;
  });
};

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('mouseenter', handleContainerMouseEnter);
  }
});

onUnmounted(() => {
  if (animationFrameId) cancelAnimationFrame(animationFrameId);
  if (containerRef.value) {
    containerRef.value.removeEventListener('mouseenter', handleContainerMouseEnter);
  }
});
</script>

<style scoped lang="scss">
.interactive-bar-container {
  display: flex;
  align-items: flex-end;
  justify-content: stretch;
  gap: 0; /* 彻底消除缝隙 */
  width: 100%;
  height: 150px;
  cursor: pointer;
  background: transparent;
  overflow: hidden;
}

.bar-item {
  position: relative;
  /* 极致流畅的关键：告诉浏览器此属性会变化 */
  will-change: height;
  /* 使用更物理的贝塞尔曲线 */
  transition: height 0.4s cubic-bezier(0.23, 1, 0.32, 1);

  // 顶端保持纯平或极微小圆角，符合2D木板裁切感
  border-top-left-radius: 1px;
  border-top-right-radius: 1px;
}

/* 2D木质纹理实现 - 模拟自然木纹层 */
.grain-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.15;
  /* 模拟木材表面的深浅节理 */
  background-image: radial-gradient(circle at 50% -20%, #ffffff 0%, transparent 80%),
  radial-gradient(circle at 20% 120%, #000000 0%, transparent 70%);
  mix-blend-mode: overlay;
}

/* 2D木质纹理实现 - 模拟纵向纤维 */
.fiber-lines {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.1;
  /* 极其细密的竖向线条 */
  background: repeating-linear-gradient(
          90deg,
          transparent 0px,
          transparent 1px,
          #000 2px,
          transparent 3px
  );
  /* 增加微小的横向纹理，打破死板 */
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: repeating-linear-gradient(
            0deg,
            rgba(0,0,0,0.1) 0px,
            transparent 1px,
            transparent 15px
    );
  }
}

/* 鼠标悬停时轻微色偏反馈，不产生3D光影 */
.bar-item:hover {
  filter: contrast(1.1) brightness(1.1);
}
</style>
