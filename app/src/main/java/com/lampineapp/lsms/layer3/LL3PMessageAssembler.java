package com.lampineapp.lsms.layer3;

import android.util.Log;

import java.nio.charset.StandardCharsets;

abstract public class LL3PMessageAssembler implements LL3InputBuffer {
    private final static String TAG = LL3PMessageAssembler.class.getSimpleName();

    private String mMessageStr;

    @Override
    public void put(byte[] frame) {
        if (beginsWithValidSomAndTypeBytes(frame) && (endsWithEom(frame))) {
            final LampineMessage message = new LampineRxMessage(frame);
            switch (message.getType()) {
                case TYPE_ACK:
                    onACKMessageComplete();
                    break;
                case TYPE_NACK:
                    onNACKMessageComplete();
                    break;
                case TYPE_SHORT:
                case TYPE_LONG:
                    onDATMessageComplete(message);
                    break;
                default:
                    Log.e(TAG, "Unknown message type received");
            }
        } else if (beginsWithValidSomAndTypeBytes(frame)) {
            // Assume new message
            // TODO: Implement
        } else if (endsWithEom(frame)) {
            // Assume end of message
            // TODO: Implement
            final LampineMessage message = new LampineRxMessage(frame);
            switch (message.getType()) {
                case TYPE_ACK:
                    onACKMessageComplete();
                    break;
                case TYPE_NACK:
                    onNACKMessageComplete();
                    break;
                case TYPE_SHORT:
                case TYPE_LONG:
                    onDATMessageComplete(message);
                    break;
                default:
                    Log.e(TAG, "Unknown message type received");
            }
        } else {
            // Assume data frame
            // TODO: Implement
        }
    }

    @Override
    abstract public void onDATMessageComplete(LampineMessage message);

    @Override
    abstract public void onACKMessageComplete();

    @Override
    abstract public void onNACKMessageComplete();

    private boolean beginsWithValidSomAndTypeBytes(byte[] data) {
        if (data[0] != LampineMessage.SOM_BYTE) {
            return false;
        }
        final byte[] typeArray = LampineMessage.MessageType.getArray();
        for (int typeIndex = 0; typeIndex < typeArray.length; typeIndex++) {
            if (data[LampineMessage.POS_SOM_BYTE] == typeArray[typeIndex]) {
                return true;
            }
        }
        return false;
    }

    private boolean endsWithEom(byte[] frame) {
        if (frame[frame.length - 1] == LampineMessage.EOM_BYTES) {
                return true;
        }
        return false;
    }



}
