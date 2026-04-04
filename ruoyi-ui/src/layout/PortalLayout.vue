<!--<template>
  <div class="portal-container">
    <header class="portal-header">
      <div class="header-inner">
        <div class="logo-box" @click="router.push('/display/home')">
          <div class="logo-seal">白族</div>
          <span class="logo-text">非遗档案</span>
        </div>

        <nav class="main-nav">
          <router-link to="/display/home" class="nav-item">首页</router-link>
          <router-link to="/display/gallery" class="nav-item">在线展厅</router-link>
          <router-link to="/display/genealogy" class="nav-item">传承图谱</router-link>
          <router-link to="/display/acoustic" class="nav-item">三道余音</router-link>
          <router-link to="/display/images" class="nav-item">纸上乾坤</router-link>
&lt;!&ndash;          <router-link to="/display/images" class="nav-item">技艺卷轴</router-link>&ndash;&gt;
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
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { ArrowDown, User, Star, Edit, SwitchButton } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/images/profile.jpg'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/user/profile'); break
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
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  display: flex; flex-direction: column;
  background-color: #fff;
  z-index: 1500;
  overflow: hidden;
}

.portal-header {
  height: 60px;
  border-bottom: 2px solid $ink-black;
  position: absolute; /* 改为绝对定位，确保它浮在内容上方，毛玻璃才透得出内容 */
  top: 0; left: 0; right: 0;
  z-index: 2000;

  /* -&#45;&#45; 毛玻璃核心代码 -&#45;&#45; */
  background: rgba(255, 255, 255, 0.6); /* 调低透明度至 0.6 */
  backdrop-filter: blur(20px) saturate(180%); /* 增加模糊半径到 20px */
  -webkit-backdrop-filter: blur(20px) saturate(180%); /* 兼容 Safari */
  /* -&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45; */

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
  display: flex; align-items: center; gap: 8px;
  .logo-seal {
    background: $brand-blue;
    color: #fff; padding: 2px 6px; font-weight: 900; font-size: 16px; border-radius: 2px;
  }
  .logo-text { font-size: 18px; font-weight: 900; color: $ink-black; }
  .logo-badge { background: $ink-black; color: #fff; font-size: 9px; padding: 1px 5px; font-weight: 900; }
}

.main-nav {
  display: flex; gap: 40px;
  .nav-item {
    text-decoration: none; color: #999; font-size: 13px; font-weight: 900;
    transition: all 0.3s; padding: 5px 0;
    &:hover, &.router-link-active {
      color: $ink-black;
      &::after { content: ''; display: block; height: 2px; background: $ink-black; margin-top: 4px; }
    }
  }
}

.user-profile {
  display: flex; align-items: center; gap: 12px; cursor: pointer; padding: 5px 15px;
  .user-avatar { width: 32px; height: 32px; border: 1px solid $ink-black; }
  .user-name { font-size: 13px; font-weight: 900; color: $ink-black; }
}

.portal-main {
  flex: 1;
  padding-top: 60px; /* 必须给内容区加 60px 的顶部填充，否则内容会被 header 遮挡 */
  overflow-y: auto;
  background: #fff;
}

/* 下拉菜单 */
:deep(.el-dropdown-menu) {
  border-radius: 0 !important;
  border: 2px solid $ink-black !important;
  padding: 0;
  .el-dropdown-menu__item {
    padding: 12px 20px; font-weight: 700;
    &:hover { background-color: $ink-black !important; color: #fff !important; }
  }
}

/* 动画 */
.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.4s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-15px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(15px); }
</style>-->
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
          <router-link to="/display/gallery" class="nav-item">在线展厅</router-link>
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
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { ArrowDown, User, Star, Edit, SwitchButton } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/images/profile.jpg'

const router = useRouter()
const userStore = useUserStore()

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

// ✅ 核心修复：完全参考后台标准写法，删除所有全屏fixed/超高z-index
// 仅做正常流式布局，绝不覆盖后台
.portal-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  position: relative;
  /* 彻底删除：fixed、absolute、overflow: hidden、z-index:1500 */
}

// ✅ 核心修复：头部改为普通定位，仅在前台组件内显示，不全局悬浮
.portal-header {
  height: 60px;
  border-bottom: 2px solid $ink-black;
  /* 删除所有绝对/固定定位，仅内部布局 */
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
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
  display: flex; align-items: center; gap: 8px;
  .logo-seal {
    background: $brand-blue;
    color: #fff; padding: 2px 6px; font-weight: 900; font-size: 16px; border-radius: 2px;
  }
  .logo-text { font-size: 18px; font-weight: 900; color: $ink-black; }
}

.main-nav {
  display: flex; gap: 40px;
  .nav-item {
    text-decoration: none; color: #999; font-size: 13px; font-weight: 900;
    transition: all 0.3s; padding: 5px 0;
    &:hover, &.router-link-active {
      color: $ink-black;
      &::after { content: ''; display: block; height: 2px; background: $ink-black; margin-top: 4px; }
    }
  }
}

.user-profile {
  display: flex; align-items: center; gap: 12px; cursor: pointer; padding: 5px 15px;
  .user-avatar { width: 32px; height: 32px; border: 1px solid $ink-black; }
  .user-name { font-size: 13px; font-weight: 900; color: $ink-black; }
}

.portal-main {
  flex: 1;
  overflow-y: auto;
  background: #fff;
}

// 下拉菜单样式（保持不变）
:deep(.el-dropdown-menu) {
  border-radius: 0 !important;
  border: 2px solid $ink-black !important;
  padding: 0;
  .el-dropdown-menu__item {
    padding: 12px 20px; font-weight: 700;
    &:hover { background-color: $ink-black !important; color: #fff !important; }
  }
}

// 页面切换动画（保持不变）
.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.4s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-15px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(15px); }
</style>
