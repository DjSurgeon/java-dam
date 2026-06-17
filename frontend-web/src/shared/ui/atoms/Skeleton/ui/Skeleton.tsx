/**
 * @file Skeleton.tsx
 * @description Height-matched Skeleton for lazy loaded sections to avoid CLS
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import s from './Skeleton.module.scss';

interface SkeletonProps {
  height?: string;
  width?: string;
  borderRadius?: string;
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ 
  height = '100%', 
  width = '100%', 
  borderRadius = '0',
  className = ''
}) => {
  return (
    <div 
      className={`${s.skeleton} ${className}`} 
      style={{ height, width, borderRadius }}
      aria-hidden="true"
    />
  );
};
