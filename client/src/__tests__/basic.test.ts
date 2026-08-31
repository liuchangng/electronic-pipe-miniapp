/**
 * 基础测试 - 验证 vitest 环境
 */
import { describe, it, expect } from 'vitest'

describe('vitest 环境', () => {
  it('应正确运行测试', () => {
    expect(1 + 1).toBe(2)
  })

  it('应支持异步测试', async () => {
    const result = await Promise.resolve('hello')
    expect(result).toBe('hello')
  })
})