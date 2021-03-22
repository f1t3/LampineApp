package com.lampineapp.lsms.layer3;

import java.math.BigInteger;
import java.util.Arrays;

public class LMSMessageRx extends LMSMessage {

    protected LMSMessageRx(byte[] data) {
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
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES_LONG) {
                    return 0;
                }
                final byte[] lenBytes = Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES, NUM_SOM_BYTES + NUM_TYPE_BYTES);
                final int len = new BigInteger(lenBytes).intValue();
                return len;
            case TYPE_SHORT:
                // TODO!!!
            default:
                return 0;
        }
    }

    static private byte[] extractData(byte[] data) {
        switch (extractType(data)) {
            case TYPE_SHORT:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES_SHORT + 1) {
                    return null;
                }
                return Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES, data.length);
            case TYPE_LONG:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES_LONG + 1 + NUM_CHECKSUM_BYTES ) {
                    return null;
                }
                return Arrays.copyOfRange(data, NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES_LONG, data.length - NUM_CHECKSUM_BYTES);
            default:
                return null;
        }
    }

    static private byte[] extractChecksum(byte[] data) {
        switch (extractType(data)) {
            case TYPE_SHORT:
                return new byte[]{};
            case TYPE_LONG:
                if (data.length < NUM_SOM_BYTES + NUM_TYPE_BYTES + NUM_LEN_BYTES_LONG + 1 + NUM_CHECKSUM_BYTES ) {
                    return null;
                }
                return Arrays.copyOfRange(data, data.length - NUM_CHECKSUM_BYTES, data.length );
            default:
                return null;
        }
    }
}
