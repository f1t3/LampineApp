package com.lampineapp.lsms.layer2;

import android.os.Handler;
import android.util.Log;

import com.lampineapp.lsms.layer1.LMSLayer1SAP;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class LMSLayer2ServiceProvider implements LMSLayer2SAP {
    private final static String TAG = LMSLayer2ServiceProvider.class.getSimpleName();

    private enum ServiceState {READY, WAITING_FOR_ACK};
    ServiceState mServiceState = ServiceState.READY;
    private enum ServiceEvent {NONE, RECEIVED_ACK, RECEIVED_NACK};
    ServiceEvent mServiceEvent = ServiceEvent.NONE;

    private Queue<LMSFrame> mTxBuf = new LinkedList<>();

    private ReceiveListener mRespListener;

    private LMSLayer1SAP mLayer1SAP;

    private int mTxRetryCntr = 0;
    private int mWaitingForAckCntr_ms = 0;
    private final static int MAX_NUM_RETRYS = 1000000;
    private final static int WAITING_FOR_ACK_TIMEOUT_ms = 50;

    // Roundtrip cycle time measurement
    long mTimeSend_ms;
    long mTxRoundtripTime_ms = 100;
    private final static int TX_ROUND_TRIP_TIME_WEIGHT_IN_PERCENT = 10;

    public LMSLayer2ServiceProvider(LMSLayer1SAP layer1SAP) {

        mLayer1SAP = layer1SAP;
        mTxHandler.postDelayed(mTxRunnable, 100);
    }

    final Handler mTxHandler = new Handler();
    final Runnable mTxRunnable = new Runnable() {
        @Override
        public void run() {
            switch (mServiceState) {

                case WAITING_FOR_ACK:
                    switch (mServiceEvent) {
                        case RECEIVED_ACK:
                            updateTxRoundtripTime();
                            mServiceEvent = ServiceEvent.NONE;
                            mTxBuf.poll();
                            mServiceState = ServiceState.READY;
                            break;
                        case RECEIVED_NACK:
                            mServiceEvent = ServiceEvent.NONE;
                            mLayer1SAP.transmit(mTxBuf.peek().toBytes());
                            mServiceState = ServiceState.WAITING_FOR_ACK;
                            mTxRetryCntr++;
                            break;
                        case NONE:
                            mServiceEvent = ServiceEvent.NONE;
                            mWaitingForAckCntr_ms += 1;
                            if (mWaitingForAckCntr_ms >= WAITING_FOR_ACK_TIMEOUT_ms) {
                                mTxRetryCntr++;
                                mWaitingForAckCntr_ms = 0;
                                // Repeat transmission of last queue frame
                                mLayer1SAP.transmit(mTxBuf.peek().toBytes());
                                mServiceState = ServiceState.WAITING_FOR_ACK;
                            }
                            break;
                    }
                    break;

                case READY:
                    if (mTxBuf.size() > 0) {
                        mServiceState = ServiceState.WAITING_FOR_ACK;
                        mWaitingForAckCntr_ms = 0;
                        mTxRetryCntr = 0;
                        mLayer1SAP.transmit(mTxBuf.peek().toBytes());
                        mTimeSend_ms = System.currentTimeMillis();
                    }
                    break;
            }
            if (mTxRetryCntr >= MAX_NUM_RETRYS) {
                // Transmission failed
                mTxBuf.clear();
                mTxRetryCntr = 0;
                mWaitingForAckCntr_ms = 0;
                mServiceState = ServiceState.READY;
                Log.e(TAG, "Max num of retrys reached");
            }
            mTxHandler.postDelayed(mTxRunnable, 1);
        }
    };

    // TODO: Implement timer to resend frames if no ACK is received
    private LMSFrameRxBuffer mRxBuf = new LMSFrameAssembler() {
        @Override
        public void onDATFrameComplete(LMSFrame frame) {
            switch (mServiceState) {
                case READY:
                    if (frame == null) {
                        sendNACK();
                    } else if (frame.isValid()) {
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
                    sendNACK();
                    Log.e(TAG, "Received unexpected DAT frame");
                    break;
            }
        }

        @Override
        public void onACKFrameComplete() {
            mServiceEvent = ServiceEvent.RECEIVED_ACK;
            if (mServiceState == ServiceState.READY) {
                Log.e(TAG, "Received unexpected ACK");
            }
        }

        @Override
        public void onNACKFrameComplete() {
            mServiceEvent = ServiceEvent.RECEIVED_NACK;
            if (mServiceState == ServiceState.READY) {
                Log.e(TAG, "Received unexpected NACK");
            }
        }
    };

    @Override
    public void transmit(byte[] data) {
        byte[] frameBytes;
        int bytesSend = 0;
        int bytesLeft = data.length;
        while (bytesLeft > LMSFrame.MAX_NUM_DATA_BYTES) {
            frameBytes = Arrays.copyOfRange(data, bytesSend, bytesSend + LMSFrame.MAX_NUM_DATA_BYTES);
            mTxBuf.add(new LMSFrameTx(frameBytes));
            bytesSend += LMSFrame.MAX_NUM_DATA_BYTES;
            bytesLeft -= LMSFrame.MAX_NUM_DATA_BYTES;
        }
        frameBytes = Arrays.copyOfRange(data, bytesSend, bytesSend + bytesLeft);
        mTxBuf.add(new LMSFrameTx(frameBytes));
    }

    @Override
    public void receive(byte[] data) {
        mRxBuf.put(data);
    }

    @Override
    public void setOnReceiveListener(ReceiveListener listener) {
        mRespListener = listener;
    }

    @Override
    public long getRoundtripTime_ms() {
        return mTxRoundtripTime_ms;
    }

    private void sendACK() {

        final byte[] ACK = {LMSFrame.ACK};
        mLayer1SAP.transmit(ACK);

    }

    private void sendNACK() {
        final byte[] NACK = {LMSFrame.NACK};
        mLayer1SAP.transmit(NACK);
    }

    private void updateTxRoundtripTime() {
        mTxRoundtripTime_ms = (TX_ROUND_TRIP_TIME_WEIGHT_IN_PERCENT * (System.currentTimeMillis() - mTimeSend_ms) + (100 - TX_ROUND_TRIP_TIME_WEIGHT_IN_PERCENT) * mTxRoundtripTime_ms) / 100;
    }
}
