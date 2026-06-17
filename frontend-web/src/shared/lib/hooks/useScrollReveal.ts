/**
 * @file useScrollReveal.ts
 * @description Hook for standard scroll reveal animations using framer-motion
 * @date 2024-05-07
 * @author Sergio
 */

export const useScrollReveal = () => {
  const fadeIn = {
    initial: { opacity: 0, y: 30 },
    whileInView: { opacity: 1, y: 0 },
    viewport: { once: true },
    transition: { duration: 1.2, ease: [0.19, 1, 0.22, 1] }
  };

  const staggerContainer = {
    initial: {},
    whileInView: {
      transition: {
        staggerChildren: 0.15
      }
    },
    viewport: { once: true }
  };

  const fadeItem = {
    initial: { opacity: 0, y: 30 },
    whileInView: { opacity: 1, y: 0 },
    transition: { duration: 1.2, ease: [0.19, 1, 0.22, 1] }
  };

  return { fadeIn, staggerContainer, fadeItem };
};
