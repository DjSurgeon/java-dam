/**
 * @file links.ts
 * @description Navigation links configuration for the Navbar
 * @date 2024-05-07
 * @author Sergio
 */

export interface NavItem {
  label: string;
  href: string;
}

export const navItems: NavItem[] = [
  { label: 'Blog', href: '#blog' },
  { label: 'Contacto', href: '#contacto' },
];
