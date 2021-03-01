package com.lampineapp.lsms.layer1;

public interface LMSLayer1HardwareInterface {
    void transmit(byte[] data);
    void setOnResponseListener(ReceiveListener listener);
    boolean isConnected();
    int getMaxTxSize();
    interface ReceiveListener {
        void onReceive(byte[] data);
    }
}
