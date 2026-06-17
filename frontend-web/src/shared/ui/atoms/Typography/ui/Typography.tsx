/**
 * @file Typography.tsx
 * @description Polymorphic Typography component
 * @date 2024-05-07
 * @author Sergio
 */

import React, { ElementType, ComponentPropsWithoutRef, ComponentPropsWithRef, forwardRef } from 'react';
import s from './Typography.module.scss';

type Variant = 'h1' | 'h2' | 'h3' | 'body' | 'technical';
type Color = 'primary' | 'on-background' | 'on-surface' | 'on-surface-variant';

export type TypographyProps<C extends ElementType> = {
  as?: C;
  variant?: Variant;
  color?: Color;
  className?: string;
  italic?: boolean;
  children: React.ReactNode;
} & Omit<ComponentPropsWithoutRef<C>, 'as' | 'variant' | 'color' | 'className' | 'italic'>;

type PolymorphicRef<C extends ElementType> = ComponentPropsWithRef<C>['ref'];

type TypographyComponent = <C extends ElementType = 'p'>(
  props: TypographyProps<C> & { ref?: PolymorphicRef<C> }
) => React.ReactElement | null;

export const Typography: TypographyComponent = forwardRef(
  <C extends ElementType = 'p'>(
    {
      as,
      variant = 'body',
      color = 'on-background',
      className = '',
      italic = false,
      children,
      ...props
    }: TypographyProps<C>,
    ref?: PolymorphicRef<C>
  ) => {
    const Component = (as || (variant.startsWith('h') ? variant : 'p')) as ElementType;

    const classes = [
      s.typography,
      s[`typography--variant-${variant}`],
      s[`typography--color-${color}`],
      italic && s['typography--italic'],
      className,
    ].filter(Boolean).join(' ');

    return (
      <Component ref={ref} className={classes} {...props}>
        {children}
      </Component>
    );
  }
);
