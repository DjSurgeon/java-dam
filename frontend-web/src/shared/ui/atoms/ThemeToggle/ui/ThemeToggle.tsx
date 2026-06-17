/**
 * @file ThemeToggle.tsx
 * @description Theme toggle button component
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Sun, Moon } from 'lucide-react';
import s from './ThemeToggle.module.scss';

interface ThemeToggleProps {
  theme: 'light' | 'dark';
  onToggle: () => void;
}

export const ThemeToggle: React.FC<ThemeToggleProps> = ({ theme, onToggle }) => {
  return (
    <button 
      onClick={onToggle} 
      className={s.toggle}
      aria-label="Toggle theme"
    >
      <div className={`${s.iconWrapper} ${theme === 'light' ? s.iconExitUp : s.iconEnter}`}>
        <Moon size={18} strokeWidth={1.5} />
      </div>
      <div className={`${s.iconWrapper} ${theme === 'dark' ? s.iconExitDown : s.iconEnter}`}>
        <Sun size={18} strokeWidth={1.5} />
      </div>
    </button>
  );
};
