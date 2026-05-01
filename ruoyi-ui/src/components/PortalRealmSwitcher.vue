<template>
  <div
    v-if="!isLoggedIn || isBackstage"
    class="prs"
    :class="['prs--' + surface, { 'prs--backstage': isBackstage }]"
  >
    <nav
      v-if="isLoggedIn && isBackstage"
      class="prs-seg"
      :aria-label="t.ariaSeg"
    >
      <router-link
        class="prs-seg__btn"
        active-class=""
        exact-active-class=""
        :class="{ 'is-active': isPortalRoute }"
        to="/display/home"
      >{{ t.front }}</router-link>
      <router-link
        class="prs-seg__btn"
        active-class=""
        exact-active-class=""
        :class="{ 'is-active': !isPortalRoute }"
        to="/index"
      >{{ t.back }}</router-link>
    </nav>

    <div v-else-if="!isLoggedIn" class="prs-guest">
      <router-link
        class="prs-guest__link"
        :to="{ path: '/login', query: { redirect: redirectTarget } }"
      >{{ t.login }}</router-link>
      <span class="prs-guest__sep" aria-hidden="true" />
      <router-link class="prs-guest__link prs-guest__link--register" to="/register">{{ t.register }}</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { isBackstageUser } from '@/utils/backstage'

defineProps({
  surface: {
    type: String,
    default: 'light',
    validator: (v) => ['light', 'dark'].includes(v)
  }
})

const t = {
  ariaSeg: '\u524d\u53f0\u4e0e\u540e\u53f0\u5207\u6362',
  front: '\u524d\u53f0',
  back: '\u540e\u53f0',
  login: '\u767b\u5f55',
  register: '\u6ce8\u518c'
}

const route = useRoute()
const userStore = useUserStore()
const { token } = storeToRefs(userStore)

const isLoggedIn = computed(() => !!token.value)
const isBackstage = computed(() => isBackstageUser())
const isPortalRoute = computed(() => route.path.startsWith('/display'))
const redirectTarget = computed(() => route.fullPath)
</script>

<style lang="scss" scoped>
.prs {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  font-family:
    'PingFang SC',
    'Microsoft YaHei',
    'Noto Sans SC',
    system-ui,
    -apple-system,
    sans-serif;
  letter-spacing: normal;
}

.prs-seg {
  display: inline-flex;
  align-items: stretch;
  border-radius: 999px;
  padding: 2px;
  gap: 0;
  border: 1px solid rgba(26, 26, 26, 0.2);
  background: rgba(255, 255, 255, 0.75);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.prs--dark .prs-seg {
  border-color: rgba(255, 255, 255, 0.22);
  background: rgba(0, 0, 0, 0.22);
  box-shadow: none;
}

.prs-seg__btn {
  text-decoration: none;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: normal;
  padding: 6px 14px;
  border-radius: 999px;
  color: #3a3a3a;
  transition:
    color 0.2s ease,
    background 0.2s ease;
  line-height: 1.25;
  white-space: nowrap;

  &:hover {
    color: #0d0d0d;
    background: rgba(26, 26, 26, 0.08);
  }

  &.is-active {
    color: #fff;
    background: #141414;
  }

  &.router-link-active:not(.is-active) {
    color: #2d2d2d;
  }
}

.prs--dark .prs-seg__btn {
  color: rgba(255, 255, 255, 0.88);

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.12);
  }

  &.is-active {
    color: #111;
    background: #fff;
  }

  &.router-link-active:not(.is-active) {
    color: rgba(255, 255, 255, 0.92);
  }
}

.prs-guest {
  display: inline-flex;
  align-items: center;
  gap: 0;
  padding: 2px 0;
}

.prs-guest__link {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: normal;
  text-decoration: none;
  color: #2c2c2c;
  padding: 6px 10px;
  border-radius: 8px;
  transition:
    color 0.2s ease,
    background 0.2s ease;

  &:hover {
    color: #000;
    background: rgba(26, 26, 26, 0.06);
  }
}

.prs-guest__link--register {
  font-weight: 700;
  color: #111;
  border: 1px solid rgba(20, 20, 20, 0.35);
  background: rgba(255, 255, 255, 0.85);
  margin-left: 4px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 999px;

  &:hover {
    border-color: #141414;
    background: #fff;
  }
}

.prs--dark .prs-guest__link {
  color: rgba(255, 255, 255, 0.92);

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
  }
}

.prs--dark .prs-guest__link--register {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.08);

  &:hover {
    border-color: rgba(255, 255, 255, 0.75);
    background: rgba(255, 255, 255, 0.14);
  }
}

.prs-guest__sep {
  width: 1px;
  height: 14px;
  margin: 0 4px;
  background: rgba(26, 26, 26, 0.18);
  border-radius: 1px;
}

.prs--dark .prs-guest__sep {
  background: rgba(255, 255, 255, 0.28);
}
</style>
