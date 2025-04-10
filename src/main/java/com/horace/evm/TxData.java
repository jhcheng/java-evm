package com.horace.evm;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TxData {

    private final byte[] from;
    private final byte[] to;
    private final byte[] value;
    private final byte[] origin;
    private final byte[] data;
    private final BigInteger gasPrice;

}
