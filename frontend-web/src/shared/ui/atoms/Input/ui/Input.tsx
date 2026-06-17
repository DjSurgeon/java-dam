/**
 * @file Input.tsx
 * @description Atom component for text inputs
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import s from './Input.module.scss';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  isError?: boolean;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ isError, className = '', ...props }, ref) => {
    return (
      <input
        ref={ref}
        className={`${s.input} ${isError ? s['input--error'] : ''} ${className}`}
        {...props}
      />
    );
  }
);

Input.displayName = 'Input';
