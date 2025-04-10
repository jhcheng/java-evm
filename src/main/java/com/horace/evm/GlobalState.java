package com.horace.evm;

import java.io.IOException;
import java.math.BigInteger;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class GlobalState {


    private static GlobalState instance;

    private final DB db = DBMaker.memoryDB().make();
    private final HTreeMap<byte[], Account> accountMap = db.hashMap("accountMap")
        .keySerializer(Serializer.BYTE_ARRAY)
        .valueSerializer(new AccountSerializer())
        .createOrOpen();

    private GlobalState() {
    }

    public static GlobalState getInstance() {
        if (instance == null) {
            instance = new GlobalState();
        }
        return instance;
    }

    public static DB getDB() {
        return getInstance().db;
    }

    public Account getAccount(final byte[] address) {
        return accountMap.getOrDefault(address, Account.NULL_ACCOUNT);
    }

    public void putAccount(final byte[] address, final Account account) {
        accountMap.put(address, account);
    }

    public void removeAccount(final byte[] address) {
        accountMap.remove(address);
    }

    private class AccountSerializer implements Serializer<Account> {

        @Override
        public void serialize(final DataOutput2 out, final Account value) throws IOException {
            final byte[] balance = value.getBalance().toByteArray();
            out.writeInt(balance.length);
            out.write(balance);            
            out.writeInt(value.getCode().length);
            out.write(value.getCode());
        }

        @Override
        public Account deserialize(DataInput2 in, int available) throws IOException {
            byte[] balance = new byte[in.readInt()];
            in.readFully(balance);
            byte[] code = new byte[in.readInt()];
            in.readFully(code);
            return new Account(new BigInteger(balance), code);
        }
    }

}
