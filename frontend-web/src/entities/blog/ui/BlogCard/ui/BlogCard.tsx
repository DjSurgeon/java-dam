/**
 * @file BlogCard.tsx
 * @description Entity component for rendering a single blog post
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { ArrowRight } from 'lucide-react';
import { Typography } from '@/shared/ui/atoms/Typography';
import { LandingContent } from '@/entities/content/model/schema';
import s from './BlogCard.module.scss';

interface BlogCardProps {
  post: LandingContent['blog'][0];
}

export const BlogCard: React.FC<BlogCardProps> = ({ post }) => {
  return (
    <article className={s.card}>
      <div className={s.cardInner}>
        <div className={s.imageContainer}>
          <div 
            style={{ backgroundImage: `url(${post.image})` }}
            className={s.image}
          />
          <div className={s.imageOverlay} />
          <div className={s.glint} />
        </div>
        
        <div className={s.content}>
          <div className={s.meta}>
            <Typography variant="technical" color="primary">{post.category}</Typography>
            <div className={s.metaLine}></div>
          </div>
          
          <div className={s.title}>
            <Typography variant="h3" color="on-background">
              {post.title}
            </Typography>
          </div>
          
          <div className={s.footer}>
            <Typography variant="technical" color="on-surface-variant" style={{ fontSize: '11px' }}>
              {post.date}
            </Typography>
            
            <div className={s.link}>
              <Typography variant="technical" color="primary" style={{ fontSize: '11px' }}>
                Explorar
              </Typography>
              <ArrowRight size={14} className={s.icon} />
            </div>
          </div>
        </div>
      </div>
    </article>
  );
};
