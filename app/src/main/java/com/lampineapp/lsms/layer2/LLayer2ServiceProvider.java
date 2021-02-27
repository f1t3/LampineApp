package com.lampineapp.lsms.layer2;

import android.util.Log;

import com.lampineapp.lsms.layer1.LLayer1SAP;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LLayer2ServiceProvider implements LLayer2SAP {
    private final static String TAG = LLayer2ServiceProvider.class.getSimpleName();

    private enum ServiceState {READY, WAITING_FOR_ACK};
    ServiceState mServiceState = ServiceState.READY;

    private LampineTxFrameBuffer mTxBuf = new LampineTxFrameQueue();

    private ResponseListener mRespListener;

    private LLayer1SAP mLayer1SAP;

    public LLayer2ServiceProvider(LLayer1SAP layer1SAP) {
        mLayer1SAP = layer1SAP;
    }

    // TODO: Implement timer to resend frames if no ACK is received
    private LampineLayer2InputBuffer mRxBuf = new LampineLayer2FrameAssembler() {
        @Override
        public void onDATFrameComplete(LampineFrame frame) {
            switch (mServiceState) {
                case READY:
                    if (frame.isValid()) {
                        sendACK();
                        if (mRespListener != null) {
                            mRespListener.onResponse(frame.getDataBytes());
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
                    if (mTxBuf.getCount() == 1) {
                        mTxBuf.clearNextFrame();
                        mServiceState = mServiceState.READY;
                    } else {
                        mTxBuf.clearNextFrame();
                        requestTransmit(mTxBuf.getNextFrame().getDataBytes());
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
                    requestTransmit(mTxBuf.getNextFrame().getDataBytes());
                    mServiceState = mServiceState.WAITING_FOR_ACK;
                    break;
                case READY:
                    Log.e(TAG, "Received unexpected NACK");
                    break;
            }
        }
    };

    @Override
    public void requestTransmit(byte[] bytes) {
        Log.d(TAG, "Sending: " + new String(bytes, StandardCharsets.US_ASCII));

        byte[] frameBytes;
        int byteCntr = 0;
        while (bytes.length > LampineFrame.MAX_NUM_DATA_BYTES) {
            frameBytes = Arrays.copyOfRange(bytes, byteCntr, byteCntr + LampineFrame.MAX_NUM_DATA_BYTES);
            mTxBuf.putFrame(new LampineTxFrame(frameBytes));
            byteCntr += LampineFrame.MAX_NUM_DATA_BYTES;
        }

        frameBytes = Arrays.copyOfRange(bytes, byteCntr, bytes.length);
        mTxBuf.putFrame(new LampineTxFrame(frameBytes));
        // Trigger first transmission if no transmission is ongoing
        if (mServiceState == ServiceState.READY) {
            mLayer1SAP.requestTransmit(mTxBuf.getNextFrame().toBytes());
            mServiceState = ServiceState.WAITING_FOR_ACK;
        }
    }

    @Override
    public void responseReceive(byte[] resp) {
        Log.d(TAG, "Received: \"" + new String(resp, StandardCharsets.US_ASCII) + "\"");
        mRxBuf.put(resp);
    }

    @Override
    public void setOnResponseListener(ResponseListener listener) {
        mRespListener = listener;
    }

    private void sendACK() {
        Log.d(TAG, "Sending ACK");
        final byte[] ACK = {LampineFrame.ACK};
        mLayer1SAP.requestTransmit(ACK);
    }


    private void sendNACK() {
        Log.d(TAG, "Sending NACK");
        final byte[] NACK = {LampineFrame.NACK};
        mLayer1SAP.requestTransmit(NACK);
    }
}
