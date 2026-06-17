/**
 * @file Textarea.tsx
 * @description Atom component for multiline text inputs
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import s from './Textarea.module.scss';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  isError?: boolean;
}

export const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ isError, className = '', ...props }, ref) => {
    return (
      <textarea
        ref={ref}
        className={`${s.textarea} ${isError ? s['textarea--error'] : ''} ${className}`}
        {...props}
      />
    );
  }
);

Textarea.displayName = 'Textarea';
