/**
 * @file Navbar.test.tsx
 * @description Unit tests for Navbar widget
 * @date 2024-05-07
 * @author Sergio
 */

import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { Navbar } from '../Navbar';
import * as providers from '@/app/providers';

// Mock useTheme hook
vi.mock('@/app/providers', () => ({
  useTheme: vi.fn(),
}));

describe('Navbar Widget', () => {
  const mockToggleTheme = vi.fn();
  
  beforeEach(() => {
    vi.clearAllMocks();
    (providers.useTheme as any).mockReturnValue({
      theme: 'dark',
      toggleTheme: mockToggleTheme,
    });
  });

  const renderNavbar = () => {
    return render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );
  };

  it('renders the logo correctly', () => {
    renderNavbar();
    expect(screen.getByText(/Jamón/i)).toBeDefined();
    expect(screen.getByText(/al Plato/i)).toBeDefined();
  });

  it('renders all navigation links', () => {
    renderNavbar();
    expect(screen.getByText('Blog')).toBeDefined();
    expect(screen.getByText('Contacto')).toBeDefined();
  });

  it('toggles the mobile menu when the menu button is clicked', () => {
    renderNavbar();
    const toggleButton = screen.getByLabelText(/Abrir menú/i);
    
    fireEvent.click(toggleButton);
    
    // Check if close icon is visible (implies menu is open)
    expect(screen.getByLabelText(/Cerrar menú/i)).toBeDefined();
  });

  it('calls toggleTheme when ThemeToggle is clicked', () => {
    renderNavbar();
    const themeButton = screen.getByLabelText(/Toggle theme/i);
    
    fireEvent.click(themeButton);
    expect(mockToggleTheme).toHaveBeenCalledTimes(1);
  });
});
