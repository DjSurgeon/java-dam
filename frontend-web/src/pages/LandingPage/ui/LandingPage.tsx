/**
 * @file LandingPage.tsx
 * @description Composition of the Landing Page
 * @date 2024-05-07
 * @author Sergio
 */

import React, { Suspense, lazy } from 'react';
import { Navbar } from '@/widgets/Navbar';
import { Hero } from '@/widgets/Hero';
import { loadLandingContent } from '@/shared/lib/contentLoader';
import { Skeleton } from '@/shared/ui/atoms/Skeleton';

// Lazy load below-the-fold components, adapting named exports to default exports
const BlogGrid = lazy(() => import('@/widgets/BlogGrid').then(m => ({ default: m.BlogGrid })));
const ContactForm = lazy(() => import('@/features/ContactForm').then(m => ({ default: m.ContactForm })));
const Footer = lazy(() => import('@/widgets/Footer').then(m => ({ default: m.Footer })));

export const LandingPage: React.FC = () => {
  const content = loadLandingContent();

  return (
    <>
      <Navbar />
      <main>
        <Hero content={content.hero} />

        <Suspense fallback={<Skeleton height="100vh" />}>
          <BlogGrid posts={content.blog} />
        </Suspense>

        <Suspense fallback={<Skeleton height="100vh" />}>
          <ContactForm />
        </Suspense>
      </main>
      
      <Suspense fallback={<Skeleton height="400px" />}>
        <Footer />
      </Suspense>
    </>
  );
};
