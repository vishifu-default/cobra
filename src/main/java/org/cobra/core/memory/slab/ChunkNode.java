package org.cobra.core.memory.slab;

class ChunkNode {

    private final int selfIndex;
    private ChunkNode next;

    public ChunkNode(int self) {
        this.selfIndex = self;
        this.next = null;
    }

    public int getSelf() {
        return this.selfIndex;
    }

    public ChunkNode getNext() {
        return this.next;
    }

    public void setNext(ChunkNode next) {
        this.next = next;
    }
}
