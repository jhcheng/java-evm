package com.horace.evm;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import lombok.Getter;

public class ExecutionContext {

    private boolean stopped = false;    
    @Getter
    private byte[] code;
    @Getter
    private int pc;
    @Getter
    private Stack stack;
    @Getter
    private Memory memory;
    @Getter
    private byte[] returnData = new byte[0];

    // Constructors
    public ExecutionContext() {
        this(new byte[0], 0, new Stack(), new Memory());
    }

    public ExecutionContext(final byte[] code) {
        this(code, 0, new Stack(), new Memory());
    }

    public ExecutionContext(byte[] code, int pc, Stack stack, Memory memory) {
        this.code = code;
        this.pc = pc;
        this.stack = stack;
        this.memory = memory;
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public BigInteger readCode(final int numBytes) {
        final ByteBuffer buffer = ByteBuffer.wrap(this.code).position(this.pc);
        pc += numBytes;
        final byte[] bytes = new byte[numBytes];
        buffer.get(bytes);
        return new BigInteger(bytes);
    }

    public void setReturnData(final int offset, final int size) {
        stopped = true;
        returnData = memory.load(offset, size);
    }

}
