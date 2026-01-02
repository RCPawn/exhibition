<template>
  <div class="app-container">
    <div class="graph-container" v-loading="loading" element-loading-text="正在构建知识网络...">
      <div class="graph-header">
        <h2 class="title">非遗技艺知识图谱 / KNOWLEDGE GRAPH</h2>
        <p class="subtitle">数字化呈现白族非遗技艺流派与核心传承人的谱系关联</p>
      </div>
      <!-- 使用 ref 替代 ID 更加 Vue3 化 -->
      <div ref="chartRef" class="chart-box"></div>
    </div>
  </div>
</template>

<script setup name="InheritMap">
import * as echarts from 'echarts';
import { onMounted, ref, onUnmounted } from 'vue';
import { getKnowledgeGraph } from "@/api/heritage/inheritor";

const chartRef = ref(null);
const loading = ref(false); // ✅ 修复：定义 loading 变量
let myChart = null;

/** 初始化图表 */
const initChart = async () => {
  if (!chartRef.value) return;

  myChart = echarts.init(chartRef.value);
  loading.value = true;

  try {
    const res = await getKnowledgeGraph();
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          if (params.dataType === 'node') {
            return `名称: ${params.data.name}`;
          }
          return `传承关系`;
        }
      },
      legend: [{
        data: ['技艺门类', '核心传承人'],
        orient: 'vertical',
        left: 'right',
        top: 'bottom'
      }],
      series: [
        {
          type: 'graph',
          layout: 'force',
          data: res.data.nodes,
          links: res.data.links,
          categories: [
            { name: '技艺门类' },
            { name: '核心传承人' }
          ],
          roam: true,
          label: {
            show: true,
            position: 'top',
            color: '#000',
            fontWeight: 'bold'
          },
          force: {
            repulsion: 1000, // 增加排斥力，防止节点重叠
            edgeLength: [100, 200]
          },
          lineStyle: {
            color: '#333',
            width: 2,
            curveness: 0.1
          },
          emphasis: {
            focus: 'adjacency',
            lineStyle: { width: 5 }
          },
          itemStyle: {
            // 工业风配色：分类黑，人金
            color: (params) => params.data.category === 0 ? '#000' : '#ffd700',
            borderColor: '#000',
            borderWidth: 1
          }
        }
      ]
    };
    myChart.setOption(option);
  } catch (error) {
    console.error("加载图谱失败", error);
  } finally {
    loading.value = false;
  }
};

/** 监听窗口缩放 */
const handleResize = () => {
  myChart && myChart.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (myChart) {
    myChart.dispose();
  }
});
</script>

<style scoped lang="scss">
.graph-container {
  height: calc(100vh - 120px);
  padding: 30px;
  background: #fff;
  display: flex;
  flex-direction: column;
  border: 1px solid #eee;
}
.graph-header {
  border-left: 5px solid #000;
  padding-left: 20px;
  margin-bottom: 30px;
  .title { font-size: 24px; font-weight: 900; margin: 0; }
  .subtitle { font-size: 13px; color: #999; margin-top: 8px; }
}
.chart-box {
  flex: 1;
  width: 100%;
  min-height: 500px;
}
</style>