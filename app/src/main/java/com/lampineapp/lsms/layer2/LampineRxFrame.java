package com.lampineapp.lsms.layer2;

import java.util.Arrays;

public class LampineRxFrame extends LampineFrame {

    protected LampineRxFrame(byte[] frame) {
        super(extractData(frame), extractChecksum(frame));
    }

    static private byte[] extractData(byte[] frame) {
        return Arrays.copyOfRange(frame, NUM_SOF_BYTES, frame.length - NUM_CHECKSUM_BYTES - NUM_EOF_BYTES);
    }

    static private byte[] extractChecksum(byte[] frame) {
        return Arrays.copyOfRange(frame, frame.length - NUM_CHECKSUM_BYTES - NUM_EOF_BYTES, frame.length - NUM_EOF_BYTES);
    }
}
