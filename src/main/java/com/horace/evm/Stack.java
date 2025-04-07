package com.horace.evm;

import java.math.BigInteger;

public class Stack {

    private static final int MAX_SIZE = 1024;
    private final BigInteger[] stack;
    private int top = -1;
    private int size = 0;

    public Stack() {
        this.stack = new BigInteger[MAX_SIZE];
    }

    public Stack(final int maxSize) {
        this.stack = new BigInteger[Math.min(maxSize, MAX_SIZE)];
    }

    public void push(final byte[] value) {
        push(new BigInteger(value));
    }

    public void push(final BigInteger value) {
        if (size >= MAX_SIZE) {
            throw new StackOverflowError("Stack overflow");
        }
        if (Helper.checkValueRange(value)) {
            stack[++top] = value;
            size++;
        }
    }
    
    public BigInteger pop() {
        if (size <= 0) {
            throw new IllegalStateException("Stack underflow");
        }
        final BigInteger value = stack[top];
        stack[top--] = null;
        size--;
        return value;
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
            sb.append(stack[i]);
            if (i < top) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
}
