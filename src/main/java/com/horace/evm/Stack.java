package com.horace.evm;

import java.math.BigInteger;

public class Stack {

    private static final int MAX_SIZE = 1024;
    private final byte[][] stack;
    private int top = -1;
    private int size = 0;

    public Stack() {
        this.stack = new byte[MAX_SIZE][];
    }

    public Stack(final int maxSize) {
        this.stack = new byte[Math.min(maxSize, MAX_SIZE)][];
    }

    public void push(final byte[] value) {
        push(new BigInteger(value));
    }

    public void push(final BigInteger value) {
        if (size >= MAX_SIZE) {
            throw new StackOverflowError("Stack overflow");
        }
        if (Helper.checkValueRange(value)) {
            stack[++top] = value.toByteArray();
            size++;
        }
    }
    
    public BigInteger pop() {
        if (size <= 0) {
            throw new IllegalStateException("Stack underflow");
        }
        final BigInteger value = new BigInteger(stack[top]);
        stack[top--] = null;
        size--;
        return value;
    }

    /**
     * Returns the value at the top of the stack without removing it.
     * @return
     */
    public BigInteger peek() {
        if (size <= 0) {
            throw new IllegalStateException("Stack is empty");
        }
        return new BigInteger(stack[top]);
    }

    /**
     * Returns the value at the specified index from the top of the stack without removing it.
     * The top of the stack is at index 0, the next value is at index 1, and so on.
     * @param index
     * @return
     */
    public BigInteger peek(final int index) {
        if (index < 0 || index > top) {
            throw new IndexOutOfBoundsException("Stack index out of bounds");
        }
        return new BigInteger(stack[top - index]);
    }

    /**
     * Swaps the top value of the stack with the value at the specified index from the top of the stack.
     * The top of the stack is at index 0, the next value is at index 1, and so on.
     * For example, if the stack contains [a, b, c], and you call swap(1), the stack will become [b, a, c].
     * @param index
     */
    public void swap(final int index) {
        if (index < 0 || index > top) {
            throw new IndexOutOfBoundsException("Stack index out of bounds");
        }
        final byte[] temp = stack[top];
        stack[top] = stack[top - index];
        stack[top - index] = temp;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stack: [");
        for (int i = 0; i <= top; i++) {
            sb.append(new BigInteger(stack[i]).longValue());
            if (i < top) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
}
