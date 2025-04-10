package com.horace.evm;

import java.math.BigInteger;

import lombok.Getter;

@Getter
public class Block {

    private BigInteger number;
    private byte[] coinbase;
    private BigInteger timestamp;
    private BigInteger difficulty;
    private byte[] prevRandao;
    private BigInteger gasLimit;

}
