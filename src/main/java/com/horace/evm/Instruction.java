package com.horace.evm;

import java.math.BigInteger;

import lombok.Getter;

public abstract class Instruction {

    @Getter
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
        if (context.getProgramCounter() < 0) {
            throw new IndexOutOfBoundsException("Program counter out of bounds");
        }
        if (context.getProgramCounter() >= context.getCode().length) return STOP;
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

    private static void doJump(final BigInteger offset, final ExecutionContext context) {
        if (offset.intValue() < 0 || offset.intValue() >= context.getCode().length) {
            throw new IndexOutOfBoundsException("Jump target out of bounds");
        }
        if (!context.getJumpDestinations().contains(offset.intValue())) {
            throw new IllegalArgumentException("Invalid jump destination: " + offset);
        }
        context.setProgramCounter(offset.intValue());

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

    public static final Instruction JUMP = new Instruction(0x56, "JUMP") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = context.getStack().pop();
            doJump(offset, context);
        }
    };

    public static final Instruction JUMPI = new Instruction(0x57, "JUMPI") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = context.getStack().pop();
            final BigInteger condition = context.getStack().pop();
            if (condition.equals(BigInteger.ZERO)) {
                return;
            }
            doJump(offset, context);
        }
    };

    public static final Instruction JUMPDEST = new Instruction(0x5B, "JUMPDEST") {
        @Override
        public void execute(final ExecutionContext context) {
            // No operation, just a placeholder for jump destination
        }
    };

    public static final Instruction PUSH1 = new Instruction(0x60, "PUSH1") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(1).toByteArray();
            context.getStack().push(value);
        }
    };

    public static final Instruction PUSH2 = new Instruction(0x61, "PUSH2") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(2).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH3 = new Instruction(0x62, "PUSH3") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(3).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH4 = new Instruction(0x63, "PUSH4") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(4).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH5 = new Instruction(0x64, "PUSH5") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(5).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH6 = new Instruction(0x65, "PUSH6") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(6).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH7 = new Instruction(0x66, "PUSH7") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(7).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH8 = new Instruction(0x67, "PUSH8") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(8).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH9 = new Instruction(0x68, "PUSH9") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(9).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH10 = new Instruction(0x69, "PUSH10") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(10).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH11 = new Instruction(0x6A, "PUSH11") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(11).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH12 = new Instruction(0x6B, "PUSH12") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(12).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH13 = new Instruction(0x6C, "PUSH13") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(13).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH14 = new Instruction(0x6D, "PUSH14") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(14).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH15 = new Instruction(0x6E, "PUSH15") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(15).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH16 = new Instruction(0x6F, "PUSH16") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(16).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH17 = new Instruction(0x70, "PUSH17") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(17).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH18 = new Instruction(0x71, "PUSH18") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(18).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH19 = new Instruction(0x72, "PUSH19") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(19).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH20 = new Instruction(0x73, "PUSH20") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(20).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH21 = new Instruction(0x74, "PUSH21") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(21).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH22 = new Instruction(0x75, "PUSH22") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(22).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH23 = new Instruction(0x76, "PUSH23") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(23).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH24 = new Instruction(0x77, "PUSH24") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(24).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH25 = new Instruction(0x78, "PUSH25") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(25).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH26 = new Instruction(0x79, "PUSH26") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(26).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH27 = new Instruction(0x7A, "PUSH27") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(27).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH28 = new Instruction(0x7B, "PUSH28") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(28).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH29 = new Instruction(0x7C, "PUSH29") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(29).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH30 = new Instruction(0x7D, "PUSH30") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(30).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH31 = new Instruction(0x7E, "PUSH31") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(31).toByteArray();
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH32 = new Instruction(0x7F, "PUSH32") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = context.readCode(32).toByteArray();
            context.getStack().push(value);
        }
    };

    public static final Instruction DUP1 = new Instruction(0x80, "DUP1") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(0);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP2 = new Instruction(0x81, "DUP2") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(1);
            context.getStack().push(value);
        }
    };  
    public static final Instruction DUP3 = new Instruction(0x82, "DUP3") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(2);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP4 = new Instruction(0x83, "DUP4") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(3);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP5 = new Instruction(0x84, "DUP5") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(4);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP6 = new Instruction(0x85, "DUP6") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(5);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP7 = new Instruction(0x86, "DUP7") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(6);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP8 = new Instruction(0x87, "DUP8") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(7);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP9 = new Instruction(0x88, "DUP9") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(8);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP10 = new Instruction(0x89, "DUP10") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(9);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP11 = new Instruction(0x8A, "DUP11") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(10);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP12 = new Instruction(0x8B, "DUP12") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(11);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP13 = new Instruction(0x8C, "DUP13") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(12);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP14 = new Instruction(0x8D, "DUP14") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(13);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP15 = new Instruction(0x8E, "DUP15") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(14);
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP16 = new Instruction(0x8F, "DUP16") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = context.getStack().peek(15);
            context.getStack().push(value);
        }
    };
    public static final Instruction SWAP1 = new Instruction(0x90, "SWAP1") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(1);
        }
    };
    public static final Instruction SWAP2 = new Instruction(0x91, "SWAP2") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(2);
        }
    };
    public static final Instruction SWAP3 = new Instruction(0x92, "SWAP3") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(3);
        }
    };
    public static final Instruction SWAP4 = new Instruction(0x93, "SWAP4") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(4);
        }
    };
    public static final Instruction SWAP5 = new Instruction(0x94, "SWAP5") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(5);
        }
    };
    public static final Instruction SWAP6 = new Instruction(0x95, "SWAP6") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(6);
        }
    };
    public static final Instruction SWAP7 = new Instruction(0x96, "SWAP7") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(7);
        }
    };
    public static final Instruction SWAP8 = new Instruction(0x97, "SWAP8") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(8);
        }
    };
    public static final Instruction SWAP9 = new Instruction(0x98, "SWAP9") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(9);
        }
    };
    public static final Instruction SWAP10 = new Instruction(0x99, "SWAP10") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(10);
        }
    };
    public static final Instruction SWAP11 = new Instruction(0x9A, "SWAP11") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(11);
        }
    };
    public static final Instruction SWAP12 = new Instruction(0x9B, "SWAP12") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(12);
        }
    };
    public static final Instruction SWAP13 = new Instruction(0x9C, "SWAP13") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(13);
        }
    };
    public static final Instruction SWAP14 = new Instruction(0x9D, "SWAP14") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(14);
        }
    };
    public static final Instruction SWAP15 = new Instruction(0x9E, "SWAP15") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(15);
        }
    };
    public static final Instruction SWAP16 = new Instruction(0x9F, "SWAP16") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().swap(16);
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
