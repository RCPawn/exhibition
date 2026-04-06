<template>
  <div class="genealogy-container" v-loading="loading" element-loading-background="rgba(242, 240, 235, 1)">
    <div class="paper-bg"></div>
    <div class="vignette-overlay"></div>

    <div class="book-stage" :class="{ 'stage-zoom-in': isEntering }">
      <div class="header-section" :class="{ 'fade-out': isEntering }">
        <h1 class="main-title">
          <span class="seal">白族</span>
          <span class="text">传承图谱</span>
        </h1>
        <p class="subtitle">经纬交织 · 纸上乾坤</p>
      </div>

      <div class="model-wrapper" @mousedown="handleMouseDown" @mouseup="handleMouseUp">
        <model-viewer
            id="heritage-book"
            :src="modelSrc"
            shadow-intensity="1.5"
            exposure="1.2"
            camera-controls
            disable-zoom
            disable-pan
            interaction-prompt="none"
            auto-rotate
            camera-orbit="45deg 60deg 2.8m"
            loading="eager"
            class="industrial-model"
        >
          <div slot="progress-bar" style="display: none;"></div>

          <div class="hotspot-hint" slot="hotspot-center" data-position="0 0.1 0">
            <div class="hint-dot"></div>
            <span class="hint-text">点击查阅</span>
          </div>
        </model-viewer>
      </div>
    </div>

    <div class="page-turn-overlay" v-if="isEntering && !showDeck">
      <div class="page-leaf"></div>
    </div>

    <transition name="fade-slow" @after-enter="initGraphOnEnter">
      <div class="archive-deck" v-if="showDeck">
        <!-- 顶部标题栏 -->
        <nav class="deck-nav">
          <div class="nav-left" @click="handleCloseDeck">
            <el-icon><ArrowLeft/></el-icon>
            <span>返回封面</span>
          </div>
          <div class="nav-center">白族非遗传承谱系</div>
          <div class="nav-right">数据已同步</div>
        </nav>

        <!-- 主图区 -->
        <section class="graph-container" ref="graphRef">
          <!-- ECharts 图谱将渲染于此 -->
        </section>
        
        <!-- 颜色图例（在graph-container外部，避免被overflow: hidden裁剪） -->
        <div class="color-legend">
          <div class="legend-title">图例说明</div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #1A4A7F;"></span>
            <span>顶级非遗分类</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #2E86AB;"></span>
            <span>非遗子分类</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #C62828;"></span>
            <span>传承人</span>
          </div>
        </div>

        <!-- 底部悬浮导航 -->
        <div class="bottom-nav">
          <div class="nav-inner">
            <div
                class="nav-tab"
                v-for="(cat, index) in categoryTree"
                :key="cat.categoryId"
                :class="{ active: activeCatId === cat.categoryId }"
                @click="handleSelectCategory(cat)"
            >
              <span class="tab-index">0{{ index + 1 }}</span>
              <span class="tab-label">{{ cat.categoryName }}</span>
            </div>
          </div>
        </div>

        <!-- 悬浮信息卡片 -->
        <transition name="tooltip-fade">
          <div class="node-tooltip" v-if="tooltipData" :style="tooltipStyle">
            <div class="tooltip-header">
              <span class="tooltip-type">{{ tooltipData.category === 0 ? '技艺项目' : (tooltipData.category === 1 ? '子分类' : '传承人') }}</span>
              <dict-tag
                  v-if="tooltipData.category === 2 && tooltipData.level"
                  :options="heritage_level"
                  :value="tooltipData.level"
                  class="level-badge"
              />
            </div>
            <h3 class="tooltip-name">{{ tooltipData.name }}</h3>
            <div class="tooltip-intro" v-html="tooltipData.intro || '暂无详细描述'"></div>
          </div>
        </transition>
      </div>
    </transition>
  </div>
</template>

<script setup name="HeritageGenealogy">
import { ref, onMounted, computed, getCurrentInstance, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { treeselect } from "@/api/heritage/category"
import { listInheritor } from "@/api/heritage/inheritor"
import * as echarts from 'echarts'
import { ArrowLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { heritage_level } = proxy.useDict('heritage_level')

const loading = ref(true)
const isEntering = ref(false)
const showDeck = ref(false)
const categoryTree = ref([])
const activeCatId = ref(null)
const graphRef = ref(null)
let myChart = null

const tooltipData = ref(null)
const tooltipStyle = ref({})

let startX = 0, startY = 0, startTime = 0
const modelSrc = computed(() => import.meta.env.VITE_APP_BASE_API + "/profile/model/book.glb")

const initData = async () => {
  loading.value = true
  try {
    const res = await treeselect()
    categoryTree.value = res.data.slice(0, 4)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleMouseDown(e) {
  startX = e.clientX;
  startY = e.clientY;
  startTime = Date.now()
}

function handleMouseUp(e) {
  if (isEntering.value) return
  const diffX = Math.abs(e.clientX - startX)
  const diffY = Math.abs(e.clientY - startY)
  if (diffX < 10 && diffY < 10 && (Date.now() - startTime) < 300) {
    isEntering.value = true
    setTimeout(() => {
      showDeck.value = true
    }, 800)
  }
}

const initGraphOnEnter = () => {
  nextTick(() => {
    if (categoryTree.value.length > 0) handleSelectCategory(categoryTree.value[0])
  })
}

const handleCloseDeck = () => {
  showDeck.value = false
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  tooltipData.value = null
  setTimeout(() => {
    isEntering.value = false
  }, 500)
}

const handleSelectCategory = async (cat) => {
  activeCatId.value = cat.categoryId

  // 阿里巴巴矢量图标库的SVG path
  // 皇冠 -> 顶级分类，非遗标 -> 子分类，人像 -> 传承人
  const nodes = [{
    id: 'root',
    name: cat.categoryName,
    category: 0,
    symbolSize: 80,
    intro: '技艺分类'
  }]
  const links = []

  if (cat.children) {
    cat.children.forEach(child => {
      nodes.push({
        id: `c_${child.categoryId}`,
        name: child.categoryName,
        category: 1,
        symbolSize: 55,
        intro: '子分类'
      })
      links.push({ source: 'root', target: `c_${child.categoryId}` })
    })
  }

  const res = await listInheritor({ categoryId: cat.categoryId })
  if (res.rows) {
    res.rows.forEach(master => {
      nodes.push({
        id: `m_${master.inheritorId}`,
        name: master.name,
        category: 2,
        symbolSize: 65,
        level: master.level,
        intro: master.introduction || '这位传承人致力于非遗技艺的传承与保护'
      })
      links.push({ source: `c_${master.categoryId}`, target: `m_${master.inheritorId}` })
    })
  }

  renderGraph(nodes, links)
}

const renderGraph = (nodes, links) => {
  if (!graphRef.value) return
  if (myChart) myChart.dispose()

  myChart = echarts.init(graphRef.value, null, { renderer: 'canvas' })

  myChart.setOption({
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'bottom',
        color: '#333',
        fontWeight: '600',
        fontSize: 14,
        textBorderColor: '#fff',
        textBorderWidth: 3,
      },
      force: {
        repulsion: 2500,
        gravity: 0.08,
        edgeLength: 180,
        layoutAnimation: true
      },
      lineStyle: {
        color: '#999',
        width: 2,
        curveness: 0.3,
        opacity: 0.6
      },
      itemStyle: {
        color: (params) => {
          const colors = ['#1A4A7F', '#2E86AB', '#C62828']
          return colors[params?.data?.category] || '#999'
        },
        borderColor: '#fff',
        borderWidth: 3,
        shadowBlur: 20,
        shadowColor: 'rgba(0,0,0,0.15)'
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 4, opacity: 1 },
        itemStyle: {
          shadowBlur: 25,
          shadowColor: 'rgba(0,0,0,0.25)'
        }
      },

    }]
  })

  // 鼠标悬浮显示信息（优化事件处理）
  myChart.on('mouseover', function(params) {
    if (params.dataType === 'node' && params.data) {
      tooltipData.value = params.data
      updateTooltipPosition(params.event)
    }
  })

  myChart.on('mouseout', function(params) {
    if (params.dataType === 'node') {
      // 延迟隐藏，避免快速移动时闪烁
      setTimeout(() => {
        if (!myChart || myChart.isDisposed()) return
        tooltipData.value = null
      }, 100)
    }
  })

  myChart.on('mousemove', function(params) {
    if (params.dataType === 'node' && tooltipData.value) {
      updateTooltipPosition(params.event)
    }
  })

  window.addEventListener('resize', () => myChart?.resize())
}

const updateTooltipPosition = (event) => {
  const container = graphRef.value?.getBoundingClientRect()
  if (!container) return

  const x = event.offsetX || (event.clientX - container.left)
  const y = event.offsetY || (event.clientY - container.top)

  tooltipStyle.value = {
    left: `${x + 20}px`,
    top: `${y - 20}px`
  }
}

onMounted(initData)
onUnmounted(() => {
  if (myChart) myChart.dispose()
})
</script>

<style scoped lang="scss">
$bg-color: #ffffff;
$primary-blue: #1A4A7F;
$wood-brown: #5D4037;
$seal-red: #C62828;
$accent: #FFD700;

.genealogy-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 84px);
  background-color: #ffffff;
  overflow: hidden;
  font-family: "Songti SC", "SimSun", serif;
  perspective: 2000px;
}

.paper-bg {
  position: absolute;
  inset: 0;
  opacity: 0.8;
  z-index: 0;
}

.vignette-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, transparent 65%, rgba(255, 255, 255, 0.98) 100%);
  z-index: 1;
  pointer-events: none;
}

.book-stage {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 1.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.stage-zoom-in {
  transform: scale(3) translateY(10%);
  pointer-events: none;
}

.header-section {
  text-align: center;
  margin-bottom: -50px;
  transition: opacity 0.5s ease;
  transform: translateY(-40px);
}

.header-section.fade-out {
  opacity: 0;
}

.main-title {
  color: #1A4A7F;
  font-size: clamp(2rem, 4vw, 4rem); // 响应式标题
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;

  .seal {
    background: #C62828;
    color: #fff;
    font-size: clamp(1rem, 1.5vw, 1.5rem); // 响应式印章
    padding: 4px 10px;
    border-radius: 4px;
    box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
    font-weight: normal;
  }

  .text {
    font-weight: 900;
    letter-spacing: 10px;
  }
}

.subtitle {
  color: #5D4037;
  font-size: clamp(0.9rem, 1.2vw, 1.2rem); // 响应式副标题
  letter-spacing: 15px;
  margin-top: 15px;
  opacity: 0.7;
  font-weight: bold;
}

.model-wrapper {
  width: 100%;
  max-width: min(900px, 80vw); // 响应式最大宽度
  height: clamp(40vh, 60vh, 60vh); // 响应式高度
  position: relative;
  z-index: 11;
  cursor: pointer;
  overflow: visible !important;
}

.industrial-model {
  width: 100%;
  height: 100%;
  /* 隐藏加载进度条的关键 CSS 变量 */
  --progress-bar-height: 0px;
  --progress-bar-color: transparent;
  --poster-color: transparent;
}

.hotspot-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  transform: translateY(-50px);
  opacity: 0.8;
}

.hint-dot {
  width: 12px;
  height: 12px;
  background-color: #C62828;
  border-radius: 50%;
  box-shadow: 0 0 10px #C62828;
  animation: pulse 2s infinite;
}

.hint-text {
  color: #5D4037;
  font-weight: bold;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.6);
  padding: 4px 8px;
  border-radius: 4px;
}

.page-turn-overlay {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(to right, #e3e0d6, #fff 10%, #fff);
  z-index: 50;
  animation: pageFlip 1.2s cubic-bezier(0.645, 0.045, 0.355, 1) forwards;
}

@keyframes pageFlip {
  0% { width: 0; }
  100% { width: 100%; }
}

.archive-deck {
  position: absolute;
  inset: 0;
  z-index: 60;
  display: flex;
  flex-direction: column;
  background: #fff;

  .deck-nav {
    height: clamp(50px, 6vh, 60px); // 响应式导航栏高度
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 clamp(20px, 3vw, 40px); // 响应式内边距
    font-weight: 900;
    font-size: 12px;
    background: #fafafa;
    border-bottom: 1px solid #eee;

    .nav-left {
      cursor: pointer;
      user-select: none;
      display: flex;
      align-items: center;
      gap: 8px;
      &:hover { color: #999; }
    }

    .nav-center {
      font-size: 16px;
      letter-spacing: 2px;
    }
  }

  .graph-container {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at center, #fff 0%, #f5f5f5 100%);
  }

  .color-legend {
    position: absolute;
    bottom: clamp(80px, 10vh, 90px); // 响应式位置
    right: clamp(20px, 3vw, 40px); // 响应式位置
    z-index: 999;
    background: rgba(255,255,255,0.96);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 12px;
    padding: clamp(12px, 1.5vh, 16px) clamp(16px, 2vw, 20px); // 响应式内边距
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    border: 1px solid rgba(0,0,0,0.08);
    min-width: 140px;
    max-width: 200px;
      
    .legend-title {
      font-size: 13px;
      font-weight: 800;
      color: #222;
      margin-bottom: 12px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;
      letter-spacing: 1px;
    }
      
    .legend-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
      font-size: 13px;
      color: #444;
      font-weight: 600;
        
      &:last-child {
        margin-bottom: 0;
      }
        
      .legend-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        display: inline-block;
        flex-shrink: 0;
        box-shadow: 0 2px 6px rgba(0,0,0,0.15);
      }
    }
      
    // 小屏幕适配
    @media (max-width: 768px) {
      bottom: 80px;
      right: 20px;
      padding: 12px 16px;
      min-width: 140px;
        
      .legend-title {
        font-size: 12px;
        margin-bottom: 10px;
        padding-bottom: 8px;
      }
        
      .legend-item {
        gap: 8px;
        margin-bottom: 6px;
        font-size: 12px;
          
        .legend-dot {
          width: 10px;
          height: 10px;
        }
      }
    }
  }

  .bottom-nav {
    position: absolute;
    bottom: clamp(20px, 3vh, 30px); // 响应式位置
    left: 50%;
    transform: translateX(-50%);
    z-index: 100;

    .nav-inner {
      display: flex;
      gap: clamp(8px, 1vw, 12px); // 响应式间距
      background: rgba(255,255,255,0.95);
      backdrop-filter: blur(10px);
      padding: clamp(8px, 1vh, 12px) clamp(15px, 2vw, 20px); // 响应式内边距
      border-radius: 16px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.12);
      border: 1px solid rgba(0,0,0,0.08);

      .nav-tab {
        padding: clamp(8px, 1vh, 12px) clamp(16px, 2vw, 24px); // 响应式内边距
        cursor: pointer;
        transition: all 0.3s ease;
        border-radius: 10px;
        display: flex;
        align-items: center;
        gap: 8px;
        white-space: nowrap;

        .tab-index {
          font-size: 10px;
          color: #ccc;
          font-weight: 900;
        }

        .tab-label {
          font-size: 14px;
          font-weight: 700;
          color: #666;
        }

        &:hover {
          background: #f5f5f5;

          .tab-label {
            color: #333;
          }
        }

        &.active {
          background: #000;

          .tab-index {
            color: rgba(255,255,255,0.5);
          }

          .tab-label {
            color: #fff;
          }
        }
      }
    }
  }

  .node-tooltip {
    position: absolute;
    z-index: 200;
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    min-width: 280px;
    max-width: 350px;
    box-shadow: 0 8px 32px rgba(0,0,0,0.15);
    border: 1px solid #eee;
    pointer-events: none;

    .tooltip-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 2px solid #f0f0f0;

      .tooltip-type {
        font-size: 11px;
        color: #999;
        font-weight: 900;
        letter-spacing: 2px;
      }

      .level-badge {
        :deep(.el-tag) {
          font-size: 11px;
        }
      }
    }

    .tooltip-name {
      font-size: 20px;
      font-weight: 900;
      color: #000;
      margin: 0 0 12px 0;
    }

    .tooltip-intro {
      font-size: 13px;
      color: #666;
      line-height: 1.8;
      margin: 0;
    }
  }
}

.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: all 0.3s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.5; }
  100% { transform: scale(1.1); opacity: 0.8; }
}

/* 强制隐藏 model-viewer 内部进度条 */
:deep(model-viewer#heritage-book) {
  &::part(default-progress-bar) {
    display: none !important;
  }
}
</style>