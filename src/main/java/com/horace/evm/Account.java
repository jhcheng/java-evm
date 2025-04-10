package com.horace.evm;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Account {

    private BigInteger balance;
    private byte[] code;

    public static final Account NULL_ACCOUNT = new Account(null, new byte[0]);

}
