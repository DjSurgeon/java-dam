/**
 * @file setup.ts
 * @description Test setup and global mocks
 * @date 2026-05-13
 * @lastUpdate 2026-05-13
 * @author Sergio
 */

import '@testing-library/jest-dom';
import { vi } from 'vitest';

const IntersectionObserverMock = vi.fn(() => ({
  disconnect: vi.fn(),
  observe: vi.fn(),
  takeRecords: vi.fn(),
  unobserve: vi.fn(),
  root: null,
  rootMargin: '',
  thresholds: [],
}));

const ResizeObserverMock = vi.fn(() => ({
  disconnect: vi.fn(),
  observe: vi.fn(),
  unobserve: vi.fn(),
}));

vi.stubGlobal('IntersectionObserver', IntersectionObserverMock);
vi.stubGlobal('ResizeObserver', ResizeObserverMock);
