import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import React from 'react';
import { ThemeToggle } from '../ui/ThemeToggle';

describe('ThemeToggle Atom', () => {
  it('renders correctly and calls onToggle when clicked', () => {
    const handleToggle = vi.fn();
    render(<ThemeToggle theme="dark" onToggle={handleToggle} />);
    
    const button = screen.getByRole('button', { name: /toggle theme/i });
    expect(button).toBeInTheDocument();
    
    fireEvent.click(button);
    expect(handleToggle).toHaveBeenCalledTimes(1);
  });
});
