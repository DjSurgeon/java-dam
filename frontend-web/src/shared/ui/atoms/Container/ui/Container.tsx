/**
 * @file Container.tsx
 * @description Layout container component
 * @date 2024-05-07
 * @author Sergio
 */

import React, { ElementType, ComponentPropsWithoutRef, ComponentPropsWithRef, forwardRef } from 'react';
import s from './Container.module.scss';

export type ContainerProps<C extends ElementType> = {
  as?: C;
  fluid?: boolean;
  className?: string;
  children: React.ReactNode;
} & Omit<ComponentPropsWithoutRef<C>, 'as' | 'fluid' | 'className'>;

type PolymorphicRef<C extends ElementType> = ComponentPropsWithRef<C>['ref'];

type ContainerComponent = <C extends ElementType = 'div'>(
  props: ContainerProps<C> & { ref?: PolymorphicRef<C> }
) => React.ReactElement | null;

export const Container: ContainerComponent = forwardRef(
  <C extends ElementType = 'div'>(
    { as, fluid = false, className = '', children, ...props }: ContainerProps<C>,
    ref?: PolymorphicRef<C>
  ) => {
    const Component = (as || 'div') as ElementType;
    const classes = [
      s.container,
      fluid && s['container--fluid'],
      className,
    ].filter(Boolean).join(' ');

    return (
      <Component ref={ref} className={classes} {...props}>
        {children}
      </Component>
    );
  }
);
