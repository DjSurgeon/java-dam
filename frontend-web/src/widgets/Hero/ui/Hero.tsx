/**
 * @file Hero.tsx
 * @description Hero widget with interactive philosophy hotspots
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion } from 'motion/react';
import { Typography } from '@/shared/ui/atoms/Typography';
import { LandingContent } from '@/entities/content/model/schema';
import { HeroBackground } from '@/shared/ui/molecules/HeroBackground';
import { PhilosophyMap } from './components/PhilosophyMap';
import s from './Hero.module.scss';

interface HeroProps {
  content: LandingContent['hero'];
}

export const Hero: React.FC<HeroProps> = ({ content }) => {
  return (
    <section className={s.hero}>
      <HeroBackground imageUrl="/images/hero-bg.webp" />
      
      {/* 💡 INTERACTIVE: Hotspots Layer */}
      <PhilosophyMap />

      <div className={s.content}>
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1.5, ease: [0.19, 1, 0.22, 1] }}
          className={s.titleWrapper}
        >
          <Typography variant="h1" color="on-background" className={s.title}>
            {content.title} <br /> 
            <span className={s.highlight}>
              {content.highlight}
            </span>
          </Typography>
        </motion.div>
      </div>
    </section>
  );
};
