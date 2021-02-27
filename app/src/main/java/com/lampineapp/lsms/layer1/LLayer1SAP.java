package com.lampineapp.lsms.layer1;

public interface LLayer1SAP {
    void requestTransmit(byte[] bytes);
    void responseReceive(byte[] resp);
    void setOnResponseListener(ResponseListener listener);
    interface ResponseListener {
        void onResponse(byte[] resp);
    }
}
