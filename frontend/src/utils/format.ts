/**
 * 元 → 万元（保留 2 位小数，千位分隔）。
 *
 * @param yuan 金额（元）
 */
export function yuanToWan(yuan: number): string {
  return (yuan / 10000).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

/**
 * 元 → 亿元（保留 1 位小数）。
 *
 * @param yuan 金额（元）
 */
export function yuanToYi(yuan: number): string {
  return (yuan / 100000000).toFixed(1)
}
