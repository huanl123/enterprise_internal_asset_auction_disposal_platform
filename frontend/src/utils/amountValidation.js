export const MONEY_VALIDATION_MESSAGE = '金额必须大于0且最多保留2位小数'

export const validatePositiveMoney = (value) => {
  if (value === null || value === undefined || value === '') return false
  const text = String(value).trim()
  if (!/^\d+(\.\d{1,2})?$/.test(text)) return false
  return Number(text) > 0
}
