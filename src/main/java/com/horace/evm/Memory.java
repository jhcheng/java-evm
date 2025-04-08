package com.horace.evm;

import java.math.BigInteger;

public class Memory {

    private static final int MAX_SIZE = 1024 * 1024;
    private static final int WORD_SIZE = 256 / 8;

    private final byte[][] memory = new byte[MAX_SIZE][];

    public void store(final int offset, final byte[] value) {
        if (offset < 0 || offset >= MAX_SIZE) {
            throw new IndexOutOfBoundsException("Memory index out of bounds");
        }
        if (Helper.checkValueRange(value)) {
            memory[offset] = value;
        }
    }

    public byte[] load(final int index) {
        if (index < 0 || index >= MAX_SIZE) {
            throw new IndexOutOfBoundsException("Memory index out of bounds");
        }
        final byte[] value = memory[index];
        if (value == null) {
            return new byte[0];
        }
        return value;
    }

    public byte[][] load(final int offset, final int size) {
        if (offset < 0 || offset >= MAX_SIZE) {
            throw new IndexOutOfBoundsException("Memory index out of bounds");
        }
        if (size <= 0 || offset + size > MAX_SIZE) {
            throw new IllegalArgumentException("Invalid length");
        }
        final byte[][] result = new byte[size][];
        for (int i = 0; i < size; i++) {
            result[i] = memory[offset + i];
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory: [");
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != null) {
                sb.append(new BigInteger(memory[i]).longValue());
                if (i < memory.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
