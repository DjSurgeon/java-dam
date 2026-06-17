/**
 * @file BlogGrid.tsx
 * @description Blog grid section widget
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { motion } from 'motion/react';
import { ArrowRight } from 'lucide-react';
import { Typography } from '@/shared/ui/atoms/Typography';
import { SectionBadge } from '@/shared/ui/molecules/SectionBadge';
import { BlogCard } from '@/entities/blog';
import { LandingContent } from '@/entities/content/model/schema';
import { useScrollReveal } from '@/shared/lib/hooks/useScrollReveal';
import s from './BlogGrid.module.scss';

interface BlogGridProps {
  posts: LandingContent['blog'];
}

export const BlogGrid: React.FC<BlogGridProps> = ({ posts }) => {
  const { fadeIn } = useScrollReveal();

  return (
    <section id="blog" className={s.section}>
      <div className={s.container}>
        <div className={s.header}>
          <motion.div {...fadeIn} className={s.title}>
            <SectionBadge text="PUBLICACIONES" reverse />
            <Typography variant="h2" color="on-background">
              Cuaderno de <br /> <span style={{ fontStyle: 'italic', fontWeight: 300, color: 'var(--primary)' }}>bitácora.</span>
            </Typography>
          </motion.div>
        </div>

        <div className={s.grid}>
          {posts.map((post, idx) => (
            <motion.div 
              key={post.title}
              {...fadeIn}
              transition={{ delay: idx * 0.15, duration: 1.4 }}
            >
              <BlogCard post={post} />
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};
