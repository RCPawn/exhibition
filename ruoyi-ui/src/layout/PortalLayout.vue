<template>
  <div class="portal-container">
    <header class="portal-header">
      <div class="header-inner">
        <div class="logo-box" @click="router.push('/display/home')">
          <div class="logo-seal">白族</div>
          <span class="logo-text">非遗档案</span>
        </div>

        <div class="header-center">
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
        </div>

        <div class="user-actions">
          <PortalRealmSwitcher surface="light" class="user-actions__switcher" />
          <template v-if="isLoggedIn">
            <el-dropdown trigger="hover" :show-timeout="80" :hide-timeout="220" @command="handleCommand">
              <div class="user-profile">
                <img :src="userStore?.avatar || defaultAvatar" class="user-avatar" alt="" />
                <span class="user-name">{{ userStore?.nickName || userStore?.name || '用户' }}</span>
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
          </template>
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
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import defaultAvatar from '@/assets/images/profile.jpg'
import PortalRealmSwitcher from '@/components/PortalRealmSwitcher.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { token } = storeToRefs(userStore)
const isLoggedIn = computed(() => !!token.value)

const isExhibitNavActive = computed(() => {
  const p = route.path
  return p === '/display/gallery' || p.startsWith('/display/detail/')
})

function handleCommand(command) {
  switch (command) {
    case 'profile': router.push('/display/profile'); break
    case 'collection': router.push('/display/collection'); break
    case 'publish': router.push('/display/my-publish'); break
    case 'logout': handleLogout(); break
    default: break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      router.replace({ path: '/display/home' })
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
    /* 左右各 1fr、中间 auto：主导航相对整条顶栏几何居中；避免右侧「前台/后台」+ 用户区变宽时导航被挤向左侧 */
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
    align-items: center;
    gap: 12px;
  }
}

/* 中间导航在 grid 第二列，相对视口居中，不受左右栏宽度差影响 */
.header-center {
  grid-column: 2;
  justify-self: center;
  min-width: 0;
  max-width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

.logo-box {
  grid-column: 1;
  justify-self: start;
  flex-shrink: 0;
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

.user-actions {
  grid-column: 3;
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-actions__switcher {
  margin-right: 2px;
}

@media screen and (max-width: 1180px) {
  .portal-header {
    height: auto;
    min-height: var(--layout-navbar-height, 52px);
    padding-top: 8px;
    padding-bottom: 8px;
  }
  .header-inner {
    grid-template-columns: 1fr auto;
    grid-template-rows: auto auto;
    row-gap: 8px;
  }
  .logo-box {
    grid-column: 1;
    grid-row: 1;
  }
  .user-actions {
    grid-column: 2;
    grid-row: 1;
    justify-self: end;
  }
  .header-center {
    grid-column: 1 / -1;
    grid-row: 2;
    width: 100%;
    justify-self: stretch;
    justify-content: center;
    padding-top: 4px;
  }
  .main-nav {
    flex-wrap: wrap;
    justify-content: center;
    margin-top: 0;
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 15px;
  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: var(--navbar-avatar-radius, 6px);
    border: 1px solid $ink-black;
    object-fit: cover;
    flex-shrink: 0;
  }
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
