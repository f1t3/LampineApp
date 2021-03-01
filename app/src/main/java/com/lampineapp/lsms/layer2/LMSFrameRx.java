package com.lampineapp.lsms.layer2;

import java.util.Arrays;

public class LMSFrameRx extends LMSFrame {

    protected LMSFrameRx(byte[] frame) {
        super(extractData(frame), extractChecksum(frame));
    }

    static private byte[] extractData(byte[] frame) {
        return Arrays.copyOfRange(frame, NUM_SOF_BYTES + NUM_LEN_BYTES, frame.length - NUM_CHECKSUM_BYTES);
    }

    static private byte[] extractChecksum(byte[] frame) {
        return Arrays.copyOfRange(frame, frame.length - NUM_CHECKSUM_BYTES, frame.length);
    }
}
