package org.cobra.core.memory.slab;

public class Freelist {

    private Node head = null;

    public void offer(SlabOffset offset) {
        final Node node = new Node(offset);
        node.setNext(this.head);
        this.head = node;
    }

    public SlabOffset poll() {
        if (this.head == null)
            return null;

        SlabOffset offset;

        offset = this.head.getSelf();
        this.head = this.head.getNext();

        return offset;
    }

    static final class Node {
        private SlabOffset self;
        private Node next = null;

        Node(SlabOffset self) {
            this.self = self;
        }

        public void setSelf(SlabOffset self) {
            this.self = self;
        }

        public SlabOffset getSelf() {
            return this.self;
        }

        public Node getNext() {
            return this.next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }
}
