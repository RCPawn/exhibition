<template>
  <div class="genealogy-container" v-loading="loading" element-loading-background="rgba(242, 240, 235, 1)">
    <!-- 1. 沉浸式背景 (纸纹 + 晕染) -->
    <div class="paper-bg"></div>
    <div class="vignette-overlay"></div>

    <!-- 🌟 场景 A: 3D 书本核心区 -->
    <div class="book-stage" :class="{ 'stage-zoom-in': isEntering }">

      <!-- 标题区域：上移并在进入时淡出 -->
      <div class="header-section" :class="{ 'fade-out': isEntering }">
        <h1 class="main-title">
          <span class="seal">白族</span>
          <span class="text">传承图谱</span>
        </h1>
        <p class="subtitle">经纬交织 · 纸上乾坤</p>
      </div>

      <!-- 3D 模型区域 -->
      <!-- 监听 mousedown/up 用于区分拖拽和点击 -->
      <div class="model-wrapper"
           @mousedown="handleMouseDown"
           @mouseup="handleMouseUp"
           @touchstart="handleMouseDown"
           @touchend="handleMouseUp">

        <model-viewer
            id="heritage-book"
            :src="modelSrc"
            shadow-intensity="1.5"
            shadow-softness="0.8"
            exposure="1.2"
            camera-controls
            auto-rotate
            rotation-per-second="15deg"
            min-camera-orbit="auto auto 2m"
            max-camera-orbit="auto auto 100%"
            camera-orbit="45deg 60deg 2.8m"
            class="industrial-model"
            disable-zoom
            interaction-prompt="none"
        >
          <!-- 自定义浮动提示 (跟随模型中心) -->
          <div class="hotspot-hint" slot="hotspot-center" data-position="0 0.1 0" data-normal="0 1 0">
            <div class="hint-dot"></div>
            <span class="hint-text">点击查阅</span>
          </div>
        </model-viewer>
      </div>
    </div>

    <!-- 🌟 场景 B: 翻书转场特效层 -->
    <!-- 这是一个覆盖全屏的遮罩，模拟书页翻过的动作 -->
    <div class="page-turn-overlay" v-if="isEntering && !showCatalog">
      <div class="page-leaf"></div>
    </div>

    <!-- 🌟 场景 C: 目录内容层 -->
    <transition name="fade-slow">
      <div class="catalog-layer" v-if="showCatalog">
        <div class="catalog-header">
          <el-button link class="back-btn" @click="handleCloseBook">
            <el-icon><Back /></el-icon> 合上图谱
          </el-button>
          <h2>传承类目索引</h2>
        </div>

        <div class="pop-up-grid">
          <div
              v-for="(item, index) in categoryTree"
              :key="item.categoryId"
              class="pop-card"
              :style="{ '--delay': index * 0.1 + 's' }"
              @click="handleCategoryClick(item)"
          >
            <div class="card-inner">
              <span class="bg-char">{{ item.categoryName ? item.categoryName.charAt(0) : '' }}</span>
              <div class="card-content">
                <h3>{{ item.categoryName }}</h3>
                <div class="divider"></div>
                <ul class="sub-list">
                  <li v-for="sub in item.children" :key="sub.categoryId">
                    {{ sub.categoryName }}
                  </li>
                  <li v-if="!item.children?.length" class="empty-tip">暂无分支</li>
                </ul>
              </div>
              <div class="tie-dye-pattern"></div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup name="HeritageGenealogy">
import { ref, onMounted, computed, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { treeselect } from "@/api/heritage/category"
import { Back } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

// 状态控制
const loading = ref(true)
const isEntering = ref(false) // 是否正在执行进入动画
const showCatalog = ref(false) // 是否显示目录
const categoryTree = ref([])
const dataReady = ref(false)

// 交互逻辑变量
let startX = 0
let startY = 0
let startTime = 0

// 计算模型路径
const modelSrc = computed(() => {
  return import.meta.env.VITE_APP_BASE_API + "/profile/model/book.glb"
})

// 1. 获取数据
const getTreeData = async () => {
  loading.value = true
  try {
    const res = await treeselect()
    if (res.code === 200 && Array.isArray(res.data)) {
      categoryTree.value = res.data.filter(i => i.categoryName).slice(0, 4)
      dataReady.value = true
    }
  } catch (error) {
    console.error("加载失败", error)
  } finally {
    loading.value = false
  }
}

// 2. 智能交互处理：区分拖拽和点击
function handleMouseDown(e) {
  // 兼容触摸和鼠标
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY

  startX = clientX
  startY = clientY
  startTime = Date.now()
}

function handleMouseUp(e) {
  if (isEntering.value) return // 已经触发了就别动了

  const clientX = e.changedTouches ? e.changedTouches[0].clientX : e.clientX
  const clientY = e.changedTouches ? e.changedTouches[0].clientY : e.clientY

  const diffX = Math.abs(clientX - startX)
  const diffY = Math.abs(clientY - startY)
  const diffTime = Date.now() - startTime

  // 判定规则：位移小于 10px 且 时间小于 300ms 视为点击，否则视为拖拽旋转
  if (diffX < 10 && diffY < 10 && diffTime < 300) {
    triggerEnterAnimation()
  }
}

// 3. 执行进入动画 (核心体验)
function triggerEnterAnimation() {
  if (!dataReady.value) {
    proxy.$modal.msgWarning("典籍数据加载中...")
    return
  }

  // 第一步：镜头推进动画 (CSS transform scale)
  isEntering.value = true

  // 第二步：800ms 后触发翻页遮罩，并显示目录
  setTimeout(() => {
    showCatalog.value = true
  }, 800)
}

// 4. 合上图谱 (重置状态)
function handleCloseBook() {
  showCatalog.value = false
  setTimeout(() => {
    isEntering.value = false
  }, 300) // 等目录淡出后再恢复 3D 场景
}

// 5. 跳转详情
function handleCategoryClick(category) {
  router.push({
    path: '/heritage/heritage_exhibition',
    query: { categoryId: category.categoryId }
  })
}

onMounted(() => {
  getTreeData()
})
</script>

<style scoped lang="scss">
/* --- 1. 全局与背景 --- */
$bg-color: #F2F0EB;
$primary-blue: #1A4A7F;
$wood-brown: #5D4037;
$seal-red: #C62828;

.genealogy-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 84px);
  background-color: $bg-color;
  overflow: hidden;
  font-family: "Songti SC", "SimSun", serif;
  perspective: 2000px; /* 增强 3D 纵深感 */
}

.paper-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://www.transparenttextures.com/patterns/cream-paper.png');
  opacity: 0.8;
  z-index: 0;
}

/* 四周暗角，增加电影感 */
.vignette-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, transparent 60%, rgba(0,0,0,0.1) 100%);
  z-index: 1;
  pointer-events: none;
}

/* --- 2. 舞台区域 (标题 + 模型) --- */
.book-stage {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 1.2s cubic-bezier(0.25, 1, 0.5, 1); /* 极度平滑的缓动 */
}

/* 进入时的镜头推进效果：放大舞台，仿佛人脸贴近了书 */
.stage-zoom-in {
  transform: scale(3) translateY(10%); /* 放大并略微下移，让视线聚焦书本 */
  pointer-events: none; /* 动画过程中禁止交互 */
}

.header-section {
  text-align: center;
  margin-bottom: -50px; /* 让书本侵入标题区域，更紧凑 */
  position: relative;
  z-index: 12;
  transition: opacity 0.5s ease;
  transform: translateY(-40px); /* 整体上移一点 */
}

.header-section.fade-out {
  opacity: 0;
}

.main-title {
  color: $primary-blue;
  font-size: 4rem;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  text-shadow: 0 10px 20px rgba(0,0,0,0.05);

  .seal {
    background: $seal-red;
    color: #fff;
    font-size: 1.5rem;
    padding: 4px 10px;
    border-radius: 4px;
    box-shadow: 2px 2px 5px rgba(0,0,0,0.2);
  }
  .text {
    font-weight: 900;
    letter-spacing: 10px;
  }
}

.subtitle {
  color: $wood-brown;
  font-size: 1.2rem;
  letter-spacing: 15px;
  margin-top: 15px;
  opacity: 0.7;
  font-weight: bold;
}

/* --- 3. 模型容器优化 --- */
.model-wrapper {
  /* 使用宽高比自适应，防止模型旋转超出 */
  width: 100%;
  max-width: 900px;
  height: 60vh; /* 只有高度限制，宽度铺满 */
  position: relative;
  z-index: 11;
  cursor: grab;
  /* 解决遮挡的核心：visible */
  overflow: visible !important;

  &:active {
    cursor: grabbing;
  }
}

.industrial-model {
  width: 100%;
  height: 100%;
  /* 允许模型超出 canvas 边界渲染 (虽然 model-viewer 本身 canvas 是矩形，但这样设置容器不会截断) */
  --poster-color: transparent;
}

/* 浮动提示点：代替原来的按钮 */
.hotspot-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  transform: translateY(-50px); /* 悬浮在书本上方 */
  opacity: 0.8;
  transition: all 0.3s;
  cursor: pointer;
}

.hint-dot {
  width: 12px;
  height: 12px;
  background-color: $seal-red;
  border-radius: 50%;
  box-shadow: 0 0 10px $seal-red;
  animation: pulse 2s infinite;
}

.hint-text {
  color: $wood-brown;
  font-weight: bold;
  font-size: 14px;
  letter-spacing: 1px;
  background: rgba(255,255,255,0.6);
  padding: 4px 8px;
  border-radius: 4px;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 1; }
  100% { transform: scale(0.8); opacity: 0.5; }
}

/* --- 4. 翻页遮罩 (Novel Page Flip) --- */
.page-turn-overlay {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(to right, #e3e0d6, #fff 10%, #fff);
  box-shadow: -10px 0 30px rgba(0,0,0,0.1);
  z-index: 50;
  /* 翻页动画：从右向左刷过全屏 */
  animation: pageFlip 1.2s cubic-bezier(0.645, 0.045, 0.355, 1) forwards;
}

@keyframes pageFlip {
  0% { width: 0; opacity: 0.8; }
  50% { width: 100%; opacity: 1; }
  100% { width: 100%; opacity: 0; pointer-events: none; } /* 最后消失，露出下面的 Catalog */
}

/* --- 5. 目录层 (Catalog) --- */
.catalog-layer {
  position: absolute;
  inset: 0;
  z-index: 60; /* 最高层级 */
  padding: 40px 60px;
  display: flex;
  flex-direction: column;
  background: rgba(242, 240, 235, 0.95); /* 稍微一点点透 */
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid $wood-brown;
  padding-bottom: 20px;
  margin-bottom: 50px;

  h2 {
    color: $wood-brown;
    font-size: 2rem;
    margin: 0;
  }
}

.back-btn {
  font-size: 1.1rem;
  color: $primary-blue;
  &:hover { color: $seal-red; }
}

.pop-up-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.pop-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  height: 400px;
  position: relative;
  cursor: pointer;
  opacity: 0;
  transform-style: preserve-3d;
  /* 卡片入场动画 */
  animation: cardEnter 0.6s ease-out forwards;
  animation-delay: var(--delay);
  transition: all 0.4s ease;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);

  &:hover {
    transform: translateY(-10px);
    box-shadow: 0 20px 40px rgba(26, 74, 127, 0.15);
    border-color: $primary-blue;

    .bg-char { color: rgba(26, 74, 127, 0.1); transform: scale(1.1); }
  }
}

.card-inner {
  height: 100%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  position: relative;
}

.bg-char {
  position: absolute;
  top: -30px;
  right: -20px;
  font-size: 10rem;
  font-family: "KaiTi", serif;
  color: #f7f7f7;
  z-index: 0;
  transition: all 0.5s;
}

.card-content {
  position: relative;
  z-index: 1;
  width: 100%;
  text-align: center;

  h3 {
    font-size: 1.8rem;
    color: $primary-blue;
    margin-bottom: 20px;
  }
}

.divider {
  width: 50px;
  height: 4px;
  background: $seal-red;
  margin: 0 auto 30px;
}

.sub-list {
  list-style: none;
  padding: 0;

  li {
    padding: 10px 0;
    color: #666;
    font-size: 1rem;
    border-bottom: 1px dashed #eee;
    &:last-child { border: none; }
  }
  .empty-tip { color: #ccc; font-style: italic; }
}

.tie-dye-pattern {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 12px;
  background: repeating-linear-gradient(45deg, $primary-blue, $primary-blue 10px, #fff 10px, #fff 20px);
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-slow-enter-active, .fade-slow-leave-active {
  transition: opacity 1s ease;
}
.fade-slow-enter-from, .fade-slow-leave-to {
  opacity: 0;
}
</style>