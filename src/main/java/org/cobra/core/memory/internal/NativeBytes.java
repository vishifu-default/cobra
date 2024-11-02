package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.core.memory.neo.VanillaBytes;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

public class NativeBytes implements VanillaBytes {

    private static final UnsafeMemory memory = Jvm.ofUnsafeMemory();

    private long address;
    private long capacity;
    private final long limitCapacity;


    public NativeBytes(final int log2OfSegment) {
        final int initCapacity = calcInitialCapacity(log2OfSegment);
        allocate(initCapacity);

        this.limitCapacity = (1L << log2OfSegment);
    }

    private int calcInitialCapacity(final int log2OfSegment) {
        final int minLog2Segment = log2OfSegment / 3;
        return (1 << minLog2Segment);
    }

    private void allocate(final int capacity) {
        long address = memory.allocate(capacity);
        setAddress(address);
        this.capacity = capacity;
    }

    private void setAddress(final long address) {
        this.address = address;
    }

    private void ensureCapacity(final int requiredCapacity) {
        if (requiredCapacity < this.capacity)
            return;

        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void write(byte b) {

    }

    @Override
    public void write(long pos, byte b) {

    }

    @Override
    public void writeArray(byte[] arr) {

    }

    @Override
    public void writeArray(byte[] arr, int arrOffset, int len) {

    }

    @Override
    public void writeArray(long pos, byte[] arr) {

    }

    @Override
    public void writeArray(long pos, byte[] arr, int arrOffset, int len) {

    }

    @Override
    public byte read() {
        return 0;
    }

    @Override
    public byte read(long pos) {
        return 0;
    }

    @Override
    public byte @NotNull [] readArray(int len) {
        return new byte[0];
    }

    @Override
    public byte @NotNull [] readArray(long pos, int len) {
        return new byte[0];
    }

    @Override
    public long capacity() {
        return 0;
    }

    @Override
    public long position() {
        return 0;
    }

    @Override
    public void seek(long pos) {

    }

    @Override
    public void release() {

    }

    private long translate(long offset) {
        return this.address + offset;
    }
}
