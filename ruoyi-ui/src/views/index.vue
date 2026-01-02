<template>
  <div class="data-screen-container" v-loading="loading" element-loading-background="rgba(24, 38, 64, 0.9)">

    <!-- 背景层 -->
    <div class="screen-bg">
      <div class="indigo-glow"></div>
      <div class="grid-texture"></div>
    </div>

    <!-- 顶部 -->
    <header class="screen-header">
      <div class="header-line left"></div>
      <div class="header-title">
        <span class="cn">白族非遗 · 数字孪生仪表盘</span>
      </div>
      <div class="header-line right"></div>
      <div class="header-time">{{ currentTime }}</div>
    </header>

    <main class="screen-body">

      <!-- === 左侧面板 === -->
      <div class="side-column">

        <!-- 1. 数字化资源构成 (真实数据) -->
        <div class="data-panel">
          <div class="panel-title">数字化资源构成</div>
          <div class="panel-body">
            <div ref="resourceChartRef" class="chart-instance"></div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>

        <!-- 2. 非遗类目分布 (保留) -->
        <div class="data-panel">
          <div class="panel-title">非遗类目分布</div>
          <div class="panel-body">
            <div ref="pieChartRef" class="chart-instance"></div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>

        <!-- 3. 最新收录 (改为列表展示) -->
        <div class="data-panel">
          <div class="panel-title">最新收录展品</div>
          <div class="panel-body latest-list">
            <div class="latest-item" v-for="(item, index) in stats.latestItems" :key="item.itemId || index">
              <!-- 封面图 -->
              <div class="item-img">
                <img :src="getAssetUrl(item.coverImage)" alt="cover" />
              </div>
              <div class="item-info">
                <div class="i-name">{{ item.itemName }}</div>
                <div class="i-date">{{ parseTime(item.createTime, '{y}-{m}-{d}') }}</div>
              </div>
              <div class="i-status">NEW</div>
            </div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>
      </div>

      <!-- === 中间舞台 === -->
      <div class="center-column">
        <div class="hud-stats">
          <div class="stat-box">
            <div class="label">非遗展品总数</div>
            <div class="value">{{ stats.totalItems || 0 }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box">
            <div class="label">累计浏览热度</div>
            <div class="value">{{ stats.totalViews || 0 }}</div>
          </div>
        </div>

        <div class="center-stage">
          <model-viewer
              v-if="stats.centerModelUrl"
              :src="getAssetUrl(stats.centerModelUrl)"
              auto-rotate
              camera-controls
              rotation-per-second="20deg"
              shadow-intensity="1.5"
              environment-image="neutral"
              class="dashboard-viewer"
          >
          </model-viewer>
          <div class="stage-ring"></div>
          <div class="model-name-card">
            <h3>{{ stats.centerModelName || '数据加载中...' }}</h3>
            <p>🔥全站热度最高🔥</p>
          </div>
        </div>
      </div>

      <!-- === 右侧面板 === -->
      <div class="side-column">

        <!-- 4. 用户交互分析 (改为横向柱图) -->
        <div class="data-panel">
          <div class="panel-title">全站交互行为</div>
          <div class="panel-body">
            <div ref="interactionChartRef" class="chart-instance"></div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>

        <!-- 5. 热门藏品 Top 5 (真实数据版) -->
        <div class="data-panel">
          <div class="panel-title">热门展品 Top 5</div>
          <div class="panel-body ranking-list">
            <!-- 遍历 stats.top5Items -->
            <div class="rank-item" v-for="(item, index) in stats.top5Items" :key="index">
              <!-- 序号颜色逻辑 -->
              <div class="rank-idx" :class="'top-'+(index+1)">{{ index + 1 }}</div>

              <div class="rank-info">
                <span class="r-name">{{ item.name }}</span>
                <!-- 进度条逻辑：当前值 / 第一名的值 * 100 -->
                <div class="r-bar-bg">
                  <div class="r-bar" :style="{ width: getPercent(item.value) + '%' }"></div>
                </div>
              </div>

              <!-- 数值：显示真实浏览量 -->
              <div class="rank-val">{{ item.value }}</div>
            </div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>

        <!-- 6. 词云 (改为气泡图展示) -->
        <div class="data-panel">
          <div class="panel-title">非遗热词云</div>
          <div class="panel-body">
            <div ref="wordCloudRef" class="chart-instance"></div>
          </div>
          <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import request from '@/utils/request';
import { parseTime } from "@/utils/ruoyi"; // 引入若依的时间格式化工具

const loading = ref(true);
const stats = ref({});
const currentTime = ref('');
let timer = null;

// 图表 Refs
const resourceChartRef = ref(null); // 左上
const pieChartRef = ref(null);      // 左中
const interactionChartRef = ref(null); // 右上
const wordCloudRef = ref(null);     // 右下

// 计算进度条百分比的辅助函数
const getPercent = (val) => {
  // 如果没有数据，返回0
  if (!stats.value.top5Items || stats.value.top5Items.length === 0) return 0;
  // 获取第一名（最大值）的值
  const maxVal = stats.value.top5Items[0].value;
  if (maxVal === 0) return 0;
  // 计算百分比
  return (val / maxVal) * 100;
};

const getAssetUrl = (url) => import.meta.env.VITE_APP_BASE_API + url;

const updateTime = () => {
  const now = new Date();
  currentTime.value = `${now.getFullYear()}.${String(now.getMonth()+1).padStart(2,'0')}.${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
};

// ECharts 通用配置
const commonOption = {
  backgroundColor: 'transparent',
  textStyle: { fontFamily: 'Microsoft YaHei, sans-serif' },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(20, 30, 50, 0.9)',
    borderColor: '#409eff',
    textStyle: { color: '#fff' }
  }
};

const initCharts = () => {
  // 1. 左上：数字化资源构成 (环形图)
  const resourceChart = echarts.init(resourceChartRef.value);
  resourceChart.setOption({
    ...commonOption,
    color: ['#00d2ff', '#36f', '#8fa0c0'],
    // --- 修改点 1：图例优化 ---
    legend: {
      show: true,
      top: '5%',       // 距离顶部 5%，留出一点呼吸感
      left: 'center',  // 水平居中
      icon: 'rect',    // ✅ 改回方形 (如果不写这行默认也是圆角矩形，写 rect 就是直角方块)
      itemWidth: 14,   // 稍微加大一点图标，更清晰
      itemHeight: 10,  // 扁平一点的矩形
      textStyle: { color: '#a0cfff', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      // --- 修改点 2：圆心上移 ---
      // X轴 50% (保持左右居中)，Y轴 55% (视觉重心最佳位置)
      center: ['50%', '55%'],
      data: stats.value.resourceComposition || [],
      itemStyle: { borderRadius: 2, borderColor: '#182640', borderWidth: 3 },
      label: { show: false },
    }, {
      // 中间仪表盘文字同步上移
      type: 'gauge',
      radius: '60%',
      // --- 修改点 3：保持一致 ---
      center: ['50%', '55%'],
      startAngle: 0, endAngle: 0,
      pointer: { show: false }, axisLine: { show: false }, axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
      detail: { show: true, formatter: '资源', fontSize: 14, color: '#fff', offsetCenter: [0, 0] },
      data: [{ value: 0 }]
    }]
  });

// 2. 左中：非遗类目分布 (恢复为单环大类统计)
  const pieChart = echarts.init(pieChartRef.value);

  pieChart.setOption({
    ...commonOption,
    color: ['#36f', '#00d2ff', '#7bffb6', '#faad14', '#f5222d'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      name: '类目分布',
      type: 'pie',
      // radius: '70%', // 如果想要实心饼图
      radius: ['40%', '70%'], // 环形图更有科技感
      center: ['50%', '50%'],
      roseType: 'radius', // 加上这个就是南丁格尔玫瑰图，不想要可以删掉
      data: stats.value.categoryPie || [],
      itemStyle: {
        borderRadius: 5,
        borderColor: '#182640',
        borderWidth: 2
      },
      label: {
        show: true,
        color: '#fff',
        fontSize: 12,
        formatter: '{b}\n{d}%'
      },
      labelLine: {
        length: 10,
        lineStyle: { color: '#409eff' }
      }
    }]
  });

  // 3. 右上：交互行为 (胶囊柱状图)
  const interactionChart = echarts.init(interactionChartRef.value);
  // 处理数据
  const interactData = stats.value.interactionStats || [];
  const yAxisData = interactData.map(item => item.name);
  const seriesData = interactData.map(item => item.value);

  interactionChart.setOption({
    ...commonOption,
    grid: { top: 10, right: 30, bottom: 20, left: 60 },
    xAxis: { show: false },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisLabel: { color: '#fff' },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      data: seriesData,
      barWidth: 12,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{offset: 0, color: '#36f'}, {offset: 1, color: '#00d2ff'}]), borderRadius: 10 },
      label: { show: true, position: 'right', color: '#fff' },
      showBackground: true,
      backgroundStyle: { color: 'rgba(255,255,255,0.05)', borderRadius: 10 }
    }]
  });

  // 4. 右下：词云 (用气泡图模拟，免去安装插件)
  const wordCloud = echarts.init(wordCloudRef.value);
  const cloudData = (stats.value.wordCloud || []).map((item, idx) => {
    return {
      name: item.name,
      value: [
        Math.random() * 100,
        Math.random() * 100,
        // ✅ 修复点：虽然气泡大小可以用小数，但如果你想显示在 tooltip 里，建议取整
        // 这里控制气泡大小
        item.value || (Math.random() * 50 + 10)
      ],
      // ✅ 新增：在数据对象里存一个整数值，用于 Label 显示
      realValue: item.value,
      itemStyle: { color: ['#409eff', '#e6a23c', '#67c23a', '#f56c6c', '#00d2ff'][idx % 5] }
    }
  });

  wordCloud.setOption({
    ...commonOption,
    grid: { top: 10, right: 10, bottom: 10, left: 10 },
    xAxis: { show: false, min: 0, max: 100 },
    yAxis: { show: false, min: 0, max: 100 },
    series: [{
      type: 'scatter',
      symbolSize: function (data) { return Math.sqrt(data[2]) * 8; }, // 根据热度调整大小
      data: cloudData,
      label: {
        show: true,
        formatter:  function(param) {
          return param.name; // 只显示名字，清爽
          // return param.name + '\n' + Math.floor(param.data.realValue);  // 显示名字+热度(整数)
        },
        color: '#fff',
        fontSize: 11,
        textShadowBlur: 2,
        textShadowColor: '#000'
      },
      itemStyle: { opacity: 0.8 }
    }]
  });

  window.addEventListener('resize', () => {
    resourceChart.resize(); pieChart.resize(); interactionChart.resize(); wordCloud.resize();
  });
};

const getData = () => {
  request({ url: '/heritage/stats/dashboard', method: 'get' }).then(res => {
    stats.value = res.data;
    loading.value = false;
    nextTick(() => initCharts());
  });
};

onMounted(() => {
  getData();
  timer = setInterval(updateTime, 1000);
  updateTime();
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<style lang="scss" scoped>
$bg-color: #182640;
$border-color: rgba(255, 255, 255, 0.15);
$primary: #409eff;

.data-screen-container {
  width: 100%; height: calc(100vh - 84px);
  background-color: $bg-color;
  position: relative; overflow: hidden;
  color: #fff;
  display: flex; flex-direction: column;
}

/* 背景、头部样式保持不变，复用之前的CSS */
.screen-bg {
  position: absolute; inset: 0; pointer-events: none; z-index: 0;
  .indigo-glow {
    width: 100%; height: 100%;
    background: radial-gradient(circle at 50% 50%, #243452 0%, #0f1521 100%);
  }
  .grid-texture {
    position: absolute; top: 0; left: 0; width: 100%; height: 100%;
    background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
    background-size: 30px 30px;
  }
}

.screen-header {
  height: 60px; display: flex; justify-content: center; align-items: center;
  position: relative; z-index: 10; gap: 20px;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), transparent);

  .header-line { width: 100px; height: 2px; background: linear-gradient(90deg, transparent, $primary, transparent); }
  .header-title {
    text-align: center;
    .cn { font-size: 24px; font-weight: 700; letter-spacing: 4px; text-shadow: 0 0 10px rgba(64,158,255,0.6); }
  }
  .header-time { position: absolute; right: 30px; color: #a0cfff; font-family: monospace; font-size: 14px; }
}

/* 主体 */
.screen-body {
  flex: 1; display: grid; grid-template-columns: 26% 48% 26%;
  gap: 20px; padding: 10px 30px 30px; z-index: 2;

  .side-column { display: flex; flex-direction: column; justify-content: space-between; }

  /* 面板样式 */
  .data-panel {
    flex: 1; margin-bottom: 20px; position: relative;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid $border-color;
    display: flex; flex-direction: column; padding: 15px;
    box-shadow: inset 0 0 20px rgba(0,0,0,0.2);
    &:last-child { margin-bottom: 0; }

    .panel-title {
      font-size: 14px; color: #fff; font-weight: bold; border-left: 3px solid $primary;
      padding-left: 10px; margin-bottom: 10px; display: flex; align-items: center;
    }
    .panel-body { flex: 1; min-height: 0; position: relative; }

    /* 四角装饰 */
    .corner {
      position: absolute; width: 8px; height: 8px; border-color: #a0cfff; border-style: solid;
      &.tl { top: -1px; left: -1px; border-width: 2px 0 0 2px; }
      &.tr { top: -1px; right: -1px; border-width: 2px 2px 0 0; }
      &.bl { bottom: -1px; left: -1px; border-width: 0 0 2px 2px; }
      &.br { bottom: -1px; right: -1px; border-width: 0 2px 2px 0; }
    }
  }

  .chart-instance { width: 100%; height: 100%; }
}

/* 最新收录列表样式 */
.latest-list {
  overflow-y: auto;
  .latest-item {
    display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05);
    .item-img {
      width: 40px; height: 40px; border-radius: 4px; overflow: hidden; margin-right: 10px; border: 1px solid rgba(255,255,255,0.1);
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .item-info { flex: 1;
      .i-name { font-size: 13px; color: #fff; margin-bottom: 2px; }
      .i-date { font-size: 11px; color: #8fa0c0; }
    }
    .i-status { font-size: 10px; background: rgba(0, 210, 255, 0.2); color: #00d2ff; padding: 2px 6px; border-radius: 2px; }
  }
}

/* 中间舞台 */
.center-column {
  display: flex; flex-direction: column; align-items: center; justify-content: center;

  .hud-stats {
    display: flex; justify-content: center; gap: 40px; margin-bottom: 10px;
    .stat-box {
      text-align: center;
      .value { font-size: 38px; font-weight: 700; color: #fff; text-shadow: 0 0 10px rgba(0,0,0,0.5); font-family: 'Impact', sans-serif; }
      .label { font-size: 12px; color: #a0cfff; letter-spacing: 1px; margin-bottom: 5px; }
    }
    .stat-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.2); }
  }

  .center-stage {
    flex: 1; width: 100%; position: relative;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    .dashboard-viewer { width: 100%; height: 85%; z-index: 5; outline: none; }
    .stage-ring {
      position: absolute; bottom: 10%; left: 50%; transform: translateX(-50%) rotateX(70deg);
      width: 320px; height: 320px; border: 2px solid rgba(64,158,255,0.3); border-radius: 50%;
      box-shadow: 0 0 40px rgba(64,158,255,0.2); animation: pulse 4s infinite;
    }
    .model-name-card {
      position: absolute; bottom: 20px; text-align: center;
      h3 { font-size: 26px; color: #fff; margin: 0; letter-spacing: 2px; }
      p { font-size: 12px; color: #a0cfff; margin: 5px 0 0; }
    }
  }
}

/* 排行榜样式保持不变 */
.ranking-list {
  .rank-item {
    display: flex; align-items: center; margin-bottom: 12px;
    .rank-idx {
      width: 20px; height: 20px; line-height: 20px; text-align: center; font-size: 12px;
      background: rgba(255,255,255,0.1); margin-right: 10px; border-radius: 2px;
      &.top-1 { background: #f56c6c; color: #fff; }
      &.top-2 { background: #e6a23c; color: #fff; }
      &.top-3 { background: #409eff; color: #fff; }
    }
    .rank-info { flex: 1; margin-right: 10px;
      .r-name { font-size: 12px; display: block; margin-bottom: 4px; color: #ddd; }
      .r-bar-bg { height: 4px; background: rgba(255,255,255,0.1); border-radius: 2px;
        .r-bar { height: 100%; background: linear-gradient(90deg, #409eff, #36cfc9); border-radius: 2px; }
      }
    }
    .rank-val { font-size: 12px; color: #fff; font-weight: bold; }
  }
}

@keyframes pulse { 0% { box-shadow: 0 0 20px rgba(64,158,255,0.1); opacity: 0.5; } 50% { box-shadow: 0 0 50px rgba(64,158,255,0.4); opacity: 1; } 100% { box-shadow: 0 0 20px rgba(64,158,255,0.1); opacity: 0.5; } }

@media (max-width: 1200px) {
  .screen-body { grid-template-columns: 1fr 1fr; overflow-y: auto; }
  .center-column { grid-column: 1 / -1; height: 450px; order: -1; }
}
</style>