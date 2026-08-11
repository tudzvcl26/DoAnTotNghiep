import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link, type LinkProps } from 'react-router-dom'
import './button.css'

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'dark'
type ButtonSize = 'sm' | 'md' | 'lg'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode
  variant?: ButtonVariant
  size?: ButtonSize
  fullWidth?: boolean
}

type ButtonLinkProps = LinkProps & {
  children: ReactNode
  variant?: ButtonVariant
  size?: ButtonSize
  fullWidth?: boolean
}

const classNames = (variant: ButtonVariant, size: ButtonSize, fullWidth?: boolean) =>
  `button button--${variant} button--${size}${fullWidth ? ' button--full' : ''}`

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  fullWidth,
  className = '',
  ...props
}: ButtonProps) {
  return (
    <button className={`${classNames(variant, size, fullWidth)} ${className}`.trim()} {...props}>
      {children}
    </button>
  )
}

export function ButtonLink({
  children,
  variant = 'primary',
  size = 'md',
  fullWidth,
  className = '',
  ...props
}: ButtonLinkProps) {
  return (
    <Link className={`${classNames(variant, size, fullWidth)} ${className}`.trim()} {...props}>
      {children}
    </Link>
  )
}
