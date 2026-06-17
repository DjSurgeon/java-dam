/**
 * @file Footer.tsx
 * @description Footer widget component
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Utensils, Instagram, Linkedin, Twitter } from 'lucide-react';
import { Typography } from '@/shared/ui/atoms/Typography';
import s from './Footer.module.scss';

export const Footer: React.FC = () => {
  return (
    <footer className={s.footer}>
      <div className={s.container}>
        <div className={s.grid}>
          <div className={s.brand}>
            <div className={s.logo}>
              <Utensils size={32} strokeWidth={1} color="var(--primary)" />
              <Typography variant="h3" color="on-background" as="span" className="tracking-tighter font-light">
                Jamón al Plato
              </Typography>
            </div>
            <Typography variant="body" color="on-surface-variant" className="font-light max-w-sm">
              El estándar definitivo en la gestión y reserva de maestros cortadores de jamón ibérico para eventos de alta categoría.
            </Typography>
          </div>
          
          <div>
            <Typography variant="technical" color="on-background" style={{ marginBottom: '2rem', display: 'block' }}>
              Compañía
            </Typography>
            <ul className={s.list}>
              <li><Typography variant="body" color="on-surface-variant" as="a" href="#">Manifiesto</Typography></li>
              <li><Typography variant="body" color="on-surface-variant" as="a" href="#">Maestros</Typography></li>
              <li><Typography variant="body" color="on-surface-variant" as="a" href="#">Prensa</Typography></li>
              <li><Typography variant="body" color="on-surface-variant" as="a" href="#">Contacto</Typography></li>
            </ul>
          </div>

          <div>
            <Typography variant="technical" color="on-background" style={{ marginBottom: '2rem', display: 'block' }}>
              Social
            </Typography>
            <div className={s.social}>
              <a href="#" className={s.socialIcon}><Instagram size={16} /></a>
              <a href="#" className={s.socialIcon}><Linkedin size={16} /></a>
              <a href="#" className={s.socialIcon}><Twitter size={16} /></a>
            </div>
          </div>
        </div>

        <div className={s.divider} />

        <div className={s.bottom}>
          <Typography variant="technical" color="on-surface-variant">
            © 2026 FORJADO CON ESFUERZO Y PRECISIÓN POR SERGIO JIMÉNEZ.
          </Typography>
          <div className={s.legalLinks}>
            <Typography variant="technical" color="on-surface-variant" as="a" href="#">Privacidad</Typography>
            <Typography variant="technical" color="on-surface-variant" as="a" href="#">Legal</Typography>
          </div>
        </div>
      </div>
    </footer>
  );
};
