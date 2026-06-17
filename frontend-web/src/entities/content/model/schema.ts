/**
 * @file schema.ts
 * @description Zod schemas for content validation
 * @date 2024-05-07
 * @author Sergio
 */

import { z } from 'zod';

export const BlogPostSchema = z.object({
  title: z.string(),
  category: z.string(),
  image: z.string(),
  date: z.string(),
});

export const LandingContentSchema = z.object({
  hero: z.object({
    badge: z.string(),
    title: z.string(),
    highlight: z.string(),
    ctaText: z.string(),
    scrollText: z.string(),
  }),
  blog: z.array(BlogPostSchema),
});

export type BlogPost = z.infer<typeof BlogPostSchema>;
export type LandingContent = z.infer<typeof LandingContentSchema>;
