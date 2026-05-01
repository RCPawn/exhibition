import useUserStore from '@/store/modules/user'
import { normalizeToStringArray } from '@/utils/normalizeRuoYiRes'

/**
 * Whether the current user should use the admin console (aligned with RuoYi backend).
 * - Super admin: permissions include "*:*:*", or role "admin", or userId === 1
 * - Same idea as super_admin / all_permission in src/plugins/auth.js
 */
export function isBackstageUser() {
  const store = useUserStore()
  const permissions = normalizeToStringArray(store.permissions)
  if (permissions.includes('*:*:*')) return true

  const roles = normalizeToStringArray(store.roles)
  if (roles.includes('admin')) return true

  const id = store.id
  if (id !== '' && id !== undefined && id != null && String(id) === '1') return true

  return false
}

/**
 * Post-login landing path (call after getInfo so roles/permissions are in store).
 * If redirect is /display/... and user is admin, send them to /index instead of staying on portal.
 */
export function resolveLoginRedirect(redirect) {
  const raw = redirect == null ? '' : (Array.isArray(redirect) ? redirect[0] : redirect)
  const raw0 = String(raw).trim()
  const r = (raw0.split('#')[0] || '')

  if (isBackstageUser()) {
    if (!r || r === '/' || r.startsWith('/index') || r.startsWith('/display')) {
      return '/index'
    }
    if (r.startsWith('/login') || r.startsWith('/register')) {
      return '/index'
    }
    return r.startsWith('/') ? r : `/${r}`
  }

  if (!r || r === '/' || r.startsWith('/index')) {
    return '/display/home'
  }
  if (r.startsWith('/display')) {
    return r
  }
  if (r.startsWith('/login') || r.startsWith('/register')) {
    return '/display/home'
  }
  return '/display/home'
}
