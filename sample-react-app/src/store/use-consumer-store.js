import { create } from 'zustand/react';

export const useConsumerStore = create((set) => ({
  consumers: [],
  add: (url) =>
    set((state) => ({
      consumers: [...state.consumers, { url: url, started: new Date().getTime() }]
    }))
}));
