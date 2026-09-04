import { type KeyboardEvent, type ClipboardEvent, type FormEvent, useCallback, useLayoutEffect, useRef } from 'react'

type EditableTextProps = {
  value: string
  placeholder: string
  label: string
  multiline?: boolean
  className?: string
  onChange: (value: string) => void
  onEditStart?: () => void
  onEditEnd?: () => void
}

export function EditableText({ value, placeholder, label, multiline = false, className = '', onChange, onEditStart, onEditEnd }: EditableTextProps) {
  const elementRef = useRef<HTMLElement | null>(null)
  const initialValue = useRef(value)
  const latestValue = useRef(value)
  const composing = useRef(false)
  latestValue.current = value

  const setElementRef = useCallback((element: HTMLElement | null) => {
    elementRef.current = element
    if (element) element.innerText = latestValue.current
  }, [])

  useLayoutEffect(() => {
    const element = elementRef.current
    // Input echoes already match the DOM. Only external changes (undo/redo,
    // restore) replace text, including when this field still has focus.
    if (!element || composing.current || element.innerText === value) return
    element.innerText = value
    if (document.activeElement === element) {
      const range = document.createRange()
      range.selectNodeContents(element)
      range.collapse(false)
      const selection = window.getSelection()
      selection?.removeAllRanges()
      selection?.addRange(range)
    }
  }, [value])

  const commit = (element: HTMLElement) => onChange(element.innerText.replace(/\r/g, ''))
  const input = (event: FormEvent<HTMLElement>) => { if (!composing.current) commit(event.currentTarget) }
  const keyDown = (event: KeyboardEvent<HTMLElement>) => {
    if (composing.current || event.nativeEvent.isComposing || event.keyCode === 229) return
    if (event.key === 'Escape') {
      event.preventDefault()
      onChange(initialValue.current)
      if (elementRef.current) elementRef.current.innerText = initialValue.current
      elementRef.current?.blur()
      return
    }
    if ((!multiline && event.key === 'Enter') || (multiline && event.key === 'Enter' && (event.ctrlKey || event.metaKey))) {
      event.preventDefault()
      elementRef.current?.blur()
    }
  }
  const paste = (event: ClipboardEvent<HTMLElement>) => {
    event.preventDefault()
    const selection = window.getSelection()
    if (!selection?.rangeCount) return
    const range = selection.getRangeAt(0)
    if (!event.currentTarget.contains(range.commonAncestorContainer)) return
    const plain = event.clipboardData.getData('text/plain').replace(/\r\n?/g, '\n')
    const node = document.createTextNode(multiline ? plain : plain.replace(/\n/g, ' '))
    range.deleteContents()
    range.insertNode(node)
    range.setStartAfter(node)
    range.collapse(true)
    selection.removeAllRanges()
    selection.addRange(range)
    commit(event.currentTarget)
  }
  const focus = () => {
    initialValue.current = value
    onEditStart?.()
  }
  const Tag = multiline ? 'div' : 'span'

  return <Tag
    ref={setElementRef}
    className={`cv-inline-text${multiline ? ' is-multiline' : ''}${className ? ` ${className}` : ''}`}
    contentEditable
    suppressContentEditableWarning
    role="textbox"
    aria-label={label}
    aria-multiline={multiline || undefined}
    tabIndex={0}
    data-placeholder={placeholder}
    onFocus={focus}
    onInput={input}
    onPaste={paste}
    onCompositionStart={() => { composing.current = true }}
    onCompositionEnd={(event) => {
      composing.current = false
      commit(event.currentTarget)
      if (document.activeElement !== event.currentTarget) onEditEnd?.()
    }}
    onKeyDown={keyDown}
    onBlur={(event) => {
      // Some IMEs dispatch blur before their final composition event. Keep the
      // DOM protected from server echoes until that final text is available.
      if (composing.current) return
      commit(event.currentTarget)
      onEditEnd?.()
    }}
  />
}
