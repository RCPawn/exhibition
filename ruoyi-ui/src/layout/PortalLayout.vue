<template>
  <div class="portal-container">
    <header class="portal-header">
      <div class="header-inner">
        <div class="logo-box" @click="router.push('/display/home')">
          <div class="logo-seal">白族</div>
          <span class="logo-text">非遗档案</span>
        </div>

        <nav class="main-nav">
          <router-link to="/display/home" class="nav-item">首页</router-link>
          <router-link
            to="/display/gallery"
            class="nav-item"
            :class="{ 'router-link-active': isExhibitNavActive }"
          >在线展厅</router-link>
          <router-link to="/display/acoustic" class="nav-item">三道余音</router-link>
          <router-link to="/display/images" class="nav-item">纸上乾坤</router-link>
          <router-link to="/display/genealogy" class="nav-item">传承图谱</router-link>
        </nav>

        <div class="user-actions">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile">
              <img :src="userStore?.avatar || defaultAvatar" class="user-avatar" />
              <span class="user-name">{{ userStore?.name || '未登录' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="industrial-dropdown">
                <el-dropdown-item command="profile" icon="User">个人信息</el-dropdown-item>
                <el-dropdown-item command="collection" icon="Star">我的收藏</el-dropdown-item>
                <el-dropdown-item command="publish" icon="Edit">我的发布</el-dropdown-item>
                <el-dropdown-item divided command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="portal-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { ArrowDown, User, Star, Edit, SwitchButton } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/images/profile.jpg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

/** 展品详情与展厅列表同属「在线展厅」，避免详情页无高亮与悬浮下划线突然出现导致跳动 */
const isExhibitNavActive = computed(() => {
  const p = route.path
  return p === '/display/gallery' || p.startsWith('/display/detail/')
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/display/profile'); break
    case 'collection': router.push('/display/collection'); break
    case 'publish': router.push('/display/my-publish'); break
    case 'logout': handleLogout(); break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/login';
    })
  })
}
</script>

<style lang="scss" scoped>
$brand-blue: #339af0;
$ink-black: #1A1A1A;

.portal-container {
  width: 100%;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  position: relative;
  /* 子页（如展品详情）用同一变量扣减顶栏高度 */
  --layout-navbar-height: 52px;
}

.portal-header {
  height: var(--layout-navbar-height, 52px);
  flex-shrink: 0;
  border-bottom: 1px solid rgba(26, 26, 26, 0.1);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  padding: 0 clamp(12px, 2.5vw, 40px);

  .header-inner {
    max-width: min(1680px, 100%);
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
}

.logo-box {
  cursor: pointer;
  display: flex; align-items: center; gap: 8px;
  .logo-seal {
    background: $brand-blue;
    color: #fff; padding: 2px 6px; font-weight: 900; font-size: 16px; border-radius: 2px;
  }
  .logo-text { font-size: 18px; font-weight: 900; color: $ink-black; }
}

.main-nav {
  display: flex;
  gap: clamp(10px, 1.8vw, 40px);
  .nav-item {
    text-decoration: none;
    color: #999;
    font-size: clamp(11px, 0.85vw, 13px);
    font-weight: 900;
    transition: color 0.2s ease;
    padding: 5px 0;
    white-space: nowrap;
    /* 下划线占位，避免仅悬浮时出现条带导致布局跳动 */
    &::after {
      content: '';
      display: block;
      height: 2px;
      margin-top: 4px;
      background: transparent;
      transition: background-color 0.2s ease;
    }
    &:hover {
      color: $ink-black;
    }
    &:hover::after {
      background: $ink-black;
    }
    &.router-link-active {
      color: $ink-black;
    }
    &.router-link-active::after {
      background: $ink-black;
    }
  }
}

@media screen and (max-width: 1180px) {
  .portal-header {
    height: auto;
    min-height: var(--layout-navbar-height, 52px);
    padding-top: 8px;
    padding-bottom: 8px;
  }
  .header-inner {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  .logo-box {
    order: 1;
  }
  .user-actions {
    order: 2;
    margin-left: auto;
  }
  .main-nav {
    order: 3;
    flex-basis: 100%;
    justify-content: center;
    margin-top: 6px;
  }
}

.user-profile {
  display: flex; align-items: center; gap: 12px; cursor: pointer; padding: 5px 15px;
  .user-avatar { width: 32px; height: 32px; border: 1px solid $ink-black; }
  .user-name { font-size: 13px; font-weight: 900; color: $ink-black; }
}

/* 主内容区：占满顶栏以下剩余高度（flex 子项），具体 min-height 由各子页自理 */
.portal-main {
  flex: 1;
  min-height: 0;
  background: #fff;
}

// 下拉菜单样式
:deep(.el-dropdown-menu) {
  border-radius: 0 !important;
  border: 2px solid $ink-black !important;
  padding: 0;
  .el-dropdown-menu__item {
    padding: 12px 20px; font-weight: 700;
    &:hover { background-color: $ink-black !important; color: #fff !important; }
  }
}

// 页面切换动画
.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.4s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-15px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(15px); }
</style>
