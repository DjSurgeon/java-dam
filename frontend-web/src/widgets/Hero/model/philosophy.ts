/**
 * @file philosophy.ts
 * @description Data and coordinates for Hero hotspots
 * @date 2024-05-07
 * @author Sergio
 */

export type CardPosition = 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'center-bottom';

export interface HotspotData {
  id: string;
  title: string;
  description: string;
  x: number; // Percentage from left
  y: number; // Percentage from top
  cardPosition: CardPosition;
}

export const hotspots: HotspotData[] = [
  {
    id: 'ritual',
    title: 'El Ritual del Corte',
    description: 'Un corte fino pero firme, donde cada loncha se funde en el paladar. Garantizamos un rendimiento profesional y un ritmo constante para que el flujo de platos nunca se detenga en tu evento.',
    x: 20,
    y: 35,
    cardPosition: 'bottom-right',
  },
  {
    id: 'arte',
    title: 'El Arte del Emplatado',
    description: 'La presentación en espiral o almenas no es solo estética; es clave para el éxito. Un orden definido facilita la degustación y satisface todos los sentidos del comensal.',
    x: 55,
    y: 50,
    cardPosition: 'bottom-right',
  },
  {
    id: 'experiencia',
    title: 'Experiencia y Presencia',
    description: 'Fundado por David Ventura Cano, con más de 10 años de experiencia. Contamos con herramientas de precisión y una presencia impecable para elevar el nivel de cualquier celebración.',
    x: 80,
    y: 75,
    cardPosition: 'top-left',
  },
];
