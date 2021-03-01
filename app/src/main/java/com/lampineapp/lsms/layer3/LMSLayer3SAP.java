package com.lampineapp.lsms.layer3;

public interface LMSLayer3SAP {
    void requestTransmit(LMSMessage.MessageType type, byte[] data);
    void responseReceive(byte[] resp);
    void setOnResponseListener(ResponseListener listener);
    interface ResponseListener {
        void onResponse(byte[] resp);
    }
}
