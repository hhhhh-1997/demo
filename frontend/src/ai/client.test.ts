import { describe, it, expect } from 'vitest'
import { accumulateToolCalls } from './client'
import type { ToolCallDelta } from './client'

describe('accumulateToolCalls', () => {
  it('按 index 合并分片参数', () => {
    const deltas: ToolCallDelta[] = [
      { index: 0, id: 'call_1', function: { name: 'top_units', arguments: '{"n":' } },
      { index: 0, function: { arguments: '2}' } },
    ]
    const out = accumulateToolCalls([], deltas)
    expect(out).toHaveLength(1)
    expect(out[0].function.arguments).toBe('{"n":2}')
    expect(out[0].function.name).toBe('top_units')
  })
})
