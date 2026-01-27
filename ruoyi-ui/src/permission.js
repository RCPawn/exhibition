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

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
    return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
    NProgress.start()
    if (getToken()) {
        to.meta.title && useSettingsStore().setTitle(to.meta.title)

        /* 1. 登录后访问登录页，跳转到根路径 */
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

                        // === 核心分流逻辑 A：首次登录/刷新时的分流 ===
                        const roles = useUserStore().roles;
                        const isAdmin = roles.includes('admin'); // 判断是否为管理员

                        // 如果不是管理员，且试图进入系统根目录或数据大屏，则导向门户首页
                        if (!isAdmin && (to.path === '/index' || to.path === '/')) {
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
                const roles = useUserStore().roles;
                const isAdmin = roles.includes('admin');

                // 防止普通用户通过修改 URL 强行进入 /index
                if (!isAdmin && (to.path === '/index' || to.path === '/')) {
                    next({ path: '/display/home', replace: true })
                } else {
                    next()
                }
            }
        }
    } else {
        // 没有token
        if (isWhiteList(to.path)) {
            next()
        } else {
            next(`/login?redirect=${to.fullPath}`)
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})