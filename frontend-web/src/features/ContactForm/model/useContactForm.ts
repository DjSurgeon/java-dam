/**
 * @file useContactForm.ts
 * @description Hook to manage the contact form state and logic
 * @date 2026-05-13
 * @lastUpdate 2026-05-13
 * @author Sergio
 */

import { useState } from 'react';
import type { FormEvent } from 'react';

export interface ContactFormState {
  name: string;
  email: string;
  message: string;
}

export const useContactForm = () => {
  const [formState, setFormState] = useState<ContactFormState>({
    name: '',
    email: '',
    message: ''
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const handleChange = (field: keyof ContactFormState, value: string) => {
    setFormState((prev) => ({ ...prev, [field]: value }));
    // Clear error for the field being edited
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors: { [key: string]: string } = {};
    if (!formState.name.trim()) newErrors.name = 'El nombre es obligatorio';
    if (!formState.email.trim()) {
      newErrors.email = 'El email es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formState.email)) {
      newErrors.email = 'El formato del email no es válido';
    }
    if (!formState.message.trim()) newErrors.message = 'Por favor, cuéntanos algo sobre tu evento';
    if (formState.message.length > 0 && formState.message.length < 10) {
      newErrors.message = 'El mensaje debe tener al menos 10 caracteres';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e?: FormEvent) => {
    if (e) {
      e.preventDefault();
    }
    
    if (!validateForm()) return;

    setIsSubmitting(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500));
    setIsSubmitting(false);
    setSubmitSuccess(true);
    setFormState({ name: '', email: '', message: '' });
    
    setTimeout(() => setSubmitSuccess(false), 5000);
  };

  return {
    formState,
    errors,
    isSubmitting,
    submitSuccess,
    handleChange,
    handleSubmit
  };
};
