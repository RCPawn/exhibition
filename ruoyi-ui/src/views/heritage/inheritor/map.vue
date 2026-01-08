<template>
  <div class="genealogy-container" v-loading="loading" element-loading-background="rgba(242, 240, 235, 1)">
    <!-- 1. 沉浸式背景 -->
    <div class="paper-bg"></div>
    <div class="vignette-overlay"></div>

    <!-- 🌟 场景 A: 【绝对原版】3D 书本核心入口区 -->
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
            auto-rotate
            camera-orbit="45deg 60deg 2.8m"
            class="industrial-model"
            disable-zoom
        >
          <div class="hotspot-hint" slot="hotspot-center" data-position="0 0.1 0">
            <div class="hint-dot"></div>
            <span class="hint-text">点击查阅</span>
          </div>
        </model-viewer>
      </div>
    </div>

    <!-- 🌟 场景 B: 【原版】翻书转场特效 -->
    <div class="page-turn-overlay" v-if="isEntering && !showDeck">
      <div class="page-leaf"></div>
    </div>

    <!-- 🌟 场景 C: 数字化卷宗控制台 -->
    <transition name="fade-slow" @after-enter="initGraphOnEnter">
      <div class="archive-deck" v-if="showDeck">
        <!-- ✅ 修改：导航文案全面去英文 -->
        <nav class="deck-nav">
          <div class="nav-left" @click="handleCloseDeck">
            <el-icon><ArrowLeft /></el-icon> <span>返回封面 / BACK</span>
          </div>
          <div class="nav-center">非遗谱系关联网络</div>
          <div class="nav-right">数据状态：已同步</div>
        </nav>

        <main class="deck-body">
          <aside class="axis-nav">
            <div class="nav-item"
                 v-for="(cat, index) in categoryTree"
                 :key="cat.categoryId"
                 :class="{ active: activeCatId === cat.categoryId }"
                 @click="handleSelectCategory(cat)">
              <span class="index">0{{ index + 1 }}</span>
              <span class="label">{{ cat.categoryName }}</span>
            </div>
          </aside>

          <!-- 轴二：可视化图谱 -->
          <section class="axis-graph">
            <div ref="graphRef" class="graph-canvas"></div>
            <!-- ✅ 修改：图例去英文 -->
            <div class="graph-legend">
              <div class="l-item"><span class="dot cat"></span> 技艺项目</div>
              <div class="l-item"><span class="dot master"></span> 核心传承人</div>
            </div>
          </section>

          <!-- 轴三：节点详细档案 (重点优化区) -->
          <transition name="fade">
            <aside class="axis-detail" v-if="selectedNode">
              <!-- ✅ 修改：减小顶部空间，移除标识边框，标识中文化 -->
              <div class="detail-header">
                <div class="node-type-plain">{{ selectedNode.category === 0 ? '技艺项目' : '传承人' }}</div>
                <div class="name-row">
                  <h2 class="node-name">{{ selectedNode.name }}</h2>
                  <dict-tag v-if="selectedNode.category === 1" :options="heritage_level" :value="selectedNode.level" class="level-tag" />
                </div>
              </div>

              <div class="detail-content">
                <div class="info-group">
                  <!-- ✅ 修改：去英文 -->
                  <label>档案摘要</label>
                  <div class="p-intro" v-html="selectedNode.intro || '正在检索数字化历史档案...'"></div>
                </div>
              </div>
              <div class="watermark">{{ selectedNode.name }}</div>
            </aside>
          </transition>
        </main>
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
import { ArrowLeft, Pointer } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { heritage_level } = proxy.useDict('heritage_level')

const loading = ref(true)
const isEntering = ref(false)
const showDeck = ref(false)
const categoryTree = ref([])
const activeCatId = ref(null)
const selectedNode = ref(null)
const graphRef = ref(null)
let myChart = null

let startX = 0, startY = 0, startTime = 0
const modelSrc = computed(() => import.meta.env.VITE_APP_BASE_API + "/profile/model/book.glb")

const initData = async () => {
  loading.value = true
  try {
    const res = await treeselect()
    categoryTree.value = res.data.slice(0, 4)
  } catch (e) { console.error(e) } finally { loading.value = false }
}

function handleMouseDown(e) { startX = e.clientX; startY = e.clientY; startTime = Date.now() }
function handleMouseUp(e) {
  if (isEntering.value) return
  const diffX = Math.abs(e.clientX - startX)
  const diffY = Math.abs(e.clientY - startY)
  if (diffX < 10 && diffY < 10 && (Date.now() - startTime) < 300) {
    isEntering.value = true
    setTimeout(() => { showDeck.value = true }, 800)
  }
}

const initGraphOnEnter = () => {
  nextTick(() => { if (categoryTree.value.length > 0) handleSelectCategory(categoryTree.value[0]) })
}

function handleCloseDeck() {
  showDeck.value = false
  if (myChart) { myChart.dispose(); myChart = null }
  setTimeout(() => { isEntering.value = false }, 500)
}

const handleSelectCategory = async (cat) => {
  activeCatId.value = cat.categoryId
  selectedNode.value = { name: cat.categoryName, category: 0, intro: '请在左侧点击节点查看详细传承档案。' }
  const nodes = [{ id: 'root', name: cat.categoryName, category: 0, symbolSize: 70 }]
  const links = []
  if(cat.children) {
    cat.children.forEach(child => {
      nodes.push({ id: `c_${child.categoryId}`, name: child.categoryName, category: 0, symbolSize: 45 })
      links.push({ source: 'root', target: `c_${child.categoryId}` })
    })
  }
  const res = await listInheritor({ categoryId: cat.categoryId })
  if(res.rows) {
    res.rows.forEach(master => {
      nodes.push({ id: `m_${master.inheritorId}`, name: master.name, category: 1, symbolSize: 55, level: master.level, intro: master.introduction })
      links.push({ source: `c_${master.categoryId}`, target: `m_${master.inheritorId}` })
    })
  }
  renderGraph(nodes, links)
}

const renderGraph = (nodes, links) => {
  if (!graphRef.value) return
  if (myChart) myChart.dispose()
  myChart = echarts.init(graphRef.value)
  myChart.setOption({
    animationDuration: 1500,
    series: [{
      type: 'graph', layout: 'force', data: nodes, links: links, roam: true,
      label: { show: true, position: 'bottom', color: '#000', fontWeight: 'bold', fontSize: 13 },
      force: { repulsion: 2000, gravity: 0.1, edgeLength: 150 },
      lineStyle: { color: '#000', width: 1.5, curveness: 0.1 },
      itemStyle: { color: (p) => p.data.category === 0 ? '#000' : '#FFD700', borderColor: '#000', borderWidth: 2 },
      emphasis: { focus: 'adjacency', lineStyle: { width: 5 } }
    }]
  })
  myChart.on('click', (params) => { selectedNode.value = params.data })
  myChart.resize()
}

onMounted(initData)
onUnmounted(() => { if (myChart) myChart.dispose() })
</script>

<style scoped lang="scss">
/* --- 样式完全还原你的原始满意版 --- */
$bg-color: #F2F0EB;
$primary-blue: #1A4A7F;
$wood-brown: #5D4037;
$seal-red: #C62828;
$accent: #FFD700;

.genealogy-container {
  position: relative; width: 100%; height: calc(100vh - 84px);
  background-color: $bg-color; overflow: hidden;
  font-family: "Songti SC", "SimSun", serif;
  perspective: 2000px;
}
.paper-bg { position: absolute; inset: 0; background-image: url('https://www.transparenttextures.com/patterns/cream-paper.png'); opacity: 0.8; z-index: 0; }
.vignette-overlay { position: absolute; inset: 0; background: radial-gradient(circle at center, transparent 60%, rgba(0,0,0,0.1) 100%); z-index: 1; pointer-events: none; }

.book-stage {
  position: relative; width: 100%; height: 100%; z-index: 10;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  transition: transform 1.2s cubic-bezier(0.25, 1, 0.5, 1);
}
.stage-zoom-in { transform: scale(3) translateY(10%); pointer-events: none; }
.header-section { text-align: center; margin-bottom: -50px; transition: opacity 0.5s ease; transform: translateY(-40px); }
.header-section.fade-out { opacity: 0; }
.main-title {
  color: #1A4A7F; font-size: 4rem; margin: 0; display: flex; align-items: center; justify-content: center; gap: 20px;
  .seal { background: #C62828; color: #fff; font-size: 1.5rem; padding: 4px 10px; border-radius: 4px; box-shadow: 2px 2px 5px rgba(0,0,0,0.2); font-weight: normal; }
  .text { font-weight: 900; letter-spacing: 10px; }
}
.subtitle { color: #5D4037; font-size: 1.2rem; letter-spacing: 15px; margin-top: 15px; opacity: 0.7; font-weight: bold; }
.model-wrapper { width: 100%; max-width: 900px; height: 60vh; position: relative; z-index: 11; cursor: pointer; overflow: visible !important; }
.industrial-model { width: 100%; height: 100%; --poster-color: transparent; }
.hotspot-hint { display: flex; align-items: center; gap: 10px; transform: translateY(-50px); opacity: 0.8; }
.hint-dot { width: 12px; height: 12px; background-color: #C62828; border-radius: 50%; box-shadow: 0 0 10px #C62828; animation: pulse 2s infinite; }
.hint-text { color: #5D4037; font-weight: bold; font-size: 14px; background: rgba(255,255,255,0.6); padding: 4px 8px; border-radius: 4px; }
.instruction { margin-top: 1rem; color: #1A4A7F; font-size: 0.9rem; opacity: 0.6; &.fade-out { opacity: 0; } }

.page-turn-overlay {
  position: absolute; top: 0; right: 0; width: 0; height: 100%;
  background: linear-gradient(to right, #e3e0d6, #fff 10%, #fff); z-index: 50;
  animation: pageFlip 1.2s cubic-bezier(0.645, 0.045, 0.355, 1) forwards;
}
@keyframes pageFlip { 0% { width: 0; } 100% { width: 100%; } }

.archive-deck {
  position: absolute; inset: 0; z-index: 60; display: flex; flex-direction: column; background: #fff;
  .deck-nav {
    height: 60px; display: flex; justify-content: space-between; align-items: center;
    padding: 0 40px; border-bottom: 2px solid #000; font-weight: 900; font-size: 12px;
    .nav-left { cursor: pointer; user-select: none; display: flex; align-items: center; gap: 8px; &:hover { color: #999; } }
  }
  .deck-body {
    flex: 1; display: flex; overflow: hidden;
    .axis-nav {
      width: 260px; border-right: 1px solid #eee; background: #fafafa;
      .nav-item { padding: 30px 40px; cursor: pointer; transition: all 0.3s; border-bottom: 1px solid #eee; .index { font-size: 10px; color: #bbb; display: block; } .label { font-size: 16px; font-weight: 900; } &.active { background: #000; color: #fff; border-left: 5px solid $accent; } }
    }
    .axis-graph { flex: 1; position: relative; .graph-canvas { width: 100%; height: 100%; }
      .graph-legend { position: absolute; bottom: 30px; left: 30px; font-size: 11px; font-weight: 900; display: flex; gap: 20px; .dot { display: inline-block; width: 8px; height: 8px; border: 1px solid #000; margin-right: 5px; } .dot.cat { background: #000; } .dot.master { background: $accent; } }
    }
    .axis-detail {
      width: 380px; border-left: 1px solid #000; padding: 30px; position: relative; // ✅ 修复：顶部空间缩小
      background: #fff; display: flex; flex-direction: column;

      .name-row {
        display: flex; align-items: center; gap: 12px; margin-bottom: 25px;
        border-bottom: 4px solid $accent; padding-bottom: 8px;
        .node-name { font-size: 32px; font-weight: 900; margin: 0; color: #000; }
        .level-tag { margin-top: 5px; }
      }

      // ✅ 修复：去方框化的标识
      .node-type-plain {
        font-size: 11px; color: #999; font-weight: 900;
        margin-bottom: 5px; letter-spacing: 2px;
      }

      .p-intro {
        font-size: 15px; line-height: 2.2; color: #444; max-height: 550px; overflow-y: auto;
        padding-right: 5px;
        :deep(p) { margin-bottom: 15px; }
        :deep(img) { width: 100%; border-radius: 2px; margin: 10px 0; }
        &::-webkit-scrollbar { width: 2px; }
        &::-webkit-scrollbar-thumb { background: #eee; }
      }

      .info-group label { display: block; font-size: 11px; font-weight: 900; color: #ccc; margin-bottom: 12px; letter-spacing: 1px; }
      .watermark { position: absolute; bottom: 80px; right: -20px; font-size: 80px; font-weight: 900; color: rgba(0,0,0,0.02); transform: rotate(90deg); pointer-events: none; }
    }
  }
}

@keyframes pulse { 0% { transform: scale(0.8); opacity: 0.5; } 100% { transform: scale(0.8); opacity: 0.5; } }
</style>