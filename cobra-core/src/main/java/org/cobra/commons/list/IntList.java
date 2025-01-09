package org.cobra.commons.list;

import java.util.Arrays;
import java.util.Objects;

public class IntList {

    private int[] barr;
    private int size;

    public IntList() {
        this(16);
    }

    public IntList(int initCapacity) {
        this.barr = new int[initCapacity];
    }

    public int capacity() {
        return this.barr.length;
    }

    public int size() {
        return this.size;
    }

    public void add(int value) {
        if (this.size == capacity())
            expandTo(this.size * 3 / 2);

        this.barr[this.size++] = value;
    }

    public void set(int index, int value) {
        ensureBound(index);
        this.barr[index] = value;
    }

    public int get(int index) {
        ensureBound(index);
        return this.barr[index];

    }

    public void sort() {
        Arrays.sort(this.barr, 0, this.size);
    }

    public void reverse() {
        final int mid = this.size / 2;
        int temp;
        for (int i = 0; i < mid; i++) {
            temp = this.barr[i];
            this.barr[i] = barr[this.size - 1 - i];
            this.barr[this.size - 1 - i] = temp;
        }
    }

    public void clear() {
        this.size = 0;
    }

    public void expandTo(int size) {
        this.barr = Arrays.copyOf(this.barr, size);
    }

    public int[] copyRangeOf(int fromIndex, int toIndex) {
        int[] arr = new int[toIndex - fromIndex];
        System.arraycopy(this.barr, fromIndex, arr, 0, arr.length);
        return arr;
    }

    private void ensureBound(int index) {
        if (index >= this.size)
            throw new IllegalArgumentException("Index out of limit; limit = %d".formatted(this.size));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        IntList intList = (IntList) object;
        if (size() != intList.size()) return false;
        if (capacity() != intList.capacity()) return false;

        for (int i = 0; i < size(); i++) {
            if (get(i) != intList.get(i)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(barr), size);
    }

    @Override
    public String toString() {
        return "IntList(capacity=%d, limit=%d)".formatted(capacity(), size);
    }
}
