/**
 * @file ContactForm.tsx
 * @description Contact Form feature component
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion } from 'motion/react';
import { BadgeCheck, ArrowRight } from 'lucide-react';
import { Typography } from '@/shared/ui/atoms/Typography';
import { Input } from '@/shared/ui/atoms/Input';
import { Textarea } from '@/shared/ui/atoms/Textarea';
import { FormField } from '@/shared/ui/molecules/FormField';
import { useScrollReveal } from '@/shared/lib/hooks/useScrollReveal';
import { SectionBadge } from '@/shared/ui/molecules/SectionBadge';
import { useContactForm } from '../model/useContactForm';
import s from './ContactForm.module.scss';

export const ContactForm: React.FC = () => {
  const { fadeIn } = useScrollReveal();
  const {
    formState,
    errors,
    isSubmitting,
    submitSuccess,
    handleChange,
    handleSubmit
  } = useContactForm();

  return (
    <section id="contacto" className={s.section}>
      <div className={s.container}>
        <motion.div {...fadeIn} className={s.header}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '2rem' }}>
            <SectionBadge text="CONSULTAS & RESERVAS" />
          </div>
          <Typography variant="h2" color="on-background" className="tracking-tight">
            Eleve su próximo <span style={{ fontStyle: 'italic', fontWeight: 300, color: 'var(--primary)' }}>evento.</span>
          </Typography>
          <Typography variant="body" color="on-surface-variant" className={s.description}>
            Consulte disponibilidad y reciba una propuesta personalizada para su evento.
          </Typography>
        </motion.div>

        <motion.form 
          {...fadeIn}
          transition={{ delay: 0.3, duration: 1.5 }}
          onSubmit={handleSubmit}
          className={s.form}
        >
          {submitSuccess && (
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className={s.successOverlay}
            >
               <BadgeCheck size={64} className="text-primary mb-6" strokeWidth={1} color="var(--primary)" />
               <Typography variant="h3" color="on-background" style={{ marginBottom: '1rem' }}>
                 Solicitud Recibida
               </Typography>
               <Typography variant="body" color="on-surface-variant" className="font-light" style={{ maxWidth: '28rem' }}>
                 Un Concierge maestro se pondrá en contacto con usted en las próximas 24 horas para perfilar los detalles.
               </Typography>
            </motion.div>
          )}

          <div className={s.grid}>
            <FormField label="Nombre Completo" htmlFor="contact-name" error={errors.name}>
              <Input 
                id="contact-name"
                type="text" 
                value={formState.name}
                onChange={e => handleChange('name', e.target.value)}
                isError={!!errors.name}
                placeholder="Ej. Conde de Romanones"
              />
            </FormField>
            
            <FormField label="Correo Electrónico" htmlFor="contact-email" error={errors.email}>
              <Input 
                id="contact-email"
                type="email" 
                value={formState.email}
                onChange={e => handleChange('email', e.target.value)}
                isError={!!errors.email}
                placeholder="contacto@empresa.com"
              />
            </FormField>
          </div>

          <FormField 
            label="Detalles del Evento" 
            htmlFor="contact-message"
            error={errors.message}
          >
            <Textarea 
              id="contact-message"
              value={formState.message}
              onChange={e => handleChange('message', e.target.value)}
              isError={!!errors.message}
              placeholder="Fecha, ubicación, número estimado de invitados y tipo de evento."
            />
          </FormField>

          <div className={s.submitWrapper}>
            <button 
              disabled={isSubmitting}
              className={s.submitBtn}
            >
              <span className={s.submitContent}>
                {isSubmitting ? 'Procesando...' : 'ENVIAR SOLICITUD'}
                {!isSubmitting && <ArrowRight size={14} className="group-hover:translate-x-2 transition-transform duration-500" />}
              </span>
              <div className={s.submitBg} />
            </button>
          </div>
        </motion.form>
      </div>
    </section>
  );
};
