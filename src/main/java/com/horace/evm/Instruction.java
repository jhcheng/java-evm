package com.horace.evm;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.BigIntegers;

import lombok.Getter;

public abstract class Instruction {

    @Getter
    private final int opcode;
    private final String name;

    public static final int MAX_OPCODE = 0xFF;
    public static final int MIN_OPCODE = 0x00;

    public static final Instruction[] INSTRUCTIONS = new Instruction[MAX_OPCODE + 1];

    private static final byte[] TRUE = { 0x01 };
    private static final byte[] FALSE = { 0x00 };
    private static final byte[] ZERO_BYTE = { 0x00 };

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
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.add(b)));
        }
    };

    public static final Instruction MUL = new Instruction(0x02, "MUL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.multiply(b)));
        }
    };
    public static final Instruction SUB = new Instruction(0x03, "SUB") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.subtract(b)));
        }
    };
    public static final Instruction DIV = new Instruction(0x04, "DIV") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(b.equals(BigInteger.ZERO) ? BigInteger.ZERO : Helper.mod256(a.divide(b)));
        }
    };
    public static final Instruction SDIV = new Instruction(0x05, "SDIV") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger b = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger div = b.equals(BigInteger.ZERO) ? BigInteger.ZERO : a.divide(b);
            context.getStack().push(Helper.toUnsigned256Int(div));
        }
    };
    public static final Instruction MOD = new Instruction(0x06, "MOD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(b.equals(BigInteger.ZERO) ? BigInteger.ZERO : Helper.mod256(a.mod(b)));
        }
    };
    public static final Instruction SMOD = new Instruction(0x07, "SMOD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger b = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger mod = b.equals(BigInteger.ZERO) ? BigInteger.ZERO : a.mod(b);
            context.getStack().push(Helper.toUnsigned256Int(mod));
        }
    };
    public static final Instruction ADDMOD = new Instruction(0x08, "ADDMOD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            final BigInteger c = new BigInteger(context.getStack().pop());
            context.getStack().push(c.equals(BigInteger.ZERO) ? BigInteger.ZERO : Helper.mod256(a.add(b).mod(c)));
        }
    };
    public static final Instruction MULMOD = new Instruction(0x09, "MULMOD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            final BigInteger c = new BigInteger(context.getStack().pop());
            context.getStack().push(c.equals(BigInteger.ZERO) ? BigInteger.ZERO : Helper.mod256(a.multiply(b).mod(c)));
        }
    };
    public static final Instruction EXP = new Instruction(0x0a, "EXP") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.pow(b.intValue())));
        }
    };
    public static final Instruction SIGNEXTEND = new Instruction(0x0b, "SIGNEXTEND") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger b = new BigInteger(context.getStack().pop());
            final BigInteger x = new BigInteger(context.getStack().pop());
            final byte[] bytes = Helper.signExtend(x, b.intValue());
            context.getStack().push(bytes);
        }
    };
    public static final Instruction LT = new Instruction(0x10, "LT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(a.compareTo(b) < 0 ? TRUE : FALSE);
        }
    };
    public static final Instruction GT = new Instruction(0x11, "GT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            final BigInteger b = new BigInteger(context.getStack().pop());
            context.getStack().push(a.compareTo(b) > 0 ? TRUE : FALSE);
        }
    };
    public static final Instruction SLT = new Instruction(0x12, "SLT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger b = Helper.toSigned256Int(context.getStack().pop());
            context.getStack().push(a.compareTo(b) < 0 ? TRUE : FALSE);
        }
    };
    public static final Instruction SGT = new Instruction(0x13, "SGT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = Helper.toSigned256Int(context.getStack().pop());
            final BigInteger b = Helper.toSigned256Int(context.getStack().pop());
            context.getStack().push(a.compareTo(b) > 0 ? TRUE : FALSE);
        }
    };
    public static final Instruction EQ = new Instruction(0x14, "EQ") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] a = context.getStack().pop();
            final byte[] b = context.getStack().pop();
            context.getStack().push(Arrays.equals(a, b) ? TRUE : FALSE);
        }
    };
    public static final Instruction ISZERO = new Instruction(0x15, "ISZERO") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = new BigInteger(context.getStack().pop());
            context.getStack().push(a.equals(BigInteger.ZERO) ? TRUE : FALSE);
        }
    };
    public static final Instruction AND = new Instruction(0x16, "AND") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            final BigInteger b = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.and(b)));
        }
    };
    public static final Instruction OR = new Instruction(0x17, "OR") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            final BigInteger b = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.or(b)));
        }
    };
    public static final Instruction XOR = new Instruction(0x18, "XOR") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            final BigInteger b = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.xor(b)));
        }
    };
    public static final Instruction NOT = new Instruction(0x19, "NOT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger a = BigIntegers.fromUnsignedByteArray(context.getStack().pop());
            context.getStack().push(Helper.mod256(a.not()));
        }
    };
    public static final Instruction BYTE = new Instruction(0x1A, "BYTE") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger index = new BigInteger(context.getStack().pop());
            final byte[] value = context.getStack().pop();
            if (index.intValue() < 0 || index.intValue() >= value.length) {
                context.getStack().push(ZERO_BYTE);
            }
            context.getStack().push(new byte[] {value[index.intValue()]});
        }
    };
    public static final Instruction SHL = new Instruction(0x1B, "SHL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger shift = new BigInteger(context.getStack().pop());
            final BigInteger value = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(value.shiftLeft(shift.intValue())));
        }
    };
    public static final Instruction SHR = new Instruction(0x1C, "SHR") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger shift = new BigInteger(context.getStack().pop());
            final BigInteger value = new BigInteger(context.getStack().pop());
            context.getStack().push(Helper.mod256(value.shiftRight(shift.intValue())));
        }
    };
    public static final Instruction SAR = new Instruction(0x1D, "SAR") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger shift = new BigInteger(context.getStack().pop());
            final BigInteger value = Helper.toSigned256Int(context.getStack().pop());
            context.getStack().push(Helper.mod256(value.shiftRight(shift.intValue())));
        }
    };
    public static final Instruction KECCAK256 = new Instruction(0x20, "KECCAK256") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] data = context.getMemory().load(offset.intValue(), size.intValue());
            final Keccak.Digest256 digest256 = new Keccak.Digest256();
            final byte[] hash = digest256.digest(data);
            context.getStack().push(hash);
        }
    };
    public static final Instruction ADDRESS = new Instruction(0x30, "ADDRESS") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getTxData().getTo());
        }
    };
    public static final Instruction BALANCE = new Instruction(0x31, "BALANCE") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] address = context.getStack().pop();
            final BigInteger balance = GlobalState.getInstance().getAccount(address).getBalance();
            context.getStack().push(BigIntegers.asUnsignedByteArray(balance));
        }
    };
    public static final Instruction ORIGIN = new Instruction(0x32, "ORIGIN") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getTxData().getOrigin());
        }
    };
    public static final Instruction CALLER = new Instruction(0x33, "CALLER") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getTxData().getFrom());
        }
    };
    public static final Instruction CALLVALUE = new Instruction(0x34, "CALLVALUE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getTxData().getValue());
        }
    };
    public static final Instruction CALLDATALOAD = new Instruction(0x35, "CALLDATALOAD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            byte[] calldataWord = new byte[32];
            System.arraycopy(context.getTxData().getData(), offset.intValue(), calldataWord, 0, 32);
            context.getStack().push(calldataWord);
        }
    };
    public static final Instruction CALLDATASIZE = new Instruction(0x36, "CALLDATASIZE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(BigIntegers.asUnsignedByteArray(
                BigInteger.valueOf(context.getTxData().getData().length)));
        }
    };
    public static final Instruction CALLDATACOPY = new Instruction(0x37, "CALLDATACOPY") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger destOffset = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());        
            context.getMemory().store(destOffset.intValue(), Arrays.copyOfRange(context.getTxData().getData(), offset.intValue(), size.intValue()));
        }
    };
    public static final Instruction CODESIZE = new Instruction(0x38, "CODESIZE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(Helper.intToByteArray(context.getCode().length));
        }
    };
    public static final Instruction CODECOPY = new Instruction(0x39, "CODECOPY") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger destOffset = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            context.getMemory().store(destOffset.intValue(), Arrays.copyOfRange(context.getCode(), offset.intValue(), size.intValue()));
        }
    };
    public static final Instruction GASPRICE = new Instruction(0x3A, "GASPRICE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getTxData().getGasPrice());
        }
    };
    public static final Instruction EXTCODESIZE = new Instruction(0x3B, "EXTCODESIZE") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] address = context.getStack().pop();
            final int size = GlobalState.getInstance().getAccount(address).getCode().length;
            context.getStack().push(Helper.intToByteArray(size));
        }
    };
    public static final Instruction EXTCODECOPY = new Instruction(0x3C, "EXTCODECOPY") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] address = context.getStack().pop();
            final BigInteger destOffset = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] code = GlobalState.getInstance().getAccount(address).getCode();
            context.getMemory().store(destOffset.intValue(), Arrays.copyOfRange(code, offset.intValue(), size.intValue()));
        }
    };
    public static final Instruction RETURNDATASIZE = new Instruction(0x3D, "RETURNDATASIZE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(Helper.intToByteArray(context.getReturnData().length));
        }
    };
    public static final Instruction RETURNDATACOPY = new Instruction(0x3E, "RETURNDATACOPY") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger destOffset = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            context.getMemory().store(destOffset.intValue(), Arrays.copyOfRange(context.getReturnData(), offset.intValue(), size.intValue()));
        }
    };
    public static final Instruction EXTCODEHASH = new Instruction(0x3F, "EXTCODEHASH") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] address = context.getStack().pop();
            final Account account = GlobalState.getInstance().getAccount(address);
            if (account == Account.NULL_ACCOUNT) {
                context.getStack().push(ZERO_BYTE);
            } else {
                final byte[] code = account.getCode();
                final Keccak.Digest256 digest256 = new Keccak.Digest256();
                final byte[] codeHash = digest256.digest(code);
                context.getStack().push(codeHash);
            }
        }
    };
    public static final Instruction BLOCKHASH = new Instruction(0x40, "BLOCKHASH") {
        @Override
        public void execute(final ExecutionContext context) {
            // Not implemented
            final BigInteger blockNumber = new BigInteger(context.getStack().pop());
        }
    };
    public static final Instruction COINBASE = new Instruction(0x41, "COINBASE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getBlock().getCoinbase());
        }
    };
    public static final Instruction TIMESTAMP = new Instruction(0x42, "TIMESTAMP") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(BigIntegers.asUnsignedByteArray(context.getBlock().getTimestamp()));
        }
    };
    public static final Instruction NUMBER = new Instruction(0x43, "NUMBER") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(BigIntegers.asUnsignedByteArray(context.getBlock().getNumber()));
        }
    };
    public static final Instruction PREVRANDAO = new Instruction(0x44, "PREVRANDAO") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getBlock().getPrevRandao());
        }
    };

    public static final Instruction GASLIMIT = new Instruction(0x45, "GASLIMIT") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(BigIntegers.asUnsignedByteArray(context.getBlock().getGasLimit()));
        }
    };
    public static final Instruction CHAINID = new Instruction(0x46, "CHAINID") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(BigIntegers.asUnsignedByteArray(context.getChainId()));
        }
    };
    public static final Instruction SELFBALANCE = new Instruction(0x47, "SELFBALANCE") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger balance = context.getContract().getBalance();
            context.getStack().push(BigIntegers.asUnsignedByteArray(balance));
        }
    };

    public static final Instruction POP = new Instruction(0x50, "POP") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().pop();
        }
    };
    public static final Instruction MLOAD = new Instruction(0x51, "MLOAD") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final byte[] value = context.getMemory().load(offset.intValue(), 32);
            context.getStack().push(value);
        }
    };
    public static final Instruction MSTORE = new Instruction(0x52, "MSTORE") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final byte[] value = context.getStack().pop();
            context.getMemory().store(offset.intValue(), value);
        }
    };
    public static final Instruction MSTORE8 = new Instruction(0x53, "MSTORE8") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger value = new BigInteger(context.getStack().pop());
            context.getMemory().store(offset.intValue(), value.toByteArray());
        }
    };
    public static final Instruction SLOAD = new Instruction(0x54, "SLOAD") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] key = context.getStack().pop();
            context.getStack().push(context.getStorage().get(context.getTxData().getTo(), key));
        }
    };
    public static final Instruction SSTORE = new Instruction(0x55, "SSTORE") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] key = context.getStack().pop();
            final byte[] value = context.getStack().pop();
            context.getStorage().put(context.getTxData().getTo(), key, value);
        }
    };
    public static final Instruction JUMP = new Instruction(0x56, "JUMP") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            doJump(offset, context);
        }
    };
    public static final Instruction JUMPI = new Instruction(0x57, "JUMPI") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger condition = new BigInteger(context.getStack().pop());
            if (condition.equals(BigInteger.ZERO)) {
                return;
            }
            doJump(offset, context);
        }
    };
    public static final Instruction PC = new Instruction(0x58, "PC") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(Helper.intToByteArray(context.getProgramCounter()));
        }
    };
    public static final Instruction MSIZE = new Instruction(0x59, "MSIZE") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(Helper.intToByteArray(context.getMemory().size()));
        }
    };
    public static final Instruction GAS = new Instruction(0x5A, "GAS") {
        @Override
        public void execute(final ExecutionContext context) {
            //context.getStack().push(Helper.intToByteArray(context.getGas()));
        }
    };
    public static final Instruction JUMPDEST = new Instruction(0x5B, "JUMPDEST") {
        @Override
        public void execute(final ExecutionContext context) {
            // No operation, just a placeholder for jump destination
        }
    };
    public static final Instruction TLOAD = new Instruction(0x5C, "TLOAD") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] key = context.getStack().pop();
            //context.getStack().push(context.getStorage().load(offset));
        }
    };
    public static final Instruction TSTORE = new Instruction(0x5D, "TSTORE") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] key = context.getStack().pop();
            final byte[] value = context.getStack().pop();
            //context.getStorage().store(offset, value);
        }
    };
    public static final Instruction MCOPY = new Instruction(0x5E, "MCOPY") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger destOffset = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            /*
            final byte[] data = context.getCode();
            if (offset.intValue() < 0 || offset.intValue() >= data.length) {
                return;
            }
            context.getMemory().store(destOffset.intValue(), Arrays.copyOfRange(data, offset.intValue(), size.intValue()));
            */
        }
    };
    public static final Instruction PUSH0 = new Instruction(0x5F, "PUSH0") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(new byte[] { 0x00 });
        }
    };
    public static final Instruction PUSH1 = new Instruction(0x60, "PUSH1") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(1));
            context.getStack().push(value);
        }
    };

    public static final Instruction PUSH2 = new Instruction(0x61, "PUSH2") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(2));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH3 = new Instruction(0x62, "PUSH3") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(3));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH4 = new Instruction(0x63, "PUSH4") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(4));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH5 = new Instruction(0x64, "PUSH5") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(5));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH6 = new Instruction(0x65, "PUSH6") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(6));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH7 = new Instruction(0x66, "PUSH7") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(7));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH8 = new Instruction(0x67, "PUSH8") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(8));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH9 = new Instruction(0x68, "PUSH9") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(9));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH10 = new Instruction(0x69, "PUSH10") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(10));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH11 = new Instruction(0x6A, "PUSH11") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(11));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH12 = new Instruction(0x6B, "PUSH12") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(12));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH13 = new Instruction(0x6C, "PUSH13") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(13));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH14 = new Instruction(0x6D, "PUSH14") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(14));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH15 = new Instruction(0x6E, "PUSH15") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(15));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH16 = new Instruction(0x6F, "PUSH16") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(16));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH17 = new Instruction(0x70, "PUSH17") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(17));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH18 = new Instruction(0x71, "PUSH18") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(18));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH19 = new Instruction(0x72, "PUSH19") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(19));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH20 = new Instruction(0x73, "PUSH20") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(20));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH21 = new Instruction(0x74, "PUSH21") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(21));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH22 = new Instruction(0x75, "PUSH22") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(22));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH23 = new Instruction(0x76, "PUSH23") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(23));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH24 = new Instruction(0x77, "PUSH24") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(24));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH25 = new Instruction(0x78, "PUSH25") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(25));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH26 = new Instruction(0x79, "PUSH26") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(26));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH27 = new Instruction(0x7A, "PUSH27") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(27));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH28 = new Instruction(0x7B, "PUSH28") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(28));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH29 = new Instruction(0x7C, "PUSH29") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(29));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH30 = new Instruction(0x7D, "PUSH30") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(30));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH31 = new Instruction(0x7E, "PUSH31") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(31));
            context.getStack().push(value);
        }
    };
    public static final Instruction PUSH32 = new Instruction(0x7F, "PUSH32") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] value = Helper.asUnsignedByteArray(context.readCode(32));
            context.getStack().push(value);
        }
    };
    public static final Instruction DUP1 = new Instruction(0x80, "DUP1") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(0));
        }
    };
    public static final Instruction DUP2 = new Instruction(0x81, "DUP2") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(1));
        }
    };      
    public static final Instruction DUP3 = new Instruction(0x82, "DUP3") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(2));
        }
    };
    public static final Instruction DUP4 = new Instruction(0x83, "DUP4") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(3));
        }
    };
    public static final Instruction DUP5 = new Instruction(0x84, "DUP5") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(4));
        }
    };
    public static final Instruction DUP6 = new Instruction(0x85, "DUP6") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(5));
        }
    };
    public static final Instruction DUP7 = new Instruction(0x86, "DUP7") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(6));
        }
    };
    public static final Instruction DUP8 = new Instruction(0x87, "DUP8") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(7));
        }
    };
    public static final Instruction DUP9 = new Instruction(0x88, "DUP9") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(8));
        }
    };
    public static final Instruction DUP10 = new Instruction(0x89, "DUP10") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(9));
        }
    };
    public static final Instruction DUP11 = new Instruction(0x8A, "DUP11") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(10));
        }
    };
    public static final Instruction DUP12 = new Instruction(0x8B, "DUP12") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(11));
        }
    };
    public static final Instruction DUP13 = new Instruction(0x8C, "DUP13") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(12));
        }
    };
    public static final Instruction DUP14 = new Instruction(0x8D, "DUP14") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(13));
        }
    };
    public static final Instruction DUP15 = new Instruction(0x8E, "DUP15") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(14));
        }
    };
    public static final Instruction DUP16 = new Instruction(0x8F, "DUP16") {
        @Override
        public void execute(final ExecutionContext context) {
            context.getStack().push(context.getStack().peek(15));
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
    public static final Instruction LOG0 = new Instruction(0xA0, "LOG0") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            //context.getLog().log(offset.intValue(), size.intValue(), null);
        }
    };
    public static final Instruction LOG1 = new Instruction(0xA1, "LOG1") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] topic = context.getStack().pop();
            //context.getLog().log(offset.intValue(), size.intValue(), topic);
        }
    };
    public static final Instruction LOG2 = new Instruction(0xA2, "LOG2") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] topic1 = context.getStack().pop();
            final byte[] topic2 = context.getStack().pop();
            //context.getLog().log(offset.intValue(), size.intValue(), topic1, topic2);
        }
    };
    public static final Instruction LOG3 = new Instruction(0xA3, "LOG3") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] topic1 = context.getStack().pop();
            final byte[] topic2 = context.getStack().pop();
            final byte[] topic3 = context.getStack().pop();
            //context.getLog().log(offset.intValue(), size.intValue(), topic1, topic2, topic3);
        }
    };
    public static final Instruction LOG4 = new Instruction(0xA4, "LOG4") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] topic1 = context.getStack().pop();
            final byte[] topic2 = context.getStack().pop();
            final byte[] topic3 = context.getStack().pop();
            final byte[] topic4 = context.getStack().pop();
            //context.getLog().log(offset.intValue(), size.intValue(), topic1, topic2, topic3, topic4);
        }
    };
    public static final Instruction CREATE = new Instruction(0xF0, "CREATE") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            //context.create(value.intValue(), offset.intValue(), size.intValue());
        }
    };
    public static final Instruction CALL = new Instruction(0xF1, "CALL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger gas = new BigInteger(context.getStack().pop());
            final byte[] address = context.getStack().pop();
            final BigInteger value = new BigInteger(context.getStack().pop());
            final BigInteger argsOffset = new BigInteger(context.getStack().pop());
            final BigInteger argsSize = new BigInteger(context.getStack().pop());
            final BigInteger retOffset = new BigInteger(context.getStack().pop());
            final BigInteger retSize = new BigInteger(context.getStack().pop());
            //context.call(gas.intValue(), value.intValue(), address, offset.intValue(), size.intValue());
        }
    };
    public static final Instruction CALLCODE = new Instruction(0xF2, "CALLCODE") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger gas = new BigInteger(context.getStack().pop());
            final byte[] address = context.getStack().pop();
            final BigInteger value = new BigInteger(context.getStack().pop());
            final BigInteger argsOffset = new BigInteger(context.getStack().pop());
            final BigInteger argsSize = new BigInteger(context.getStack().pop());
            final BigInteger retOffset = new BigInteger(context.getStack().pop());
            final BigInteger retSize = new BigInteger(context.getStack().pop());
            //context.call(gas.intValue(), value.intValue(), address, offset.intValue(), size.intValue());
        }
    };
    public static final Instruction RETURN = new Instruction(0xF3, "RETURN") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            context.setReturnData(offset.intValue(), size.intValue());
        }
    };
    public static final Instruction DELEGATECALL = new Instruction(0xF4, "DELEGATECALL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger gas = new BigInteger(context.getStack().pop());
            final byte[] address = context.getStack().pop();
            final BigInteger argsOffset = new BigInteger(context.getStack().pop());
            final BigInteger argsSize = new BigInteger(context.getStack().pop());
            final BigInteger retOffset = new BigInteger(context.getStack().pop());
            final BigInteger retSize = new BigInteger(context.getStack().pop());
            //context.call(gas.intValue(), value.intValue(), address, offset.intValue(), size.intValue());
        }
    };
    public static final Instruction CREATE2 = new Instruction(0xF5, "CREATE2") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger value = new BigInteger(context.getStack().pop());
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            final byte[] salt = context.getStack().pop();
            //context.create(value.intValue(), offset.intValue(), size.intValue());
        }
    };
    public static final Instruction STATICCALL = new Instruction(0xFA, "STATICCALL") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger gas = new BigInteger(context.getStack().pop());
            final byte[] address = context.getStack().pop();
            final BigInteger argsOffset = new BigInteger(context.getStack().pop());
            final BigInteger argsSize = new BigInteger(context.getStack().pop());
            final BigInteger retOffset = new BigInteger(context.getStack().pop());
            final BigInteger retSize = new BigInteger(context.getStack().pop());
            //context.call(gas.intValue(), value.intValue(), address, offset.intValue(), size.intValue());
        }
    };
    public static final Instruction REVERT = new Instruction(0xFD, "REVERT") {
        @Override
        public void execute(final ExecutionContext context) {
            final BigInteger offset = new BigInteger(context.getStack().pop());
            final BigInteger size = new BigInteger(context.getStack().pop());
            //context.revert(offset.intValue(), size.intValue());
        }
    };
    public static final Instruction INVALID = new Instruction(0xFE, "INVALID") {
        @Override
        public void execute(final ExecutionContext context) {
            throw new IllegalStateException("Invalid instruction");
        }
    };
    public static final Instruction SELFDESTRUCT = new Instruction(0xFF, "SELFDESTRUCT") {
        @Override
        public void execute(final ExecutionContext context) {
            final byte[] address = context.getStack().pop();
            // Not implemented
        }
    };

}
