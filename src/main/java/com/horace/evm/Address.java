package com.horace.evm;

import java.util.HexFormat;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Address {

    public static final int ADDRESS_LENGTH = 20;

    private final byte[] address;
    private final String addressString;
    private final int hashCode;

    public Address(final byte[] address) {
        this.address = address;
        this.addressString = HexFormat.of().formatHex(address);
        final HashCodeBuilder hcb = new HashCodeBuilder(17, 37);
        hcb.append(address);
        hcb.append(addressString);
        this.hashCode = hcb.build();
    }

    public byte[] getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return this.addressString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address1 = (Address) obj;
        return addressString.equals(address1.addressString);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

}
