package com.lampineapp.lsms.layer3;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

abstract public class LMSMessageAssembler implements LMSMessageRxBuffer {
    private final static String TAG = LMSMessageAssembler.class.getSimpleName();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    private String mMessageStr;

    @Override
    public void put(byte[] frame) {
        // TODO IMPLEMENT!
//        if (beginsWithValidSomAndTypeBytes(frame)) {
//            stream.reset();
//            final LMSMessage message = new LMSMessageRx(frame);
//            switch (message.getType()) {
//                case TYPE_ACK:
//                    onACKMessageComplete();
//                    break;
//                case TYPE_NACK:
//                    onNACKMessageComplete();
//                    break;
//                case TYPE_SHORT:
//                case TYPE_LONG:
//                    onDATMessageComplete(message);
//                    break;
//                default:
//                    Log.e(TAG, "Unknown message type received");
//            }
//        } else if (beginsWithValidSomAndTypeBytes(frame)) {
//            // Assume new message
//            // TODO: Implement
//            stream.reset();
//            try {stream.write(frame);} catch (IOException e) {};
//        } else if (endsWithEom(frame)) {
//            // Assume end of message
//            // TODO: USE LEN!
//            try {stream.write(frame);} catch (IOException e) {};
//            byte[] msg = stream.toByteArray();
//            // TODO: Implement
//            final LMSMessage message = new LMSMessageRx(msg);
//            Log.d(TAG, "Received " + msg.length + " bytes");
//            switch (message.getType()) {
//                case TYPE_ACK:
//                    onACKMessageComplete();
//                    break;
//                case TYPE_NACK:
//                    onNACKMessageComplete();
//                    break;
//                case TYPE_SHORT:
//                case TYPE_LONG:
//                    onDATMessageComplete(message);
//                    break;
//                default:
//                    Log.e(TAG, "Unknown message type received");
//            }
//        } else {
//            // Assume data frame
//            // TODO: Implement
//            try {stream.write(frame);} catch (IOException e) {};
//        }
    }

    @Override
    abstract public void onDATMessageComplete(LMSMessage message);

    @Override
    abstract public void onACKMessageComplete();

    @Override
    abstract public void onNACKMessageComplete();

    private boolean beginsWithValidSomAndTypeBytes(byte[] data) {
        if (data[0] != LMSMessage.SOM_BYTE) {
            return false;
        }
        final byte[] typeArray = LMSMessage.MessageType.getArray();
        for (int typeIndex = 0; typeIndex < typeArray.length; typeIndex++) {
            if (data[LMSMessage.POS_SOM_BYTE] == typeArray[typeIndex]) {
                return true;
            }
        }
        return false;
    }
}
