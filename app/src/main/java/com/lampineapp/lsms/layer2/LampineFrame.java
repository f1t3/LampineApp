package com.lampineapp.lsms.layer2;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class LampineFrame {
    private final static String TAG = LampineFrame.class.getSimpleName();

    protected static final int NUM_CHECKSUM_BYTES = 2;
    protected static final int NUM_EOT_BYTES = 1;
    protected static final int MAX_NUM_DATA_BYTES = 16;

    protected static final int NUM_SOF_BYTES = 1;
    protected static final int NUM_EOF_BYTES = 1;

    // Use ASCII control bytes
    protected static final char SOF  = 0x02;
    protected static final char EOF  = 0x03;
    protected static final byte ACK  = 0x06;
    protected static final byte NACK = 0x15;

    // TODO: Store as byte[]
    private byte[] mData;
    private byte[] mChecksum;

    protected LampineFrame(byte[] data) {
        mData = data;
        mChecksum = calcChecksum(data);
    }

    protected LampineFrame(byte[] data, byte[] checksum) {
        mData = data;
        mChecksum = checksum;
    }

    protected boolean isValid() {
        if (!isFrameLenValid()) {
            return false;
        }
        if (hasFrameValidChecksum()) {
            return true;
        }
        return false;
    }

    protected byte[] toBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final byte[] sof = {SOF};
        final byte[] eof = {EOF};
        try {
            stream.write(sof);
            stream.write(mData);
            stream.write(mChecksum);
            stream.write(eof);
        } catch (IOException e) {};
        return stream.toByteArray();
    }

    protected byte[] getDataBytes() { return mData; }

    private boolean isFrameLenValid() {
        // Has string at least 1 data byte?
        if (mData.length < 1 + NUM_EOT_BYTES + NUM_CHECKSUM_BYTES)
            return false;
        else
            return true;
    }

    private boolean hasFrameValidChecksum() {
        if (!isFrameLenValid()) {
            return false;
        }
        final byte[] sumCalc = calcChecksum(mData);
        if (Arrays.equals(mChecksum, sumCalc)) {
            return true;
        } else {
            return false;
        }
    }

    private byte[] calcChecksum(byte[] data) {
        int sum1 = 0;
        int sum2 = 0;
        for (int i = 0; i < data.length; i++) {
            sum1 = sum1 + sum2 + (int)data[i];
            // Modulo 65535
            sum1 = (sum1 & 0xFFFF) + (sum1 >> 16);
            sum2 = sum2 + sum1;
            // Modulo 65535
            sum2 = (sum2 & 0xFFFF) + (sum2 >> 16);
        }
        // Modulo 255
        sum1 = (sum1 & 0xFF) + (sum1 >> 8);
        sum2 = (sum2 & 0xFF) + (sum2 >> 8);
        final byte[] sum = {(byte)sum1, (byte)sum2};
        Log.d(TAG, "Checksum of \"" + new String(data, StandardCharsets.US_ASCII) + "\" is: " + new String(sum, StandardCharsets.US_ASCII));
        return sum;
    }
}
