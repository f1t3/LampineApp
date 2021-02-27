package com.lampineapp.lsms.layer3;

public interface LLayer3SAP {
    void requestTransmit(LampineMessage.MessageType type, byte[] data);
    void responseReceive(byte[] resp);
    void setOnResponseListener(ResponseListener listener);
    interface ResponseListener {
        void onResponse(byte[] resp);
    }
}
