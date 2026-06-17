/**
 * @file FeatureCard.tsx
 * @description Molecule displaying a feature card
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Typography } from '@/shared/ui/atoms/Typography';
import s from './FeatureCard.module.scss';

interface FeatureCardProps {
  label: string;
  title: string;
  text: string;
}

export const FeatureCard: React.FC<FeatureCardProps> = ({ label, title, text }) => {
  return (
    <div className={s.featureCard}>
      <Typography variant="technical" color="primary">{label}</Typography>
      <Typography variant="h3" color="on-background">{title}</Typography>
      <Typography variant="body" color="on-surface-variant">{text}</Typography>
    </div>
  );
};
