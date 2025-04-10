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

    public static boolean checkValueRange(final byte[] value) {
        return checkValueRange(new BigInteger(value));
    }

    public static BigInteger mod256(final BigInteger value) {
        final BigInteger mod = value.mod(MAX_VALUE);
        return mod.compareTo(BigInteger.ZERO) < 0 ? mod.add(MAX_VALUE) : mod;
    }

    public static BigInteger toUnsignedBigInteger(final byte[] bytes) {        
        return new BigInteger(1, bytes);
    }

    public static BigInteger toUnsignedBigInteger(final byte[] bytes, final int sizeInBits) {        
        return new BigInteger(1, bytes, 0, Math.min(bytes.length, sizeInBits / 8));
    }

    public static BigInteger toUnsignedBigInteger(final BigInteger value, final int sizeInBits) {
        return value.signum() > 0 ? value : value.add(BigInteger.TWO.pow(sizeInBits));
    }

    public static BigInteger toSignedBigInteger(final BigInteger value, final int sizeInBits) {
        final BigInteger maxSigned = BigInteger.TWO.pow(sizeInBits - 1).subtract(BigInteger.ONE);
        return (value.compareTo(maxSigned) <= 0) ? 
            value : 
            value.subtract(BigInteger.TWO.pow(sizeInBits));
    }

    public static BigInteger toSigned256Int(final BigInteger value) {
        return toSignedBigInteger(value, 256);
    }

    public static BigInteger toSigned256Int(final byte[] bytes) {
        return toSignedBigInteger(new BigInteger(bytes), 256);
    }

    public static BigInteger toUnsigned256Int(final BigInteger value) {
        return toUnsignedBigInteger(value, 256);
    }

    public static byte[] signExtend(final BigInteger value, final int bytePos) {
        final int bitPos = (bytePos + 1) * 8 - 1;
        // Create bit mask for lower 'bits' number of bits
        final BigInteger mask = toUnsigned256Int(BigInteger.ONE.shiftLeft(bitPos).subtract(BigInteger.ONE));
        final BigInteger signBit = toUnsigned256Int(value).shiftRight(bitPos).and(BigInteger.ONE);
        return asUnsignedByteArray(toUnsigned256Int(signBit.equals(BigInteger.ONE) ? value.or(mask.not()) : value.and(mask)));
    }

    /**
     * Return the passed in value as an unsigned byte array.
     *
     * @param value the value to be converted.
     * @return a byte array without a leading zero byte if present in the signed encoding.
     */
    public static byte[] asUnsignedByteArray(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0 && bytes.length != 1) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            return tmp;
        }
        return bytes;
    }

    /**
     * Convert an integer to a byte array.
     * The byte array is in big-endian order.
     * @param value
     * @return
     */
    public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}

}
