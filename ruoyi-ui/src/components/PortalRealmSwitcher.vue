<template>
  <div
    v-if="!isLoggedIn || isBackstage"
    class="prs-root"
  >
    <el-radio-group
      v-if="isLoggedIn && isBackstage"
      :model-value="realmTab"
      class="prs-realm"
      :class="{ 'prs-realm--dark': surface === 'dark' }"
      size="small"
      :aria-label="REALM_SWITCH_LABELS.aria"
      @change="onRealmChange"
    >
      <el-radio-button label="portal">
        {{ REALM_SWITCH_LABELS.front }}
      </el-radio-button>
      <el-radio-button label="admin">
        {{ REALM_SWITCH_LABELS.back }}
      </el-radio-button>
    </el-radio-group>

    <div v-else-if="!isLoggedIn" class="hui-aux-links hui-aux-links--register">
      <router-link
        :to="{ path: '/login', query: { redirect: redirectTarget } }"
      >{{ t.login }}</router-link>
      <span class="hui-aux-links__sep" aria-hidden="true" />
      <router-link to="/register">{{ t.register }}</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { isBackstageUser } from '@/utils/backstage'
import { REALM_SWITCH_LABELS } from '@/constants/portalNavLabels'

defineProps({
  surface: {
    type: String,
    default: 'light',
    validator: (v) => ['light', 'dark'].includes(v)
  }
})

const t = {
  login: '\u767b\u5f55',
  register: '\u6ce8\u518c'
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { token } = storeToRefs(userStore)

const isLoggedIn = computed(() => !!token.value)
const isBackstage = computed(() => isBackstageUser())
const isPortalRoute = computed(() => route.path.startsWith('/display'))
const redirectTarget = computed(() => route.fullPath)

const realmTab = computed(() => (isPortalRoute.value ? 'portal' : 'admin'))

function onRealmChange(val) {
  if (val === 'portal') {
    router.push('/display/home')
    return
  }
  if (val === 'admin') {
    router.push('/index')
  }
}
</script>

<style lang="scss" scoped>
.prs-root {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* Element Plus radio-button group: compact pill, aligns with admin navbar */
.prs-realm {
  flex-shrink: 0;
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: stretch;
  border-radius: 8px;
  padding: 2px;
  background: var(--el-fill-color-light, #f5f7fa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);

  :deep(.el-radio-button) {
    margin: 0;

    &:first-child .el-radio-button__inner {
      border-radius: 6px 2px 2px 6px;
    }

    &:last-child .el-radio-button__inner {
      border-radius: 2px 6px 6px 2px;
    }
  }

  :deep(.el-radio-button__inner) {
    border: none !important;
    padding: 5px 14px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.02em;
    line-height: 1.35;
    color: var(--el-text-color-secondary, #909399);
    background: transparent;
    box-shadow: none !important;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    color: var(--el-color-primary, #409eff);
    background: var(--el-bg-color, #fff);
    /* 避免投影造成「选中项变大」的错觉，与未选项同轮廓 */
    box-shadow: none !important;
  }

  :deep(.el-radio-button__original-radio:not(:checked) + .el-radio-button__inner:hover) {
    color: var(--el-text-color-primary, #303133);
    background: rgba(255, 255, 255, 0.55);
  }
}

.prs-realm--dark {
  background: rgba(15, 23, 42, 0.35);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);

  :deep(.el-radio-button__inner) {
    color: rgba(255, 255, 255, 0.72) !important;
    background: transparent !important;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    color: var(--el-color-primary, #409eff) !important;
    background: rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    transform: none !important;
  }

  :deep(.el-radio-button__original-radio:not(:checked) + .el-radio-button__inner:hover) {
    color: #fff !important;
    background: rgba(255, 255, 255, 0.08) !important;
  }
}
</style>
