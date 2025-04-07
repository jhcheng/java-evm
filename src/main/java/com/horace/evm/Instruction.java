package com.horace.evm;

import java.math.BigInteger;

public abstract class Instruction {

    private final int opcode;
    private final String name;

    public static final int MAX_OPCODE = 0xFF;
    public static final int MIN_OPCODE = 0x00;

    public static final Instruction[] INSTRUCTIONS = new Instruction[MAX_OPCODE + 1];


    public Instruction(final int opcode, final String name) {
        this.opcode = opcode;
        this.name = name;
        INSTRUCTIONS[opcode] = this;
    }

    abstract public void execute(final ExecutionContext context);

    public static Instruction decodeOpcode(final ExecutionContext context) {
        if (context.getPc() < 0) {
            throw new IndexOutOfBoundsException("Program counter out of bounds");
        }
        if (context.getPc() >= context.getCode().length) return STOP;
        final int opcode = Byte.toUnsignedInt(context.readCode(1).byteValue());
        if (opcode < MIN_OPCODE || opcode > MAX_OPCODE) {
            throw new IllegalArgumentException("Invalid opcode: " + opcode);
        }
        final Instruction instruction = INSTRUCTIONS[opcode];
        if (instruction == null) {
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
        return instruction;
    }

    public String toString() {
        return String.format("Instruction{opcode=%02X, name='%s'}", opcode, name);
    }

    public static final Instruction STOP = new Instruction(0x00, "STOP") {
        @Override
        public void execute(final ExecutionContext context) {
            context.stop();
        }
    };

    public static final Instruction ADD = new Instruction(0x01, "ADD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            context.getStack().push(a.add(b));
        }
    };

    public static final Instruction MUL = new Instruction(0x02, "MUL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            context.getStack().push(a.multiply(b));
        }
    };
    public static final Instruction SUB = new Instruction(0x03, "SUB") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            context.getStack().push(a.subtract(b));
        }
    };
    public static final Instruction DIV = new Instruction(0x04, "DIV") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            if (b.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Division by zero");
            }
            context.getStack().push(a.divide(b));
        }
    };
    public static final Instruction MOD = new Instruction(0x06, "MOD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            if (b.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Division by zero");
            }
            context.getStack().push(a.mod(b));
        }
    };
    public static final Instruction EXP = new Instruction(0x0a, "EXP") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = context.getStack().pop();
            final BigInteger b = context.getStack().pop();
            context.getStack().push(a.pow(b.intValue()));
        }
    };
    public static final Instruction MSTORE8 = new Instruction(0x53, "MSTORE8") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = context.getStack().pop();
            final BigInteger value = context.getStack().pop();
            context.getMemory().store(offset.intValue(), value.toByteArray());
        }
    };
    public static final Instruction PUSH1 = new Instruction(0x60, "PUSH1") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(1).toByteArray();
            context.getStack().push(value);
        }
    };

    public static final Instruction RETURN = new Instruction(0xF3, "RETURN") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = context.getStack().pop();
            final BigInteger size = context.getStack().pop();
            context.setReturnData(offset.intValue(), size.intValue());
        }
    };
}
