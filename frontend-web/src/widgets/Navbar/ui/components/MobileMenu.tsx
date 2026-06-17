/**
 * @file MobileMenu.tsx
 * @description Mobile menu overlay for Navbar
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { NavLink } from '@/shared/ui/atoms/NavLink';
import { Button } from '@/shared/ui/atoms/Button';
import { NavItem } from '../../model/links';
import s from '../Navbar.module.scss';

interface MobileMenuProps {
  isOpen: boolean;
  items: NavItem[];
  onClose: () => void;
}

export const MobileMenu: React.FC<MobileMenuProps> = ({ isOpen, items, onClose }) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -20 }}
          transition={{ duration: 0.3 }}
          className={s.mobileMenu}
        >
          <nav className={s.mobileNav}>
            {items.map((item, index) => (
              <motion.div
                key={item.label}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 * (index + 1) }}
              >
                <NavLink 
                  href={item.href} 
                  isMobile 
                  onClick={onClose}
                >
                  {item.label}
                </NavLink>
              </motion.div>
            ))}
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.4 }}
              style={{ marginTop: '2rem' }}
            >
              <Button as="a" href="#contacto" variant="primary" onClick={onClose}>
                Reservar Ahora
              </Button>
            </motion.div>
          </nav>
        </motion.div>
      )}
    </AnimatePresence>
  );
};
