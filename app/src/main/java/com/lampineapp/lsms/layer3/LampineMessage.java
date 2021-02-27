package com.lampineapp.lsms.layer3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.lampineapp.lsms.layer3.LampineMessage.MessageType.TYPE_LONG;

abstract public class LampineMessage {

    final static protected int NUM_SOM_BYTES      = 1;
    final static protected int NUM_EOM_BYTES      = 1;
    final static protected int NUM_TYPE_BYTES     = 1;
    final static protected int NUM_LEN_BYTES      = 4;
    final static protected int NUM_CHECKSUM_BYTES = 4;

    final static protected int POS_SOM_BYTE = 0;
    final static protected int POS_TYPE_BYTE = 1;

    final static protected byte SOM_BYTE = 0x04;
    final static protected byte EOM_BYTES    = 0x05;

    final static private byte TYPE_SHORT_BYTE = 0x01;
    final static private byte TYPE_LONG_BYTES  = 0x02;
    final static private byte TYPE_ACK_BYTES   = 0x03;
    final static private byte TYPE_NACK_BYTES  = 0x04;


    public enum MessageType {
        TYPE_SHORT, TYPE_LONG, TYPE_ACK, TYPE_NACK;

        static protected byte[] getArray() {
            return new byte[] {TYPE_SHORT_BYTE, TYPE_LONG_BYTES, TYPE_ACK_BYTES, TYPE_NACK_BYTES};
        }

        static protected byte toByte(MessageType type) {
            switch (type) {
                case TYPE_SHORT: return TYPE_SHORT_BYTE;
                case TYPE_LONG:  return TYPE_LONG_BYTES;
                case TYPE_ACK:   return TYPE_ACK_BYTES;
                case TYPE_NACK:  return TYPE_NACK_BYTES;
                default: return 0x00;
            }
        }
    };

    private MessageType mMessageType;
    private byte[] mData;
    private byte[] mChecksum;
    private int mLen;

    protected LampineMessage(MessageType type, byte[] data) {
        mData = data;
        mMessageType = type;
        switch (type) {
            case TYPE_LONG:
                mLen = mData.length;
                mChecksum = calcChecksum(mData);
                break;
            case TYPE_SHORT:
            default:
                break;
        }
    }

    protected LampineMessage(MessageType type, int len, byte[] data, byte[] checksum) {
        if (type == null) {
            // TODO: What to do?
        }
        mMessageType = type;
        mData = data;
        switch (type) {
            case TYPE_SHORT:
                break;
            case TYPE_LONG:
                mChecksum = checksum;
                mLen = len;
        }
    }

    protected byte[] toBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            stream.write(SOM_BYTE);
            switch (mMessageType) {
                case TYPE_SHORT:
                    stream.write(TYPE_SHORT_BYTE);
                    stream.write(mData);
                    break;
                case TYPE_LONG:
                    stream.write(TYPE_LONG_BYTES);
                    final byte[] lenArray = ByteBuffer.allocate(4).putInt(mLen).array();
                    stream.write(lenArray);
                    stream.write(mData);
                    stream.write(mChecksum);
                    break;
            }
            stream.write(EOM_BYTES);
        } catch (IOException e) {};
        return stream.toByteArray();
    }

    protected byte[] getDataBytes() {
        return mData;
    }

    protected MessageType getType() {
        return mMessageType;
    }

    protected boolean isValid() {
        if (!isMessageLenValid()) {
            return false;
        }
        switch (mMessageType) {
            case TYPE_LONG:
                if (hasMessageValidChecksum()) {
                    return true;
                }
                break;
            case TYPE_SHORT:
                return true;
        }
        return false;
    }

    private boolean isMessageLenValid() {
        if (mData.length != mLen && mLen != 0)
            return false;
        else
            return true;
    }

    private boolean hasMessageValidChecksum() {
        if (!isMessageLenValid()) {
            return false;
        }
        if (calcChecksum(mData).equals(mChecksum)) {
            return true;
        } else {
            return false;
        }
    }

    private byte[] calcChecksum(byte[] data) {
        long sum1 = 0;
        long sum2 = 0;
        for (int i = 0; i < data.length; i++) {
            // Modulo 2^32-1
            sum1 = sum1 + sum2 + data[i];
            sum1 = (sum1 & 0xFFFFFFFF) + (sum1 >> 32);
            sum2 = sum2 + sum1;
            sum2 = (sum2 % 0xFFFFFFFF) + (sum2 >> 32);
        }
        // Modulo 2^16-1
        sum1 = (sum1 & 0xFFFF) + (sum1 >> 16);
        sum2 = (sum2 & 0xFFFF) + (sum2 >> 16);
        final byte[] sum = {(byte)(sum1 >> 8), (byte)(sum1 & 0xFF) , (byte)(sum2 >> 8), (byte)(sum2 & 0xFF)};
        return sum;
    }
}
