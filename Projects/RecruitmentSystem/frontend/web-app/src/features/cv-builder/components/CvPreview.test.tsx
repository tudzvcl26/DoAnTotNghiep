import { useState } from 'react'
import { afterEach, beforeAll, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, within } from '@testing-library/react'
import { CvPreview } from './CvPreview'
import { sampleCv } from '../cv.templates'

afterEach(cleanup)
beforeAll(() => {
  // Match browser innerText semantics in jsdom, which has no layout engine.
  Object.defineProperty(HTMLElement.prototype, 'innerText', {
    configurable: true, get() { return this.textContent ?? '' },
    set(value: string) { this.textContent = value },
  })
})

it('duplicates, reorders and removes skill items through shared history-aware controls', () => {
  const checkpoint = vi.fn()
  function Editor() {
    const [content, setContent] = useState({ ...structuredClone(sampleCv), skills: ['Java', 'Tiếng Việt'] })
    return <CvPreview content={content} templateId="classic" editor={{ onChange: setContent, onCheckpoint: checkpoint }} />
  }
  render(<Editor />)
  const skill = (n: number) => screen.getByRole('textbox', { name: `Kỹ năng ${n}` }).parentElement!
  fireEvent.click(within(skill(1)).getByRole('button', { name: 'Nhân bản mục' }))
  expect(screen.getByRole('textbox', { name: 'Kỹ năng 2' }).textContent).toBe('Java')
  fireEvent.click(within(skill(3)).getByRole('button', { name: 'Di chuyển lên' }))
  expect(screen.getByRole('textbox', { name: 'Kỹ năng 2' }).textContent).toBe('Tiếng Việt')
  fireEvent.click(within(skill(1)).getByRole('button', { name: 'Xóa mục' }))
  expect(screen.getByRole('textbox', { name: 'Kỹ năng 1' }).textContent).toBe('Tiếng Việt')
  expect(screen.queryByRole('textbox', { name: 'Kỹ năng 3' })).toBeNull()
  expect(checkpoint).toHaveBeenCalledTimes(3)
})
