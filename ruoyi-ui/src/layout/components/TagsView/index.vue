<template>
  <div
    id="tags-view-container"
    :class="['tags-view-container', { 'tags-view-container--embedded': embedded }]"
  >
    <scroll-pane ref="scrollPaneRef" class="tags-view-wrapper" :embedded="embedded" @scroll="handleScroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :data-path="tag.path"
        :class="{ 'active': isActive(tag), 'has-icon': tagsIcon }"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        class="tags-view-item"
        :style="activeStyle(tag)"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        <svg-icon v-if="tagsIcon && tag.meta && tag.meta.icon && tag.meta.icon !== '#'" :icon-class="tag.meta.icon" />
        {{ tag.title }}
        <span v-if="!isAffix(tag)" @click.prevent.stop="closeSelectedTag(tag)">
          <close class="el-icon-close" style="width: 1em; height: 1em;vertical-align: middle;" />
        </span>
      </router-link>
    </scroll-pane>
    <Teleport to="body">
      <ul
        v-show="visible"
        class="tags-view-contextmenu"
        :style="{ left: left + 'px', top: top + 'px' }"
        @click.stop
      >
        <li @click="refreshSelectedTag(selectedTag)">
          <refresh-right style="width: 1em; height: 1em;" /> 刷新页面
        </li>
        <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
          <close style="width: 1em; height: 1em;" /> 关闭当前
        </li>
        <li @click="closeOthersTags">
          <circle-close style="width: 1em; height: 1em;" /> 关闭其他
        </li>
        <li v-if="!isFirstView()" @click="closeLeftTags">
          <back style="width: 1em; height: 1em;" /> 关闭左侧
        </li>
        <li v-if="!isLastView()" @click="closeRightTags">
          <right style="width: 1em; height: 1em;" /> 关闭右侧
        </li>
        <li @click="closeAllTags(selectedTag)">
          <circle-close style="width: 1em; height: 1em;" /> 全部关闭
        </li>
      </ul>
    </Teleport>
  </div>
</template>

<script setup>
import ScrollPane from './ScrollPane'
import { getNormalPath } from '@/utils/ruoyi'
import useTagsViewStore from '@/store/modules/tagsView'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

defineProps({
  /** 为 true 时表示嵌在顶栏一行内（无独立标签条） */
  embedded: {
    type: Boolean,
    default: false
  }
})

const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})
const affixTags = ref([])
const scrollPaneRef = ref(null)

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const visitedViews = computed(() => useTagsViewStore().visitedViews)
const routes = computed(() => usePermissionStore().routes)
const theme = computed(() => useSettingsStore().theme)
const tagsIcon = computed(() => useSettingsStore().tagsIcon)

watch(route, () => {
  addTags()
  moveToCurrentTag()
})

watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

onMounted(() => {
  initTags()
  addTags()
})

function isActive(r) {
  return r.path === route.path
}

function activeStyle(tag) {
  if (!isActive(tag)) return {}
  return {
    "background-color": theme.value,
    "border-color": theme.value
  }
}

function isAffix(tag) {
  return tag.meta && tag.meta.affix
}

function isFirstView() {
  try {
    return selectedTag.value.fullPath === '/index' || selectedTag.value.fullPath === visitedViews.value[1].fullPath
  } catch (err) {
    return false
  }
}

function isLastView() {
  try {
    return selectedTag.value.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}

function filterAffixTags(routes, basePath = '') {
  let tags = []
  routes.forEach(route => {
    if (route.meta && route.meta.affix) {
      const tagPath = getNormalPath(basePath + '/' + route.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: route.name,
        meta: { ...route.meta }
      })
    }
    if (route.children) {
      const tempTags = filterAffixTags(route.children, route.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })
  return tags
}

function initTags() {
  const res = filterAffixTags(routes.value)
  affixTags.value = res
  for (const tag of res) {
    // Must have tag name
    if (tag.name) {
       useTagsViewStore().addVisitedView(tag)
    }
  }
}

function addTags() {
  const { name } = route
  if (name) {
    useTagsViewStore().addView(route)
  }
}

function moveToCurrentTag() {
  nextTick(() => {
    for (const r of visitedViews.value) {
      if (r.path === route.path) {
        scrollPaneRef.value.moveToTarget(r)
        // when query is different then update
        if (r.fullPath !== route.fullPath) {
          useTagsViewStore().updateVisitedView(route)
        }
      }
    }
  })
}

function refreshSelectedTag(view) {
  proxy.$tab.refreshPage(view)
  if (route.meta.link) {
    useTagsViewStore().delIframeView(route)
  }
}

function closeSelectedTag(view) {
  proxy.$tab.closePage(view).then(({ visitedViews }) => {
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function closeRightTags() {
  proxy.$tab.closeRightPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeLeftTags() {
  proxy.$tab.closeLeftPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeOthersTags() {
  router.push(selectedTag.value).catch(() => { })
  proxy.$tab.closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

function closeAllTags(view) {
  proxy.$tab.closeAllPage().then(({ visitedViews }) => {
    if (affixTags.value.some(tag => tag.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}

function toLastView(visitedViews, view) {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    // now the default is to redirect to the home page if there is no tags-view,
    // you can adjust it according to your needs.
    if (view.name === 'Dashboard') {
      // to reload home page
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      router.push('/')
    }
  }
}

function openMenu(tag, e) {
  const menuMinWidth = 168
  const menuEstHeight = 260
  let x = e.clientX
  let y = e.clientY

  if (x + menuMinWidth > window.innerWidth - 6) {
    x = window.innerWidth - menuMinWidth - 6
  }
  if (x < 6) x = 6
  if (y + menuEstHeight > window.innerHeight - 6) {
    y = Math.max(6, window.innerHeight - menuEstHeight - 6)
  }
  if (y < 6) y = 6

  left.value = x
  top.value = y
  visible.value = true
  selectedTag.value = tag
}

function closeMenu() {
  visible.value = false
}

function handleScroll() {
  closeMenu()
}
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: var(--tags-bg, #182640);
  border-bottom: 1px solid var(--tags-item-border, rgba(255, 255, 255, 0.15));
  box-shadow: none;

  &.tags-view-container--embedded {
    height: 100%;
    width: 100%;
    background: transparent;
    border-bottom: none;
    display: flex;
    align-items: center;
    min-height: 0;
  }

  .tags-view-wrapper {
    .tags-view-item {
      display: inline-block;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid var(--tags-item-border, rgba(255, 255, 255, 0.15));
      color: var(--tags-item-text, #bfcbd9);
      background: var(--tags-item-bg, #0f1828);
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;

      &:first-of-type {
        margin-left: 15px;
      }

      &:last-of-type {
        margin-right: 15px;
      }

      &:hover {
        background: var(--tags-item-hover, #121c30);
      }

      &.active {
        background-color: #409eff;
        color: #fff;
        border-color: #409eff;

        &::before {
          content: '';
          background: #fff;
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          position: relative;
          margin-right: 5px;
        }
      }
    }
  }

  .tags-view-item.active.has-icon::before {
    content: none !important;
  }

  &.tags-view-container--embedded .tags-view-wrapper .tags-view-item {
    margin-top: 0;
    vertical-align: middle;
    height: 30px;
    line-height: 28px;
    padding: 0 10px 0 12px;
    border-radius: 8px;
    margin-left: 6px;
    font-size: 12px;
    letter-spacing: 0.02em;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;

    &:first-of-type {
      margin-left: 2px;
    }

    &:last-of-type {
      margin-right: 10px;
    }

    &.active {
      /* 与未选中页签同高、同字重，仅颜色区分 */
      font-weight: 400;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

      &::before {
        width: 6px;
        height: 6px;
        margin-right: 6px;
      }
    }
  }
}
</style>

<style lang="scss">
/* 挂到 body，避免顶栏 overflow 裁剪；使用视口坐标 */
.tags-view-contextmenu {
  position: fixed;
  z-index: 5000;
  margin: 0;
  min-width: 160px;
  background: #fff;
  list-style: none;
  padding: 6px 0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 400;
  color: #303133;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;

  li {
    margin: 0;
    padding: 9px 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    white-space: nowrap;

    &:hover {
      background: #f5f7fa;
      color: var(--el-color-primary, #409eff);
    }
  }
}

//reset element css of el-icon-close
.tags-view-wrapper {
  .tags-view-item {
    .el-icon-close {
      width: 16px;
      height: 16px;
      vertical-align: 2px;
      border-radius: 50%;
      text-align: center;
      transition: all .3s cubic-bezier(.645, .045, .355, 1);
      transform-origin: 100% 50%;

      &:before {
        transform: scale(.6);
        display: inline-block;
        vertical-align: -3px;
      }

      &:hover {
        background-color: var(--tags-close-hover, rgba(255, 255, 255, 0.3));
        color: #fff;
        width: 12px !important;
        height: 12px !important;
      }
    }
  }
}
</style>