<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div
      v-if="settingsStore.tagsView"
      class="navbar-tags-wrap"
      :class="'tags-pos-nav' + settingsStore.navType"
    >
      <tags-view embedded />
    </div>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
<!--        <header-search id="header-search" class="right-menu-item" />

        <el-tooltip content="源码地址" effect="dark" placement="bottom">
          <ruo-yi-git id="ruoyi-git" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="文档地址" effect="dark" placement="bottom">
          <ruo-yi-doc id="ruoyi-doc" class="right-menu-item hover-effect" />
        </el-tooltip>

        <screenfull id="screenfull" class="right-menu-item hover-effect" />-->

<!--        <el-tooltip content="主题模式" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect theme-switch-wrapper" @click="toggleTheme">
            <svg-icon v-if="settingsStore.isDark" icon-class="sunny" />
            <svg-icon v-if="!settingsStore.isDark" icon-class="moon" />
          </div>
        </el-tooltip>-->

<!--        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>-->
      </template>

      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname"> {{ userStore.nickName }} </span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
                <span>布局设置</span>
              </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import TopNav from '@/components/TopNav'
import TopBar from './TopBar'
import TagsView from './TagsView'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case "setLayout":
      setLayout()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function toggleTheme() {
  settingsStore.toggleTheme()
}
</script>

<style lang='scss' scoped>
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: var(--layout-navbar-height, 60px);
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  box-shadow: var(--navbar-shadow, 0 1px 3px rgba(0, 0, 0, 0.08));
  display: flex;
  align-items: center;
  padding: 0 4px 0 6px;
  box-sizing: border-box;

  .hamburger-container {
    line-height: var(--layout-navbar-height, 60px);
    height: 100%;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    margin-right: 8px;

    &:hover {
      background: var(--navbar-row-hover, rgba(0, 0, 0, 0.06));
    }

    :deep(svg) {
      fill: var(--navbar-text);
    }
  }

  /* 页签占据中间剩余宽度 */
  .navbar-tags-wrap {
    flex: 1;
    min-width: 0;
    height: 100%;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 10px;
  }

  /* 混合导航：顶部菜单绝对定位，页签让出中间区域 */
  .navbar-tags-wrap.tags-pos-nav2 {
    position: absolute;
    left: 200px;
    right: 200px;
    top: 0;
    margin-left: 0;
    width: auto;
    max-width: none;
  }

  .navbar-tags-wrap.tags-pos-nav3 {
    flex: 1;
    min-width: 0;
    max-width: 40%;
    margin-left: 6px;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    height: 100%;
    max-height: var(--layout-navbar-height, 60px);
    line-height: var(--layout-navbar-height, 60px);
    display: flex;
    align-items: center;
    margin-left: auto;
    flex-shrink: 0;
    padding: 10px 8px 10px 12px;
    box-sizing: border-box;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0 10px;
      min-height: 44px;
      font-size: 18px;
      color: var(--navbar-text);
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: var(--navbar-row-hover, rgba(0, 0, 0, 0.06));
        }
      }

      &.theme-switch-wrapper {
        display: flex;
        align-items: center;

        svg {
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.15);
          }
        }
      }
    }

    .avatar-container {
      margin-right: 0;
      padding: 6px 12px 6px 8px;
      display: inline-flex;
      align-items: center;
      align-self: center;
      height: auto;
      max-height: 100%;
      border-radius: 8px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        right: 0;
        position: relative;
        gap: 8px;
        padding: 2px 0;

        .user-avatar {
          cursor: pointer;
          width: 34px;
          height: 34px;
          margin-right: 0;
          border-radius: 50%;
          border: 2px solid var(--navbar-avatar-ring, rgba(0, 0, 0, 0.12));
          flex-shrink: 0;
        }

        .user-nickname{
          position: relative;
          left: 0;
          font-size: 14px;
          font-weight: 600;
          line-height: 1.35;
          color: var(--navbar-text);
          max-width: 12em;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          display: inline-block;
          vertical-align: middle;
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
