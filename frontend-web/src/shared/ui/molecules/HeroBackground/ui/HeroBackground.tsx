/**
 * @file HeroBackground.tsx
 * @description Immersive background molecule for Hero sections
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion, MotionValue } from 'motion/react';
import s from './HeroBackground.module.scss';

interface HeroBackgroundProps {
  imageUrl: string;
  scale?: MotionValue<number> | number;
  opacity?: MotionValue<number> | number;
  overlayOpacity?: number;
}

export const HeroBackground: React.FC<HeroBackgroundProps> = ({ 
  imageUrl, 
  scale = 1, 
  opacity = 1,
  overlayOpacity = 0.4
}) => {
  return (
    <div className={s.background}>
      <motion.div 
        style={{ 
          scale,
          opacity,
          backgroundImage: `url('${imageUrl}')`
        }}
        className={s.image}
      />
      <div className={s.vignette} style={{ opacity: overlayOpacity }} />
      <div className={s.radial} />
    </div>
  );
};
