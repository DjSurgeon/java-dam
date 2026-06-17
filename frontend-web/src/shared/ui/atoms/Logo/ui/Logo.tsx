/**
 * @file Logo.tsx
 * @description Responsive Logo atom
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { Utensils } from 'lucide-react';
import { Typography } from '@/shared/ui/atoms/Typography';
import s from './Logo.module.scss';

interface LogoProps {
  onClick?: () => void;
  className?: string;
}

export const Logo: React.FC<LogoProps> = ({ onClick, className = '' }) => {
  return (
    <Link to="/" className={`${s.logo} ${className}`} onClick={onClick}>
      <div className={s.icon}>
        <Utensils size={32} strokeWidth={1} />
      </div>
      <Typography variant="h3" as="span" className={s.text}>
        <strong>Jamón</strong> al Plato
      </Typography>
    </Link>
  );
};
