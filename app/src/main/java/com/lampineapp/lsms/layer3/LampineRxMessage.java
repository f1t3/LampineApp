package com.lampineapp.lsms.layer3;

import java.math.BigInteger;
import java.util.Arrays;

public class LampineRxMessage extends LampineMessage {

    protected LampineRxMessage(byte[] data) {
        super(extractType(data), extractLen(data), extractData(data), extractChecksum(data));
    }

    static private MessageType extractType(byte[] data) {
        if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES) {
            return null;
        }
        final byte typeByte = data[POS_TYPE_BYTE];
        // TODO: Move to enum
        if (typeByte == MessageType.toByte(MessageType.TYPE_SHORT)) {
            return MessageType.TYPE_SHORT;
        }
        if (typeByte == MessageType.toByte(MessageType.TYPE_LONG)) {
            return MessageType.TYPE_LONG;
        }
        return null;
    }

    static private int extractLen(byte[] data) {
        switch (extractType(data)) {
            case TYPE_LONG:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES) {
                    return 0;
                }
                final byte[] lenBytes = Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES, NUM_SOM_BYTES + NUM_TYPE_BYTES);
                final int len = new BigInteger(lenBytes).intValue();
                return len;
            case TYPE_SHORT:
            default:
                return 0;
        }
    }

    static private byte[] extractData(byte[] data) {
        switch (extractType(data)) {
            case TYPE_SHORT:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + 1 + NUM_EOM_BYTES) {
                    return null;
                }
                return Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES, data.length - NUM_EOM_BYTES);
            case TYPE_LONG:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES + 1 + NUM_CHECKSUM_BYTES + NUM_EOM_BYTES) {
                    return null;
                }
                return Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES, data.length - NUM_EOM_BYTES - NUM_CHECKSUM_BYTES);
            default:
                return null;
        }
    }

    static private byte[] extractChecksum(byte[] data) {
        switch (extractType(data)) {
            case TYPE_SHORT:
                return new byte[]{};
            case TYPE_LONG:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES + 1 + NUM_CHECKSUM_BYTES + NUM_EOM_BYTES) {
                    return null;
                }
                return Arrays.copyOfRange(data, data.length - NUM_EOM_BYTES - NUM_CHECKSUM_BYTES, data.length - NUM_EOM_BYTES);
            default:
                return null;
        }
    }
}
