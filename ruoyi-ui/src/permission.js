/*
import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /!* has token*!/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route) // 动态添加可访问路由表
              }
            })
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
*/
import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { isBackstageUser } from '@/utils/backstage'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

/** 未登录可访问的门户展示页（不含个人中心、收藏、发布等） */
const guestPortalPatterns = [
    '/display/home',
    '/display/gallery',
    '/display/genealogy',
    '/display/acoustic',
    '/display/images',
    '/display/detail/*',
    '/display/images/detail/*'
]

const isWhiteList = (path) => {
    return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isGuestPortalPath = (path) => {
    return guestPortalPatterns.some(pattern => isPathMatch(pattern, path))
}

/** 登录页已执行 getInfo 后调用：注册动态路由（否则 roles 已存在时 permission 会跳过 generateRoutes） */
export function registerDynamicRoutesAfterLogin() {
    return usePermissionStore().generateRoutes().then(accessRoutes => {
        accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
                router.addRoute(route)
            }
        })
    })
}

router.beforeEach((to, from, next) => {
    NProgress.start()
    if (getToken()) {
        to.meta.title && useSettingsStore().setTitle(to.meta.title)

        /* 1. 登录后访问登录页：回系统根（经路由重定向至 /index），再由守卫按角色分流 */
        if (to.path === '/login') {
            next({ path: '/' })
            NProgress.done()
        } else if (isWhiteList(to.path)) {
            next()
        } else {
            /* 2. 判断是否已拉取完用户信息 */
            if (useUserStore().roles.length === 0) {
                isRelogin.show = true
                useUserStore().getInfo().then(() => {
                    isRelogin.show = false
                    usePermissionStore().generateRoutes().then(accessRoutes => {
                        // 根据roles权限生成可访问的路由表
                        accessRoutes.forEach(route => {
                            if (!isHttp(route.path)) {
                                router.addRoute(route)
                            }
                        })

                        // === 核心分流逻辑 A：首次登录/刷新时的分流（与若依角色/权限约定一致）===
                        if (!isBackstageUser() && (to.path === '/index' || to.path === '/')) {
                            next({ path: '/display/home', replace: true })
                        } else {
                            next({ ...to, replace: true })
                        }
                        // === 逻辑结束 ===
                    })
                }).catch(err => {
                    useUserStore().logOut().then(() => {
                        ElMessage.error(err)
                        next({ path: '/' })
                    })
                })
            } else {
                // === 核心分流逻辑 B：日常跳转时的权限隔离 ===
                if (!isBackstageUser() && (to.path === '/index' || to.path === '/')) {
                    next({ path: '/display/home', replace: true })
                } else {
                    next()
                }
            }
        }
    } else {
        // 没有token：登录/注册白名单、门户公开展示、根路径进门户首页，其余进登录页
        if (isWhiteList(to.path)) {
            next()
        } else if (to.path === '/' || to.path === '/index') {
            next({ path: '/display/home', replace: true })
            NProgress.done()
        } else if (isGuestPortalPath(to.path)) {
            to.meta.title && useSettingsStore().setTitle(to.meta.title)
            next()
        } else {
            next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})