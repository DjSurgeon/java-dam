import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { useContactForm } from '../model/useContactForm';

describe('useContactForm Logic', () => {
  it('initializes with empty state and no errors', () => {
    const { result } = renderHook(() => useContactForm());
    
    expect(result.current.formState).toEqual({ name: '', email: '', message: '' });
    expect(result.current.errors).toEqual({});
    expect(result.current.isSubmitting).toBe(false);
    expect(result.current.submitSuccess).toBe(false);
  });

  it('updates state on handleChange and clears field error', () => {
    const { result } = renderHook(() => useContactForm());

    // Force an error state first
    act(() => {
      result.current.handleSubmit();
    });
    expect(result.current.errors.name).toBe('El nombre es obligatorio');

    // Handle change
    act(() => {
      result.current.handleChange('name', 'Conde de Romanones');
    });

    expect(result.current.formState.name).toBe('Conde de Romanones');
    expect(result.current.errors.name).toBe(''); // Error should be cleared
  });

  it('validates email correctly', () => {
    const { result } = renderHook(() => useContactForm());

    act(() => {
      result.current.handleChange('name', 'User');
      result.current.handleChange('email', 'invalid-email');
      result.current.handleChange('message', 'Un mensaje muy largo y bonito para pasar la validacion.');
    });

    act(() => {
      result.current.handleSubmit();
    });

    expect(result.current.errors.email).toBe('El formato del email no es válido');
  });

  it('submits successfully when form is valid', async () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useContactForm());

    act(() => {
      result.current.handleChange('name', 'Sergio');
      result.current.handleChange('email', 'sergio@example.com');
      result.current.handleChange('message', 'Quiero un jamón espectacular para mi boda.');
    });

    // We must await the handleSubmit inside act because it returns a Promise
    let promise: Promise<void>;
    act(() => {
      promise = result.current.handleSubmit() as Promise<void>;
    });

    // Should be submitting
    expect(result.current.isSubmitting).toBe(true);

    // Fast-forward timeout
    await act(async () => {
      vi.runAllTimers();
      await promise;
    });

    expect(result.current.isSubmitting).toBe(false);
    expect(result.current.submitSuccess).toBe(true);
    expect(result.current.formState.name).toBe(''); // Should have been reset
    
    vi.useRealTimers();
  });
});
