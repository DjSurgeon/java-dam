/**
 * @file Navbar.tsx
 * @description Clean Navbar widget component
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Menu, X } from 'lucide-react';
import { Button } from '@/shared/ui/atoms/Button';
import { ThemeToggle } from '@/shared/ui/atoms/ThemeToggle';
import { Logo } from '@/shared/ui/atoms/Logo';
import { NavLink } from '@/shared/ui/atoms/NavLink';
import { useTheme } from '@/app/providers';
import { useNavbar } from '../model/useNavbar';
import { navItems } from '../model/links';
import { MobileMenu } from './components/MobileMenu';
import s from './Navbar.module.scss';

export const Navbar: React.FC = () => {
  const { isScrolled, isMobileMenuOpen, toggleMobileMenu, closeMobileMenu } = useNavbar();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className={`${s.header} ${isScrolled ? s['header--scrolled'] : ''}`}>
      <div className={s.container}>
        <Logo onClick={closeMobileMenu} />

        <nav className={s.desktopNav}>
          {navItems.map((item) => (
            <NavLink key={item.label} href={item.href}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className={s.actions}>
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
          
          <Button as="a" href="#contacto" variant="primary" className="hidden md:inline-flex">
            Reservar
          </Button>

          <button 
            className={s.mobileToggle}
            onClick={toggleMobileMenu}
            aria-label={isMobileMenuOpen ? "Cerrar menú" : "Abrir menú"}
          >
            {isMobileMenuOpen ? <X size={28} strokeWidth={1.5} /> : <Menu size={28} strokeWidth={1.5} />}
          </button>
        </div>
      </div>

      <MobileMenu 
        isOpen={isMobileMenuOpen} 
        items={navItems} 
        onClose={closeMobileMenu} 
      />
    </header>
  );
};
