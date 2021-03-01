package com.lampineapp.lsms.layer3;

import android.util.Log;

import com.lampineapp.lsms.layer2.LMSLayer2SAP;

import java.util.LinkedList;
import java.util.Queue;

public class LMSLayer3ServiceProvider implements LMSLayer3SAP {
    private final static String TAG = LMSLayer3ServiceProvider.class.getSimpleName();

    private Queue<LMSMessage> mTxQueue = new LinkedList<>();

    private enum ServiceState {READY, WAITING_FOR_ACK};
    private ServiceState mServiceState = ServiceState.READY;

    private LMSLayer2SAP mLayer2Sap;
    private ResponseListener mRespListener;

    public LMSLayer3ServiceProvider(LMSLayer2SAP layer2Sap) {
        mLayer2Sap = layer2Sap;
    }

    private final LMSMessageRxBuffer mRxBuf = new LMSMessageAssembler() {
        @Override
        public void onDATMessageComplete(LMSMessage message) {
            switch (mServiceState) {
                case READY:
                    if (message.isValid()) {
                        if (message.getType().equals(LMSMessage.MessageType.TYPE_LONG)) {
                            sendAck();
                        }
                        if (mRespListener != null) {
                            mRespListener.onResponse(message.getDataBytes());
                        } else {
                            Log.e(TAG, "No ResponseListener set");
                        }
                    } else {
                        if (message.getType().equals(LMSMessage.MessageType.TYPE_LONG)) {
                            sendAck();
                        }
                    }
                    break;
                case WAITING_FOR_ACK:
                    Log.e(TAG, "Received unexpected DAT message");
                    break;
            }
        }

        @Override
        public void onACKMessageComplete() {
            switch (mServiceState) {
                case READY:
                    Log.e(TAG, "Received unexpected ACK message");
                    break;
                case WAITING_FOR_ACK:
                    mTxQueue.poll();
                    mServiceState = ServiceState.READY;
                    break;
            }
        }

        @Override
        public void onNACKMessageComplete() {
            switch (mServiceState) {
                case READY:
                    Log.e(TAG, "Received unexpected NACK message");
                    break;
                case WAITING_FOR_ACK:
                    final LMSMessage message = mTxQueue.peek();
                    requestTransmit(message.getType(), message.toBytes());
                    mServiceState = mServiceState.WAITING_FOR_ACK;
                    break;
            }
        }
    };

    @Override
    public void requestTransmit(LMSMessage.MessageType type, byte[] data) {
        final LMSMessage message = new LMSMessageTx(type, data);
        mLayer2Sap.transmit(message.toBytes());
    }

    @Override
    public void responseReceive(byte[] resp) {
        mRxBuf.put(resp);
    }

    @Override
    public void setOnResponseListener(ResponseListener listener) {
        mRespListener = listener;
    }

    private void sendAck(){
        final LMSMessage message = new LMSMessageTx(LMSMessage.MessageType.TYPE_ACK);
        mLayer2Sap.transmit(message.toBytes());
    }

    private void sendNack() {
        final LMSMessage message = new LMSMessageTx(LMSMessage.MessageType.TYPE_NACK);
        mLayer2Sap.transmit(message.toBytes());
    }
}
