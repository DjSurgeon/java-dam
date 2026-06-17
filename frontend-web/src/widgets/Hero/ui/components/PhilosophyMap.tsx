/**
 * @file PhilosophyMap.tsx
 * @description Refined Interactive map for Hero hotspots
 * @date 2024-05-07
 * @author Sergio
 */

import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Hotspot } from '@/shared/ui/atoms/Hotspot';
import { Typography } from '@/shared/ui/atoms/Typography';
import { hotspots } from '../../model/philosophy';
import s from './PhilosophyMap.module.scss';

export const PhilosophyMap: React.FC = () => {
  const [activeId, setActiveId] = useState<string | null>(null);

  // 💡 LOGIC: Toggle on click (for Mobile/Tablet)
  const handleToggle = useCallback((id: string) => {
    setActiveId(prev => prev === id ? null : id);
  }, []);

  // 💡 LOGIC: Hover handlers (for Desktop)
  const handleMouseEnter = (id: string) => {
    if (window.innerWidth > 768) setActiveId(id);
  };

  const handleMouseLeave = () => {
    if (window.innerWidth > 768) setActiveId(null);
  };

  return (
    <div className={s.mapContainer}>
      {/* Hotspots */}
      {hotspots.map((h) => (
        <div
          key={h.id}
          className={s.hotspotWrapper}
          style={{ left: `${h.x}%`, top: `${h.y}%` }}
          onMouseEnter={() => handleMouseEnter(h.id)}
          onMouseLeave={handleMouseLeave}
        >
          <Hotspot
            isActive={activeId === h.id}
            onClick={() => handleToggle(h.id)}
            ariaLabel={h.title}
          />

          {/* Floating Info Card */}
          <AnimatePresence>
            {activeId === h.id && (
              <motion.div
                initial={{ opacity: 0, scale: 0.9, y: 10 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.9, y: 10 }}
                transition={{ duration: 0.3, ease: [0.19, 1, 0.22, 1] }}
                className={`${s.infoCard} ${s[`infoCard--${h.cardPosition}`]}`}
              >
                <Typography variant="technical" color="primary" className={s.cardLabel}>
                  {h.title}
                </Typography>
                <Typography variant="body" color="on-surface" className={s.cardContent}>
                  {h.description}
                </Typography>
                
                {/* Close button only visible on mobile/touch */}
                <button className={s.closeBtn} onClick={() => setActiveId(null)}>
                  Cerrar
                </button>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      ))}
    </div>
  );
};
