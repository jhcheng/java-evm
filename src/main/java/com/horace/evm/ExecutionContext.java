package com.horace.evm;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public class ExecutionContext {

    private boolean stopped = false;    
    @Getter
    private byte[] code;
    private int pc;
    @Getter
    private Stack stack;
    @Getter
    private Memory memory;
    @Getter
    private byte[][] returnData = new byte[0][];
    @Getter
    private Set<Integer> jumpDestinations = new HashSet<>();

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
        jumpDestinations = validJumpDestinations(code);
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getProgramCounter() {
        return pc;
    }

    public void setProgramCounter(final int pc) {
        this.pc = pc;
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


    private Set<Integer> validJumpDestinations(byte[] code) {
        Set<Integer> jumpdests = new HashSet<>();
        int i = 0;
        
        while (i < code.length) {
            int currentOp = code[i] & 0xFF;  // Convert to unsigned byte
            
            if (currentOp == Instruction.JUMPDEST.getOpcode()) {
                jumpdests.add(i);
            } else if (currentOp >= Instruction.PUSH1.getOpcode() && currentOp <= Instruction.PUSH32.getOpcode()) {
                // Skip push data bytes (currentOp - PUSH1_OPCODE + 1)
                i += (currentOp - Instruction.PUSH1.getOpcode());
            }
            
            i++;  // Move to next instruction
        }
        
        return jumpdests;
    }

}
