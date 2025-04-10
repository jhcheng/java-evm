package com.horace.evm;

import java.nio.ByteBuffer;
import java.util.HexFormat;

/**
// From the yellow paper:
// The memory model is a simple word-addressed byte array.
// The word size is 256 bits (32 bytes).
// The memory is expanded by a word (32 bytes) at a time. Memory expansion costs gas.
// The memory size is always a multiple of 32 bytes.
// The memory starts empty at the beginning of every instance execution.
 * 
 */
public class Memory {

    private static final int WORD_SIZE_IN_BYTE = 256 / 8;

    private ByteBuffer memory = ByteBuffer.allocate(0);
    private static final String INVALID_MEMORY_OFFSET = "Invalid memory offset";
    private static final String INVALID_MEMORY_VALUE_SIZE = "Invalid memory value size";

    //private final List<byte[]> memory = new ArrayList<>();

    public void store(final int offset, final byte[] value) {
        store(offset, value, WORD_SIZE_IN_BYTE);
    }

    public void store(final int offset, final byte[] value, final int size) {
        if (offset < 0) {
            throw new IllegalArgumentException(INVALID_MEMORY_OFFSET);
        }

        checkIfMemoryNeedsExpansion(offset, size);

        byte[] word = new byte[WORD_SIZE_IN_BYTE];
        System.arraycopy(value, 0, word, 0, Math.min(value.length, word.length));
        this.memory.put(word);
    }


    public byte[] load(final int offset) {
        return load(offset, WORD_SIZE_IN_BYTE);
    }

    public byte[] load(final int offset, final int size) {
        if (offset < 0) {
            throw new IllegalArgumentException(INVALID_MEMORY_OFFSET);
        }
        if (size == 0) {
            return new byte[0];
        }

        checkIfMemoryNeedsExpansion(offset, size);

        byte[] output = new byte[size];
        for (int i = 0; i < size; i++) {
            output[i] = this.memory.get(offset + i);
        }
        return output;
    }

    private boolean checkIfMemoryNeedsExpansion(final int offset, final int size) {
        final int overflow = (int) (Math.ceil((double) (offset + size) / 32) * 32 - this.size());
        if (overflow > 0) {
            ByteBuffer newMemory = ByteBuffer.allocate(this.size() + overflow);
            this.memory.rewind(); // Reset position to 0
            newMemory.put(this.memory);
            this.memory = newMemory;
        }
        return overflow > 0;
    }

    public String toString() {
        final StringBuilder dump = new StringBuilder();
        for (int i = 0; i < this.memory.position(); i += 32) {
            int end = Math.min(i + 32, this.memory.position());
            byte[] word = new byte[end - i];
            for (int j = 0; j < end - i; j++) {
                word[j] = this.memory.get(i + j);
            }
            dump.append(HexFormat.of().formatHex(word)).append("\n");
        }
        return dump.toString();
    }

    public int size() {
        return memory.position();
    }

    public int activeWordsCount() {
        return this.size() / WORD_SIZE_IN_BYTE;
    }

}
