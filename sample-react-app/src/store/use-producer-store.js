import { create } from 'zustand/react';

export const useProducerStore = create((set) => ({
  producerNode: {
    domain: '',
    port: 0
  },
  updateProducerNode: (node) =>
    set((state) => ({
      producerNode: node
    }))
}));
