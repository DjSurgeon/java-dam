/**
 * @file NavLink.tsx
 * @description Navigation link atom with premium hover effect
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import s from './NavLink.module.scss';

interface NavLinkProps {
  href: string;
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
  isMobile?: boolean;
}

export const NavLink: React.FC<NavLinkProps> = ({ 
  href, 
  children, 
  onClick, 
  className = '',
  isMobile = false
}) => {
  const classes = [
    s.navLink,
    isMobile && s['navLink--mobile'],
    className
  ].filter(Boolean).join(' ');

  return (
    <a href={href} className={classes} onClick={onClick}>
      {children}
    </a>
  );
};
