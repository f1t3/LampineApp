package com.lampineapp.lsms;

import com.lampineapp.lsms.layer1.LLayer1HardwareInterface;
import com.lampineapp.lsms.layer1.LLayer1SAP;
import com.lampineapp.lsms.layer1.LLayer1ServiceProvider;
import com.lampineapp.lsms.layer2.LLayer2SAP;
import com.lampineapp.lsms.layer2.LLayer2ServiceProvider;
import com.lampineapp.lsms.layer3.LLayer3SAP;
import com.lampineapp.lsms.layer3.LLayer3ServiceProvider;
import com.lampineapp.lsms.layer3.LampineMessage;

import java.nio.charset.StandardCharsets;

public class LSMStack {
    private LLayer1HardwareInterface mHwInterface;
    private LLayer1SAP mLayer1Sap;
    private LLayer2SAP mLayer2Sap;
    private LLayer3SAP mLayer3Sap;

    public interface ReceiveListener {
        void onReceive(byte[] data);
    }

    private ReceiveListener mReceiveListener;

    public LSMStack(LLayer1HardwareInterface hwInterface) {
        mHwInterface = hwInterface;
        mLayer1Sap = new LLayer1ServiceProvider(mHwInterface);
        mLayer2Sap = new LLayer2ServiceProvider(mLayer1Sap);
        mLayer3Sap = new LLayer3ServiceProvider(mLayer2Sap);

        mHwInterface.setOnResponseListener(new LLayer1HardwareInterface.ResponseListener() {
            @Override
            public void onResponse(byte[] resp) {
                mLayer1Sap.responseReceive(resp);
            }
        });
        mLayer1Sap.setOnResponseListener(new LLayer1SAP.ResponseListener() {
            @Override
            public void onResponse(byte[] resp) {
                mLayer2Sap.responseReceive(resp);
            }
        });
        mLayer2Sap.setOnResponseListener(new LLayer2SAP.ResponseListener() {
            @Override
            public void onResponse(byte[] resp) {
                mLayer3Sap.responseReceive(resp);
            }
        });
        mLayer3Sap.setOnResponseListener(new LLayer3SAP.ResponseListener() {
            @Override
            public void onResponse(byte[] resp) {
                if (mReceiveListener != null) {
                    mReceiveListener.onReceive(resp);
                }
            }
        });
    }

    public void send(byte[] data) {
        mLayer3Sap.requestTransmit(LampineMessage.MessageType.TYPE_SHORT, data);
    }

    public void send(String data) {
        final byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
        mLayer3Sap.requestTransmit(LampineMessage.MessageType.TYPE_SHORT, dataBytes);
    }

    public void setOnReceiveListener(ReceiveListener listener) {
        mReceiveListener = listener;
    }
}
