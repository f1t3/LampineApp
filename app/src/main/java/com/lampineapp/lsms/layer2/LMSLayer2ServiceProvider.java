package com.lampineapp.lsms.layer2;

import android.util.Log;

import com.lampineapp.lsms.layer1.LMSLayer1SAP;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class LMSLayer2ServiceProvider implements LMSLayer2SAP {
    private final static String TAG = LMSLayer2ServiceProvider.class.getSimpleName();

    private enum ServiceState {READY, WAITING_FOR_ACK};
    ServiceState mServiceState = ServiceState.READY;

    private Queue<LMSFrame> mTxBuf = new LinkedList<>();

    private ReceiveListener mRespListener;

    private LMSLayer1SAP mLayer1SAP;

    public LMSLayer2ServiceProvider(LMSLayer1SAP layer1SAP) {
        mLayer1SAP = layer1SAP;
    }

    // TODO: Implement timer to resend frames if no ACK is received
    private LMSFrameRxBuffer mRxBuf = new LMSFrameAssembler() {
        @Override
        public void onDATFrameComplete(LMSFrame frame) {
            switch (mServiceState) {
                case READY:
                    if (frame.isValid()) {
                        sendACK();
                        if (mRespListener != null) {
                            mRespListener.onReceive(frame.getDataBytes());
                        } else {
                            Log.e(TAG, "No ResponseListener set");
                        }
                    } else {
                        sendNACK();
                    }
                    break;

                case WAITING_FOR_ACK:
                    Log.e(TAG, "Received unexpected DAT frame");
                    break;
            }
        }

        @Override
        public void onACKFrameComplete() {
            switch (mServiceState) {
                case WAITING_FOR_ACK:
                    if (mTxBuf.size() == 1) {
                        mTxBuf.poll();
                        mServiceState = mServiceState.READY;
                    } else {
                        mTxBuf.poll();
                        transmit(mTxBuf.peek().getDataBytes());
                        mServiceState = mServiceState.WAITING_FOR_ACK;
                    }
                    break;
                case READY:
                    Log.e(TAG, "Received unexpected ACK");
                    break;
            }
        }

        @Override
        public void onNACKFrameComplete() {
            switch (mServiceState) {
                case WAITING_FOR_ACK:
                    transmit(mTxBuf.peek().getDataBytes());
                    mServiceState = mServiceState.WAITING_FOR_ACK;
                    break;
                case READY:
                    Log.e(TAG, "Received unexpected NACK");
                    break;
            }
        }
    };

    @Override
    public void transmit(byte[] data) {
        byte[] frameBytes;
        int byteCntr = 0;
        while (data.length > LMSFrame.MAX_NUM_DATA_BYTES) {
            frameBytes = Arrays.copyOfRange(data, byteCntr, byteCntr + LMSFrame.MAX_NUM_DATA_BYTES);
            mTxBuf.add(new LMSFrameTx(frameBytes));
            byteCntr += LMSFrame.MAX_NUM_DATA_BYTES;
        }

        frameBytes = Arrays.copyOfRange(data, byteCntr, data.length);
        mTxBuf.add(new LMSFrameTx(frameBytes));
        // Trigger first transmission if no transmission is ongoing
        if (mServiceState == ServiceState.READY) {
            mLayer1SAP.transmit(mTxBuf.peek().toBytes());
            mServiceState = ServiceState.WAITING_FOR_ACK;
        }
    }

    @Override
    public void receive(byte[] data) {
        mRxBuf.put(data);
    }

    @Override
    public void setOnReceiveListener(ReceiveListener listener) {
        mRespListener = listener;
    }

    private void sendACK() {
        final byte[] ACK = {LMSFrame.ACK};
        mLayer1SAP.transmit(ACK);
    }


    private void sendNACK() {
        final byte[] NACK = {LMSFrame.NACK};
        mLayer1SAP.transmit(NACK);
    }
}
