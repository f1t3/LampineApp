package com.lampineapp.lsms.layer1;

public interface LLayer1HardwareInterface {
    void transmit(byte[] data);
    void setOnResponseListener(ResponseListener listener);
    boolean isConnected();
    int getMaxTxSize();
    interface ResponseListener {
        void onResponse(byte[] resp);
    }
}
