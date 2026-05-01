/**
 * Normalize RuoYi roles / permissions (array, object map, etc.) to string[].
 */
export function normalizeToStringArray(val) {
  if (val == null) return []
  if (Array.isArray(val)) return val.map((x) => String(x))
  if (typeof val === 'object') return Object.values(val).map((x) => String(x))
  return [String(val)]
}
