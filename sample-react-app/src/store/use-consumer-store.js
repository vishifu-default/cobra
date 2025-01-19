import { create } from 'zustand/react';

export const useConsumerStore = create((set) => ({
  consumers: [],
  add: (conf) =>
    set((state) => ({
      consumers: [...state.consumers, conf]
    })),
  remove: (id) =>
    set((state) => ({
      consumers: state.consumers.filter((c) => c.id !== id)
    }))
}));
