/**
 * @file ScrollIndicator.tsx
 * @description Animated scroll indicator atom
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion, MotionValue } from 'motion/react';
import { Typography } from '@/shared/ui/atoms/Typography';
import s from './ScrollIndicator.module.scss';

interface ScrollIndicatorProps {
  text: string;
  opacity?: MotionValue<number> | number;
}

export const ScrollIndicator: React.FC<ScrollIndicatorProps> = ({ text, opacity = 1 }) => {
  return (
    <div className={s.wrapper}>
      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 0.6 }}
        transition={{ delay: 1, duration: 2 }}
      >
        <Typography variant="technical" color="on-surface-variant">
          {text}
        </Typography>
      </motion.div>
      <motion.div 
        style={{ opacity }}
        className={s.line}
      >
        <motion.div 
          animate={{ y: ["-100%", "100%"] }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className={s.dot}
        />
      </motion.div>
    </div>
  );
};
