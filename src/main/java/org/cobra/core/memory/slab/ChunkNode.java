package org.cobra.core.memory.slab;

class ChunkNode {

    private final SlabLoc self;
    private ChunkNode next;

    public ChunkNode(SlabLoc self) {
        this.self = self;
        this.next = null;
    }

    public SlabLoc getSelf() {
        return this.self;
    }

    public ChunkNode getNext() {
        return this.next;
    }

    public void setNext(ChunkNode next) {
        this.next = next;
    }
}
