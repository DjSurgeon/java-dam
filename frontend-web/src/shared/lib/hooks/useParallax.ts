/**
 * @file useParallax.ts
 * @description Hook for standard parallax scroll animations
 * @date 2024-05-07
 * @author Sergio
 */

import { useScroll, useTransform, MotionValue } from 'motion/react';

export const useParallax = () => {
  const { scrollYProgress } = useScroll();
  
  // Hero section transforms
  const scale = useTransform(scrollYProgress, [0, 0.4], [1, 1.15]);
  const opacity = useTransform(scrollYProgress, [0, 0.4], [0.5, 0.1]);
  const fadeOut = useTransform(scrollYProgress, [0, 0.05], [1, 0]);

  return {
    scrollYProgress,
    scale,
    opacity,
    fadeOut
  };
};

export const useCustomParallax = (
  value: MotionValue<number>,
  distance: number
) => {
  return useTransform(value, [0, 1], [-distance, distance]);
};
