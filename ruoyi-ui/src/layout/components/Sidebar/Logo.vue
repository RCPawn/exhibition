
<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <!-- 1. 折叠状态 (Collapse)：没有图片logo时，显示简短文字 -->
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <h1 class="sidebar-title">非遗</h1>
      </router-link>

      <!-- 2. 展开状态 (Expand)：显示完整平台名称 -->
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <h1 class="sidebar-title">{{ title }}</h1>
      </router-link>
    </transition>

    <!-- 3. 底部装饰线：银色渐变，增加精致感 -->
    <div class="bottom-line"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

// 这里定义你的系统名称
// 建议不要用 VITE_APP_TITLE 那个太长了，直接写个好听的短名字
const title = ref('白族非遗平台');

</script>

<style lang="scss" scoped>
/* 动画效果 */
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 60px; /* 加高一点，更大气 */
  line-height: 60px;
  /* 核心：强制使用深靛蓝背景，保持与侧边栏一致 */
  background: #182640;
  text-align: center;
  overflow: hidden;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      /* 纯白文字，配合深蓝背景 */
      color: #ffffff;
      font-weight: 500; /* 字体不宜太粗，体现极简 */
      line-height: 50px;
      font-size: 16px; /* 展开时字号 */
      font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
      vertical-align: middle;
      letter-spacing: 2px; /* 增加字间距，更有设计感 */
    }
  }

  /* 折叠时的样式微调 */
  &.collapse {
    .sidebar-title {
      font-size: 14px; /* 折叠时字稍微小一点 */
      letter-spacing: 0px;
      font-weight: bold;
    }
  }

  /* 底部银色分割线：去若依化的关键细节 */
  .bottom-line {
    position: absolute;
    bottom: 0;
    left: 10px;
    right: 10px;
    height: 1px;
    /* 透明 -> 半透明白 -> 透明，营造光泽感 */
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  }
}
</style>



<!--
<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 v-else class="sidebar-title">{{ title }}</h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title">{{ title }}</h1>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
// import logo from '@/assets/logo/logo.png'
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = import.meta.env.VITE_APP_TITLE
const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

// 获取Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(&#45;&#45;sidebar-bg)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightBg
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取Logo文字颜色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(&#45;&#45;sidebar-text)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightText
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 50px;
  line-height: 50px;
  background: v-bind(getLogoBackground);
  text-align: center;
  overflow: hidden;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: v-bind(getLogoTextColor);
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>-->
