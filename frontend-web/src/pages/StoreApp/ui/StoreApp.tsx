/**
 * @file StoreApp.tsx
 * @description Placeholder for the Store application
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { Typography } from '@/shared/ui/atoms/Typography';
import { Container } from '@/shared/ui/atoms/Container';
import s from './StoreApp.module.scss';

export const StoreApp: React.FC = () => {
  return (
    <div className={s.page}>
      <Container>
        <div className={s.content}>
          <Typography variant="technical" color="primary" className={s.badge}>
            PRÓXIMAMENTE
          </Typography>
          <Typography variant="h2" color="on-background">
            Experiencia de <span className={s.highlight}>tienda.</span>
          </Typography>
        </div>
      </Container>
    </div>
  );
};
