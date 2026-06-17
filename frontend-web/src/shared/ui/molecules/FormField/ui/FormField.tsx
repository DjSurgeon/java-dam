/**
 * @file FormField.tsx
 * @description Molecule component for wrapping inputs with labels and errors
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import s from './FormField.module.scss';

interface FormFieldProps {
  label: string;
  htmlFor?: string;
  error?: string;
  children: React.ReactNode;
  className?: string;
}

export const FormField: React.FC<FormFieldProps> = ({ 
  label, 
  htmlFor,
  error, 
  children,
  className = ''
}) => {
  return (
    <div className={`${s.inputGroup} ${className}`}>
      <label htmlFor={htmlFor} className={s.label}>{label}</label>
      {children}
      {error && <p className={s.errorText}>{error}</p>}
    </div>
  );
};
