package com.horace.evm;

import java.io.IOException;
import java.util.HexFormat;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class Storage {

    private final HTreeMap<byte[], Slot> addrMap = GlobalState.getDB().hashMap("addressMap")
        .keySerializer(Serializer.BYTE_ARRAY)
        .valueSerializer(new SlotSerializer())
        .createOrOpen();

    public byte[] get(final byte[] address, final byte[] key) {
        final Slot slot = addrMap.computeIfAbsent(key, k -> new Slot(address));
        return slot.slotMap.getOrDefault(key, new byte[] {(byte)0x00});
    }

    public void put(final byte[] address, final byte[] key, final byte[] value) {
        final Slot slot = addrMap.computeIfAbsent(key, k -> new Slot(address));
        slot.slotMap.put(key, value);
    }

    private class Slot {

        private final HTreeMap<byte[], byte[]> slotMap;

        private final byte[] address;

        public Slot(final byte[] address) {
            this.address = address;
            slotMap = db.hashMap("slotMap-" + HexFormat.of().formatHex(address))
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        }

    }

    private class SlotSerializer implements Serializer<Slot> {

        @Override
        public void serialize(final DataOutput2 out, final Slot value) throws IOException {
            // Implement serialization logic here
            out.write(value.address);
        }

        @Override
        public Slot deserialize(DataInput2 in, int available) throws IOException {
            // Implement deserialization logic here
            byte[] address = new byte[available];
            in.readFully(address);
            return new Slot(address);
        }

        @Override
        public int fixedSize() {
            return Address.ADDRESS_LENGTH; // Return the fixed size of the serialized object, if applicable
        }
    }
}
