<template>
  <div class="portal-container">
    <!-- 顶部极简导航 -->
    <header class="portal-header">
      <div class="header-inner">
        <!-- Logo区 -->
        <div class="logo-box" @click="router.push('/display/home')">
          <span class="logo-text">白族非遗</span>
          <span class="logo-badge">Center</span>
        </div>

        <!-- 主菜单 -->
        <nav class="main-nav">
          <router-link to="/display/home" class="nav-item">首页</router-link>
          <router-link to="/display/gallery" class="nav-item">在线展厅</router-link>
          <router-link to="/display/genealogy" class="nav-item">传承图谱</router-link>
        </nav>

        <!-- 用户操作区 -->
        <div class="user-actions">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile">
              <img :src="userStore.avatar" class="user-avatar" />
              <span class="user-name">{{ userStore.name }}</span>
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

    <!-- 内容主体 -->
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
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { ArrowDown, User, Star, Edit, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'collection':
      router.push('/display/collection') // 对应你之前的收藏页面
      break
    case 'publish':
      router.push('/display/my-publish') // 对应你之前的发布页面
      break
    case 'logout':
      handleLogout()
      break
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
.portal-container {
  /* 加上固定定位或绝对定位的强制覆盖，确保它是全屏最上层 */
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  z-index: 1500; /* 高于若依原生侧边栏 */
  overflow: hidden;
}

.portal-header {
  height: 70px;
  border-bottom: 2px solid #000;
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 0 40px;

  .header-inner {
    max-width: 1400px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.logo-box {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  .logo-text { font-size: 20px; font-weight: 900; letter-spacing: -1px; }
  .logo-badge {
    background: #000; color: #fff; font-size: 10px; padding: 2px 6px; font-weight: 900;
  }
}

.main-nav {
  display: flex;
  gap: 40px;
  .nav-item {
    text-decoration: none;
    color: #999;
    font-size: 13px;
    font-weight: 900;
    transition: color 0.3s;
    &:hover, &.router-link-active {
      color: #000;
      &::after { content: ''; display: block; height: 2px; background: #000; margin-top: 4px; }
    }
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 5px 15px;
  transition: background 0.3s;
  &:hover { background: #f5f5f5; }

  .user-avatar { width: 32px; height: 32px; border-radius: 0; border: 1px solid #000; }
  .user-name { font-size: 13px; font-weight: 900; }
}

.portal-main {
  flex: 1;
  overflow-y: auto; /* 仅内容区允许滚动 */
  background: #fff;
}

/* 下拉菜单直角化 */
:deep(.el-dropdown-menu) {
  border-radius: 0 !important;
  border: 2px solid #000 !important;
  padding: 0;
  .el-dropdown-menu__item {
    padding: 12px 20px;
    font-weight: 700;
    &:hover { background-color: #000 !important; color: #fff !important; }
  }
}
</style>