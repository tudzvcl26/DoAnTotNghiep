import { type KeyboardEvent, type FocusEvent, type FormEvent, useEffect, useRef } from 'react'

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

  useEffect(() => {
    const element = elementRef.current
    if (!element || document.activeElement === element || element.innerText === value) return
    element.innerText = value
  }, [value])

  const input = (event: FormEvent<HTMLElement>) => onChange(event.currentTarget.innerText.replace(/\r/g, ''))
  const keyDown = (event: KeyboardEvent<HTMLElement>) => {
    if (event.key === 'Escape') {
      event.preventDefault()
      onChange(initialValue.current)
      elementRef.current?.blur()
      return
    }
    if ((!multiline && event.key === 'Enter') || (multiline && event.key === 'Enter' && (event.ctrlKey || event.metaKey))) {
      event.preventDefault()
      elementRef.current?.blur()
    }
  }
  const focus = () => {
    initialValue.current = value
    onEditStart?.()
  }
  const blur = (_event: FocusEvent<HTMLElement>) => onEditEnd?.()
  const Tag = multiline ? 'div' : 'span'

  return <Tag
    ref={(node) => { elementRef.current = node }}
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
    onKeyDown={keyDown}
    onBlur={blur}
  >{value}</Tag>
}
