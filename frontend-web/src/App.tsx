/**
 * @file App.tsx
 * @description Application entry point
 * @date 2024-05-07
 * @author Sergio
 */

import { RouterProvider, ThemeProvider } from '@/app/providers';

export default function App() {
  return (
    <ThemeProvider>
      <RouterProvider />
    </ThemeProvider>
  );
}
