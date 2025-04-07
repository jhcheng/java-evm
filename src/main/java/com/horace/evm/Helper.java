package com.horace.evm;

import java.math.BigInteger;

public class Helper {
    private static final BigInteger MAX_VALUE = BigInteger.valueOf(2).pow(256).subtract(BigInteger.ONE);
    private static final BigInteger MIN_VALUE = BigInteger.ZERO;

    public static boolean checkValueRange(final BigInteger value) {
        if (value.compareTo(MAX_VALUE) > 0 || value.compareTo(MIN_VALUE) < 0) {
            throw new IllegalArgumentException("Value out of range");
        }
        return true;
    }

}
