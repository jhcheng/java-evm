package com.horace.evm;

import java.util.HexFormat;

public class EVM {

    private void run(byte[] code) {
        ExecutionContext context = new ExecutionContext(code);
        while (!context.isStopped()) {
            final int pcBefore = context.getProgramCounter();
            final Instruction instruction = Instruction.decodeOpcode(context);
            instruction.execute(context);
            System.out.println(instruction + " @ pc = " + pcBefore);
            System.out.println(context.getStack());
            System.out.println(context.getMemory());
            System.out.println("====================================");
        }
        if (context.getReturnData().length > 0) {
            System.out.println("RETURN: " + HexFormat.of().formatHex(context.getReturnData()));
        }

    }

    public static void main(String[] args) {
        byte[] code = HexFormat.of().parseHex("60048060005b8160125760005360016000f35b8201906001900390600556");
        EVM evm = new EVM();
        evm.run(code);
    }

}
