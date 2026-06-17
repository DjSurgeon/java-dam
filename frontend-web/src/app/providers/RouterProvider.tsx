/**
 * @file RouterProvider.tsx
 * @description Centralized routing configuration
 * @date 2024-05-07
 * @author Sergio
 */

import React from 'react';
import { createBrowserRouter, RouterProvider as ReactRouterProvider } from 'react-router-dom';
import { LandingPage } from '@/pages/LandingPage';
import { BookingApp } from '@/pages/BookingApp';

const router = createBrowserRouter([
  {
    path: '/',
    element: <LandingPage />,
  },
  {
    path: '/reservas',
    element: <BookingApp />,
  },
]);

export const RouterProvider: React.FC = () => {
  return <ReactRouterProvider router={router} />;
};
