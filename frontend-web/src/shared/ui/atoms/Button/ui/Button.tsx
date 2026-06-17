/**
 * @file Button.tsx
 * @description Flexible Button component
 * @date 2024-05-07
 * @author Sergio
 */

import React, { ElementType, ComponentPropsWithoutRef, ComponentPropsWithRef, forwardRef } from 'react';
import s from './Button.module.scss';

type Variant = 'primary' | 'outline' | 'text';
type Size = 'small' | 'medium';

export type ButtonProps<C extends ElementType> = {
  as?: C;
  variant?: Variant;
  size?: Size;
  className?: string;
  children: React.ReactNode;
} & Omit<ComponentPropsWithoutRef<C>, 'as' | 'variant' | 'size' | 'className'>;

type PolymorphicRef<C extends ElementType> = ComponentPropsWithRef<C>['ref'];

type ButtonComponent = <C extends ElementType = 'button'>(
  props: ButtonProps<C> & { ref?: PolymorphicRef<C> }
) => React.ReactElement | null;

export const Button: ButtonComponent = forwardRef(
  <C extends ElementType = 'button'>(
    { as, variant = 'primary', size = 'medium', className = '', children, ...props }: ButtonProps<C>,
    ref?: PolymorphicRef<C>
  ) => {
    const Component = (as || 'button') as ElementType;
    const classes = [
      s.button,
      s[`button--variant-${variant}`],
      s[`button--size-${size}`],
      className,
    ].filter(Boolean).join(' ');

    return (
      <Component ref={ref} className={classes} {...props}>
        {children}
      </Component>
    );
  }
);
