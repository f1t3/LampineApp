package com.lampineapp.lsms.layer2;

public interface LLayer2SAP {
    void requestTransmit(byte[] data);
    void responseReceive(byte[] resp);
    void setOnResponseListener(ResponseListener listener);
    interface ResponseListener {
        void onResponse(byte[] resp);
    }
}

