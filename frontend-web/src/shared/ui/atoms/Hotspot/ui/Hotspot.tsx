/**
 * @file Hotspot.tsx
 * @description Interactive point atom with pulse animation (Position agnostic)
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion } from 'motion/react';
import s from './Hotspot.module.scss';

interface HotspotProps {
  isActive: boolean;
  onClick: () => void;
  ariaLabel: string;
  className?: string;
  style?: React.CSSProperties;
}

export const Hotspot: React.FC<HotspotProps> = ({ 
  isActive, 
  onClick, 
  ariaLabel, 
  className = '',
  style 
}) => {
  return (
    <motion.button
      className={`${s.hotspot} ${isActive ? s['hotspot--active'] : ''} ${className}`}
      style={style}
      onClick={onClick}
      aria-label={ariaLabel}
      whileHover={{ scale: 1.2 }}
      whileTap={{ scale: 0.9 }}
    >
      <span className={s.inner} />
      <span className={s.pulse} />
    </motion.button>
  );
};
