/**
 * @file contentLoader.ts
 * @description Centralized content loading and validation
 * @date 2024-05-07
 * @author Sergio
 */

import { LandingContentSchema, LandingContent } from '@/entities/content/model/schema';

const RAW_CONTENT = {
  hero: {
    badge: "SINFONÍA DE LA DEHESA",
    title: "Maestría en",
    highlight: "cada trazo.",
    ctaText: "RESERVAR",
    scrollText: "Deslizar para iniciar el ritual",
  },
  blog: [
    {
      title: "El arte del afilado: La clave del sabor",
      category: "Técnica",
      image: "/images/blog-1.webp",
      date: "12 May, 2024"
    },
    {
      title: "Maridajes premium: Vinos y Jamón Ibérico",
      category: "Cultura",
      image: "/images/blog-2.webp",
      date: "08 May, 2024"
    },
    {
      title: "Historia de la montanera en la dehesa",
      category: "Origen",
      image: "/images/blog-3.webp",
      date: "03 May, 2024"
    }
  ]
};

export const loadLandingContent = (): LandingContent => {
  return LandingContentSchema.parse(RAW_CONTENT);
};
