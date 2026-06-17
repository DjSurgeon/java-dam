/**
 * @file SectionBadge.tsx
 * @description Molecule displaying a section badge with a decorative line
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Typography } from '@/shared/ui/atoms/Typography';
import s from './SectionBadge.module.scss';

interface SectionBadgeProps {
  text: string;
  reverse?: boolean;
}

export const SectionBadge: React.FC<SectionBadgeProps> = ({ text, reverse = false }) => {
  return (
    <div className={`${s.badge} ${reverse ? s['badge--reverse'] : ''}`}>
      <div className={`${s.badgeLine} ${reverse ? s['badgeLine--reverse'] : ''}`}></div>
      <Typography variant="technical" color="primary">{text}</Typography>
    </div>
  );
};
