package com.lampineapp.lsms;

import com.lampineapp.lsms.layer1.LMSLayer1HardwareInterface;
import com.lampineapp.lsms.layer1.LMSLayer1SAP;
import com.lampineapp.lsms.layer1.LMSLayer1ServiceProvider;
import com.lampineapp.lsms.layer2.LMSLayer2SAP;
import com.lampineapp.lsms.layer2.LMSLayer2ServiceProvider;
import com.lampineapp.lsms.layer3.LMSLayer3SAP;
import com.lampineapp.lsms.layer3.LMSLayer3ServiceProvider;
import com.lampineapp.lsms.layer3.LMSMessage;

import java.nio.charset.StandardCharsets;

public class LSMStack {
    private final static String TAG = LSMStack.class.getSimpleName();

    private LMSLayer1HardwareInterface mHwInterface;
    private LMSLayer1SAP mLayer1Sap;
    private LMSLayer2SAP mLayer2Sap;
    private LMSLayer3SAP mLayer3Sap;

    public interface ReceiveListener {
        void onReceive(byte[] data);
    }

    private ReceiveListener mReceiveListener;

    public LSMStack(LMSLayer1HardwareInterface hwInterface) {
        mHwInterface = hwInterface;
        mLayer1Sap = new LMSLayer1ServiceProvider(mHwInterface);
        mLayer2Sap = new LMSLayer2ServiceProvider(mLayer1Sap);
        mLayer3Sap = new LMSLayer3ServiceProvider(mLayer2Sap);

        mHwInterface.setOnResponseListener(new LMSLayer1HardwareInterface.ReceiveListener() {
            @Override
            public void onReceive(byte[] data) {
                mLayer1Sap.receive(data);
            }
        });
        mLayer1Sap.setOnReceiveListener(new LMSLayer1SAP.ReceiveListener() {
            @Override
            public void onReceive(byte[] data) {
                mLayer2Sap.receive(data);
            }
        });
        mLayer2Sap.setOnReceiveListener(new LMSLayer2SAP.ReceiveListener() {
            @Override
            public void onReceive(byte[] data) {
                mLayer3Sap.responseReceive(data);
            }
        });
        mLayer3Sap.setOnResponseListener(new LMSLayer3SAP.ResponseListener() {
            @Override
            public void onResponse(byte[] resp) {
                if (mReceiveListener != null) {
                    mReceiveListener.onReceive(resp);
                }
            }
        });
    }

    public void send(byte[] data) {
        if (data != null) {
            mLayer3Sap.requestTransmit(LMSMessage.MessageType.TYPE_SHORT, data);
        }
    }

    public void send(String data) {
        final byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
        mLayer3Sap.requestTransmit(LMSMessage.MessageType.TYPE_SHORT, dataBytes);
    }

    public boolean isConnected() {
        return mHwInterface.isConnected();
    }

    public void setOnReceiveListener(ReceiveListener listener) {
        mReceiveListener = listener;
    }

    public long getReliableInterval_ms() {
        return mLayer2Sap.getRoundtripTime_ms();
    }
}
