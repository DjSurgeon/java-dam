import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import React from 'react';
import { LandingPage } from '../ui/LandingPage';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@/app/providers/ThemeProvider';

// We mock the content loader to avoid dealing with file systems in the test
vi.mock('@/shared/lib/contentLoader', () => ({
  loadLandingContent: () => ({
    hero: {
      badge: "TEST BADGE",
      title: "Test Title",
      highlight: "Highlight",
      scrollText: "Scroll"
    },
    blog: []
  })
}));

vi.mock('motion/react', () => {
  const React = require('react');
  const DummyComponent = React.forwardRef((props: any, ref: any) => {
    const { 
      initial, animate, exit, transition, variants, whileHover, 
      whileTap, whileInView, viewport, style, ...validProps 
    } = props;
    
    // We clean up custom style objects from motion that might not be valid React styles
    const cleanStyle = style ? { ...style } : undefined;
    if (cleanStyle && typeof cleanStyle.y === 'object') delete cleanStyle.y;
    if (cleanStyle && typeof cleanStyle.opacity === 'object') delete cleanStyle.opacity;
    if (cleanStyle && typeof cleanStyle.scale === 'object') delete cleanStyle.scale;

    return React.createElement('div', { ref, ...validProps, style: cleanStyle }, props.children);
  });
  
  return {
    motion: {
      div: DummyComponent,
      span: DummyComponent,
      p: DummyComponent,
      form: DummyComponent,
      button: DummyComponent
    },
    AnimatePresence: ({ children }: any) => children
  };
});

vi.mock('@/shared/lib/hooks/useScrollReveal', () => ({
  useScrollReveal: () => ({
    fadeIn: {},
    slideUp: {},
    slideIn: {},
    scaleUp: {}
  })
}));

vi.mock('@/shared/lib/hooks/useParallax', () => ({
  useParallax: () => ({
    y: 0,
    opacity: 1,
    scale: 1,
    rotate: 0,
    fadeOut: 0
  })
}));

// We mock matchMedia which might be used by framer-motion or ThemeToggle
vi.stubGlobal('matchMedia', vi.fn(() => ({
  matches: false,
  addListener: vi.fn(),
  removeListener: vi.fn(),
})));

describe('LandingPage Integration', () => {
  it('renders all main sections correctly', async () => {
    render(
      <ThemeProvider>
        <MemoryRouter>
          <LandingPage />
        </MemoryRouter>
      </ThemeProvider>
    );

    // Check if Navbar is rendered (Logo uses <strong>Jamón</strong> al Plato)
    expect(screen.getByText(/Jamón/i)).toBeInTheDocument();

    // Check if Hero is rendered (synchronous)
    expect(screen.getAllByText(/Test Title/i).length).toBeGreaterThan(0);
    
    // Check if ContactForm is rendered
    expect(await screen.findByText(/CONSULTAS \& RESERVAS/i)).toBeInTheDocument();
  });
});
