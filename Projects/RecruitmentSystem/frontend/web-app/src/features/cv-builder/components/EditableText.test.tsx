import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { EditableText } from './EditableText'

beforeAll(() => {
  // jsdom has no layout engine / innerText implementation.
  Object.defineProperty(HTMLElement.prototype, 'innerText', {
    configurable: true, get() { return this.textContent ?? '' },
    set(value: string) { this.textContent = value },
  })
})
afterEach(cleanup)

describe('inline editing regressions', () => {
  it('does not recommit cancelled text when Escape triggers blur', () => {
    const onChange = vi.fn()
    render(<EditableText value="Nguyễn Văn An" placeholder="" label="Tên" onChange={onChange} />)
    const field = screen.getByRole('textbox')
    field.focus(); field.innerText = 'Nội dung bị hủy'; fireEvent.input(field)
    fireEvent.keyDown(field, { key: 'Escape' })
    expect(onChange).toHaveBeenLastCalledWith('Nguyễn Văn An')
    expect(field.innerText).toBe('Nguyễn Văn An')
  })
  it('applies external undo and redo even while focused', () => {
    const props = { placeholder: '', label: 'Họ và tên', onChange: vi.fn() }
    const { rerender } = render(<EditableText {...props} value="Nguyễn Văn An" />)
    const field = screen.getByRole('textbox')
    field.focus()
    rerender(<EditableText {...props} value="" />)
    expect(field.innerText).toBe('')
    rerender(<EditableText {...props} value="Nguyễn Văn An" />)
    expect(field.innerText).toBe('Nguyễn Văn An')
    expect(document.activeElement).toBe(field)
  })

  it('does not replace the text node or selection on a local input echo', () => {
    const props = { placeholder: '', label: 'Tên', onChange: vi.fn() }
    const { rerender } = render(<EditableText {...props} value="An" />)
    const field = screen.getByRole('textbox')
    field.focus()
    field.innerText = 'Nguyễn Văn An'
    const textNode = field.firstChild
    const range = document.createRange()
    range.setStart(textNode!, 7); range.collapse(true)
    window.getSelection()?.removeAllRanges(); window.getSelection()?.addRange(range)
    fireEvent.input(field)
    rerender(<EditableText {...props} value="Nguyễn Văn An" />)
    expect(field.firstChild).toBe(textNode)
    expect(window.getSelection()?.anchorOffset).toBe(7)
  })

  it('waits for composition end and does not treat IME Enter as blur', () => {
    const onChange = vi.fn()
    render(<EditableText value="" placeholder="" label="Tên" onChange={onChange} />)
    const field = screen.getByRole('textbox')
    field.focus(); fireEvent.compositionStart(field)
    field.innerText = 'Nguyễ'; fireEvent.input(field)
    fireEvent.keyDown(field, { key: 'Enter', isComposing: true, keyCode: 229 })
    expect(document.activeElement).toBe(field)
    expect(onChange).not.toHaveBeenCalled()
    field.innerText = 'Nguyễn'; fireEvent.compositionEnd(field)
    expect(onChange).toHaveBeenLastCalledWith('Nguyễn')
  })

  it('commits a completed single-line edit when Enter ends editing', () => {
    const onChange = vi.fn()
    const onEditEnd = vi.fn()
    render(<EditableText value="Nguyễn" placeholder="" label="Tên" onChange={onChange} onEditEnd={onEditEnd} />)
    const field = screen.getByRole('textbox')
    field.focus(); field.innerText = 'Nguyễn Đặng Ánh'; fireEvent.input(field)
    fireEvent.keyDown(field, { key: 'Enter' })
    expect(document.activeElement).not.toBe(field)
    expect(onChange).toHaveBeenLastCalledWith('Nguyễn Đặng Ánh')
    expect(onEditEnd).toHaveBeenCalledTimes(1)
  })

  it('does not commit an unfinished composition when blur arrives before compositionend', () => {
    const onChange = vi.fn()
    const onEditEnd = vi.fn()
    const props = { placeholder: '', label: 'Tên', onChange, onEditEnd }
    const { rerender } = render(<EditableText {...props} value="Ng" />)
    const field = screen.getByRole('textbox')
    field.focus(); fireEvent.compositionStart(field)
    field.innerText = 'Nguyễ'; fireEvent.compositionUpdate(field)
    field.blur()
    expect(onChange).not.toHaveBeenCalled()
    rerender(<EditableText {...props} value="Giá trị cũ từ máy chủ" />)
    expect(field.innerText).toBe('Nguyễ')
    field.innerText = 'Nguyễn'; fireEvent.compositionEnd(field)
    expect(onChange).toHaveBeenCalledExactlyOnceWith('Nguyễn')
    expect(onEditEnd).toHaveBeenCalledTimes(1)
  })

  it('pastes plain text, preserves Unicode and removes multiline from single-line fields', () => {
    const onChange = vi.fn()
    render(<EditableText value="" placeholder="" label="Tên" onChange={onChange} />)
    const field = screen.getByRole('textbox')
    field.focus()
    const range = document.createRange(); range.selectNodeContents(field)
    window.getSelection()?.removeAllRanges(); window.getSelection()?.addRange(range)
    fireEvent.paste(field, { clipboardData: { getData: () => 'Nguyễn\r\nVăn An' } })
    expect(field.innerText).toBe('Nguyễn Văn An')
    expect(field.querySelector('*')).toBeNull()
    expect(onChange).toHaveBeenLastCalledWith('Nguyễn Văn An')
  })
})
