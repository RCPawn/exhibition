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
          <div class="nav-left">
            <AppBackButton plain variant="deck" preset="cover" @click="handleCloseDeck" />
          </div>
          <div class="nav-center">
            <span class="nav-title">白族非遗传承谱系</span>
            <span v-if="activeCategoryName" class="nav-breadcrumb">· {{ activeCategoryName }}</span>
          </div>
          <div class="nav-right hint-muted">拖拽平移 · 滚轮缩放</div>
        </nav>

        <div class="deck-main">
          <div class="graph-stack">
            <section class="graph-container" ref="graphRef" />
            <div class="graph-empty-hint" v-if="!selectedNode">
              <span>点选节点可在右侧查看传承说明</span>
            </div>
            <!-- 顶栏横向图例，避免与底部 Tab 抢视觉 -->
            <div class="color-legend color-legend--top">
              <span class="legend-inline">
                <span class="legend-dot" style="background: #1A4A7F;"></span>技艺项目
              </span>
              <span class="legend-inline">
                <span class="legend-dot" style="background: #2E86AB;"></span>子分类
              </span>
              <span class="legend-inline">
                <span class="legend-dot" style="background: #C62828;"></span>传承人
              </span>
            </div>
          </div>

          <transition name="detail-slide">
            <aside v-if="selectedNode" class="node-detail-panel" key="detail">
              <div class="panel-head">
                <span class="panel-kicker">{{ typeLabel(selectedNode.category) }}</span>
                <button type="button" class="panel-close" aria-label="关闭" @click="clearSelection">×</button>
              </div>
              <dict-tag
                v-if="selectedNode.category === 2 && selectedNode.level"
                :options="heritage_level"
                :value="selectedNode.level"
                class="panel-level"
              />
              <h2 class="panel-title">{{ selectedNode.name }}</h2>
              <div class="panel-intro" v-html="selectedNode.intro || '暂无详细描述'"></div>
              <p class="panel-footnote">连线表示谱系中的传承从属关系，与数据一致。</p>
            </aside>
          </transition>
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
      </div>
    </transition>
  </div>
</template>

<script setup name="HeritageGenealogy">
import { ref, onMounted, computed, getCurrentInstance, nextTick, onUnmounted, watch } from 'vue'
import { treeselect } from "@/api/heritage/category"
import { listInheritor } from "@/api/heritage/inheritor"
import * as echarts from 'echarts'
import AppBackButton from '@/components/AppBackButton.vue'

const { proxy } = getCurrentInstance()
const { heritage_level } = proxy.useDict('heritage_level')

const loading = ref(true)
const isEntering = ref(false)
const showDeck = ref(false)
const categoryTree = ref([])
const activeCatId = ref(null)
const graphRef = ref(null)
let myChart = null

/** 与接口构建的 nodes/links 一致，用于窗口变化时仅重算坐标、不重拉接口 */
const graphPayload = ref({ nodes: [], links: [] })
const selectedNode = ref(null)

let startX = 0, startY = 0, startTime = 0
let resizeTimer = null

const modelSrc = computed(() => import.meta.env.VITE_APP_BASE_API + "/profile/model/book.glb")

const activeCategoryName = computed(() => {
  const id = activeCatId.value
  if (!id || !categoryTree.value.length) return ''
  const hit = categoryTree.value.find(c => c.categoryId === id)
  return hit ? hit.categoryName : ''
})

function typeLabel(cat) {
  if (cat === 0) return '技艺项目'
  if (cat === 1) return '子分类'
  return '传承人'
}

function clearSelection() {
  selectedNode.value = null
}

/**
 * 在相同 nodes / links 下做分层排布（像素坐标，原点在画布左上），不增删边，仅补充 x、y。
 */
function computeTreeLayout(nodes, links, w, h) {
  const pad = { t: 36, r: 28, b: 100, l: 28 }
  const innerW = Math.max(120, w - pad.l - pad.r)
  const cx = pad.l + innerW / 2

  const children = new Map()
  for (const e of links) {
    if (!children.has(e.source)) children.set(e.source, [])
    children.get(e.source).push(e.target)
  }

  const pos = new Map()

  pos.set('root', { x: cx, y: pad.t + 36 })

  const d1 = children.get('root') || []
  const n1 = d1.length
  d1.forEach((id, i) => {
    const x = n1 <= 1 ? cx : pad.l + ((i + 1) / (n1 + 1)) * innerW
    pos.set(id, { x, y: pad.t + 130 })
  })

  const baseY = Math.min(pad.t + 240, h - pad.b - 40)
  const MAX_PER_ROW = 6
  const ROW_GAP = 76

  d1.forEach((pid) => {
    const kids = (children.get(pid) || []).filter(k => String(k).startsWith('m_'))
    const px = pos.get(pid)?.x ?? cx
    kids.forEach((kid, j) => {
      const row = Math.floor(j / MAX_PER_ROW)
      const rowStart = row * MAX_PER_ROW
      const rowKids = kids.slice(rowStart, rowStart + MAX_PER_ROW)
      const nk = rowKids.length
      const idxInRow = j - rowStart
      const maxSpread = nk <= 1 ? 0 : Math.min(68, innerW / (nk + 1))
      const offset = (idxInRow - (nk - 1) / 2) * maxSpread
      pos.set(kid, { x: px + offset, y: baseY + row * ROW_GAP })
    })
  })

  return nodes.map((n) => {
    const p = pos.get(n.id)
    if (p) {
      return { ...n, x: p.x, y: p.y }
    }
    return { ...n, x: cx, y: h / 2 }
  })
}

function bindChartEvents() {
  if (!myChart || myChart.isDisposed()) return
  myChart.off('click')
  myChart.on('click', (params) => {
    if (params.dataType === 'node' && params.data) {
      selectedNode.value = { ...params.data }
    }
  })
}

function buildChartOption(laidOutNodes, links) {
  return {
    animationDuration: 800,
    animationEasingUpdate: 'cubicOut',
    series: [{
      type: 'graph',
      layout: 'none',
      data: laidOutNodes,
      links,
      roam: true,
      draggable: false,
      label: {
        show: true,
        position: 'bottom',
        color: '#333',
        fontWeight: '600',
        fontSize: 13,
        textBorderColor: '#fff',
        textBorderWidth: 3
      },
      lineStyle: {
        color: '#b0b0b0',
        width: 2,
        curveness: 0.12,
        opacity: 0.75
      },
      itemStyle: {
        color: (params) => {
          const colors = ['#1A4A7F', '#2E86AB', '#C62828']
          return colors[params?.data?.category] || '#999'
        },
        borderColor: '#fff',
        borderWidth: 3,
        shadowBlur: 16,
        shadowColor: 'rgba(0,0,0,0.12)'
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3, opacity: 1, color: '#5D4037' },
        itemStyle: {
          shadowBlur: 22,
          shadowColor: 'rgba(0,0,0,0.22)'
        }
      }
    }]
  }
}

const renderGraph = (nodes, links) => {
  if (!graphRef.value) return

  const w = graphRef.value.clientWidth || 800
  const h = graphRef.value.clientHeight || 520
  const laidOut = computeTreeLayout(nodes, links, w, h)

  if (!myChart) {
    myChart = echarts.init(graphRef.value, null, { renderer: 'canvas' })
  }
  myChart.setOption(buildChartOption(laidOut, links), { notMerge: true })
  bindChartEvents()
}

function scheduleRelayout() {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    const { nodes, links } = graphPayload.value
    if (nodes.length && graphRef.value) {
      renderGraph(nodes, links)
    } else {
      myChart?.resize()
    }
  }, 120)
}

function onWindowResize() {
  scheduleRelayout()
}

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
  startX = e.clientX
  startY = e.clientY
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
  selectedNode.value = null
  graphPayload.value = { nodes: [], links: [] }
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  setTimeout(() => {
    isEntering.value = false
  }, 500)
}

const handleSelectCategory = async (cat) => {
  activeCatId.value = cat.categoryId
  selectedNode.value = null

  const nodes = [{
    id: 'root',
    name: cat.categoryName,
    category: 0,
    symbolSize: 72,
    intro: '当前谱系根节点为所选技艺分类，与子分类、传承人之间的连线与后台数据一致。'
  }]
  const links = []

  if (cat.children) {
    cat.children.forEach(child => {
      nodes.push({
        id: `c_${child.categoryId}`,
        name: child.categoryName,
        category: 1,
        symbolSize: 50,
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
        symbolSize: 58,
        level: master.level,
        intro: master.introduction || '这位传承人致力于非遗技艺的传承与保护'
      })
      links.push({ source: `c_${master.categoryId}`, target: `m_${master.inheritorId}` })
    })
  }

  graphPayload.value = {
    nodes: JSON.parse(JSON.stringify(nodes)),
    links: links.map(l => ({ ...l }))
  }

  await nextTick()
  renderGraph(graphPayload.value.nodes, graphPayload.value.links)
}

onMounted(() => {
  initData()
  window.addEventListener('resize', onWindowResize)
})

watch(selectedNode, () => {
  if (graphPayload.value.nodes.length) {
    nextTick(() => scheduleRelayout())
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
  clearTimeout(resizeTimer)
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
  /* 门户无 TagsView，勿扣减 tags 高度，否则底部会多出一条留白 */
  height: calc(100vh - var(--layout-navbar-height, 52px));
  background-color: #ffffff;
  overflow: hidden;
  font-family: "Songti SC", "SimSun", serif;
  perspective: 2000px;
}

@supports (height: 1dvh) {
  .genealogy-container {
    height: calc(100dvh - var(--layout-navbar-height, 52px));
  }
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
  min-height: 0;

  .deck-nav {
    flex-shrink: 0;
    height: clamp(50px, 6vh, 60px);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 clamp(16px, 3vw, 40px);
    font-weight: 900;
    font-size: 12px;
    background: #fafafa;
    border-bottom: 1px solid #eee;

    .nav-left {
      display: flex;
      align-items: center;
      flex-shrink: 0;
    }

    .nav-center {
      display: flex;
      align-items: baseline;
      justify-content: center;
      flex-wrap: wrap;
      gap: 6px 10px;
      text-align: center;
      max-width: min(560px, 46vw);

      .nav-title {
        font-size: clamp(14px, 1.1vw, 16px);
        letter-spacing: 2px;
        color: #1a1a1a;
      }

      .nav-breadcrumb {
        font-size: clamp(13px, 1vw, 15px);
        letter-spacing: 1px;
        color: #5d4037;
        font-weight: 800;
      }
    }

    .hint-muted {
      font-size: 11px;
      font-weight: 700;
      color: #aaa;
      letter-spacing: 0.5px;
      max-width: 140px;
      text-align: right;
      line-height: 1.35;
    }

    @media (max-width: 640px) {
      .hint-muted { display: none; }
      .nav-center { max-width: 100%; }
    }
  }

  .deck-main {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: row;
    align-items: stretch;
  }

  .graph-stack {
    flex: 1;
    min-width: 0;
    position: relative;
    display: flex;
    flex-direction: column;
  }

  .graph-container {
    flex: 1;
    min-height: 0;
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at 50% 35%, #fff 0%, #f3f2ef 100%);
  }

  .graph-empty-hint {
    position: absolute;
    left: 50%;
    bottom: clamp(88px, 14vh, 120px);
    transform: translateX(-50%);
    z-index: 50;
    pointer-events: none;
    font-size: 12px;
    font-weight: 700;
    color: rgba(93, 64, 55, 0.45);
    letter-spacing: 2px;
    white-space: nowrap;
  }

  .color-legend--top {
    position: absolute;
    top: clamp(10px, 1.5vh, 16px);
    right: clamp(12px, 2vw, 24px);
    z-index: 55;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px 16px;
    padding: 8px 14px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 999px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    .legend-inline {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 11px;
      font-weight: 700;
      color: #555;
    }

    .legend-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;
    }
  }

  .node-detail-panel {
    flex-shrink: 0;
    width: min(300px, 88vw);
    box-sizing: border-box;
    padding: clamp(16px, 2vh, 24px);
    border-left: 1px solid #eceae4;
    background: linear-gradient(180deg, #fdfcfa 0%, #faf8f4 100%);
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;

    .panel-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 10px;
    }

    .panel-kicker {
      font-size: 10px;
      letter-spacing: 3px;
      font-weight: 900;
      color: #999;
    }

    .panel-close {
      border: none;
      background: transparent;
      font-size: 22px;
      line-height: 1;
      cursor: pointer;
      color: #bbb;
      padding: 0 4px;
      &:hover { color: #333; }
    }

    .panel-level {
      margin-bottom: 10px;
      :deep(.el-tag) { font-size: 11px; }
    }

    .panel-title {
      margin: 0 0 14px 0;
      font-size: clamp(1.15rem, 2vw, 1.35rem);
      font-weight: 900;
      color: #1a1a1a;
      line-height: 1.35;
    }

    .panel-intro {
      font-size: 13px;
      color: #555;
      line-height: 1.85;
      margin-bottom: 20px;
    }

    .panel-footnote {
      font-size: 11px;
      color: #aaa;
      line-height: 1.6;
      margin: 0;
      padding-top: 12px;
      border-top: 1px dashed #e5e2dc;
    }
  }

  .detail-slide-enter-active,
  .detail-slide-leave-active {
    transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.3s ease;
  }

  .detail-slide-enter-from,
  .detail-slide-leave-to {
    opacity: 0;
    transform: translateX(12px);
  }

  @media (max-width: 768px) {
    .deck-main {
      flex-direction: column;
    }

    .node-detail-panel {
      width: 100%;
      max-height: min(42vh, 320px);
      border-left: none;
      border-top: 1px solid #eceae4;
    }

    .graph-empty-hint {
      bottom: auto;
      top: clamp(52px, 10vh, 72px);
      white-space: normal;
      text-align: center;
      max-width: 90%;
    }

    .color-legend--top {
      left: 12px;
      right: 12px;
      justify-content: center;
      border-radius: 12px;
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
          background: var(--hui-primary, #409eff);
          box-shadow: 0 2px 10px rgba(64, 158, 255, 0.35);

          .tab-index {
            color: rgba(255, 255, 255, 0.85);
          }

          .tab-label {
            color: #fff;
          }
        }
      }
    }
  }
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