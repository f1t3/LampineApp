package com.lampineapp.lsms.layer3;

import android.util.Log;

import com.lampineapp.lsms.layer2.LLayer2SAP;

import java.util.LinkedList;
import java.util.Queue;

public class LLayer3ServiceProvider implements LLayer3SAP{
    private final static String TAG = LLayer3ServiceProvider.class.getSimpleName();

    private Queue<LampineMessage> mTxQueue = new LinkedList<>();

    private enum ServiceState {READY, WAITING_FOR_ACK};
    private ServiceState mServiceState = ServiceState.READY;

    private LLayer2SAP mLayer2Sap;
    private ResponseListener mRespListener;

    public LLayer3ServiceProvider(LLayer2SAP layer2Sap) {
        mLayer2Sap = layer2Sap;
    }

    private final LL3InputBuffer mRxBuf = new LL3PMessageAssembler() {
        @Override
        public void onDATMessageComplete(LampineMessage message) {
            switch (mServiceState) {
                case READY:
                    if (message.isValid()) {
                        if (message.getType().equals(LampineMessage.MessageType.TYPE_LONG)) {
                            sendAck();
                        }
                        if (mRespListener != null) {
                            mRespListener.onResponse(message.getDataBytes());
                        } else {
                            Log.e(TAG, "No ResponseListener set");
                        }
                    } else {
                        if (message.getType().equals(LampineMessage.MessageType.TYPE_LONG)) {
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
                    final LampineMessage message = mTxQueue.peek();
                    requestTransmit(message.getType(), message.toBytes());
                    mServiceState = mServiceState.WAITING_FOR_ACK;
                    break;
            }
        }
    };

    @Override
    public void requestTransmit(LampineMessage.MessageType type, byte[] data) {
        final LampineMessage message = new LampineTxMessage(type, data);
        mLayer2Sap.requestTransmit(message.toBytes());
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
        final LampineMessage message = new LampineTxMessage(LampineMessage.MessageType.TYPE_ACK);
        mLayer2Sap.requestTransmit(message.toBytes());
    }

    private void sendNack() {
        final LampineMessage message = new LampineTxMessage(LampineMessage.MessageType.TYPE_NACK);
        mLayer2Sap.requestTransmit(message.toBytes());
    }
}
