package com.lampineapp.lsms.layer1;

public interface LMSLayer1SAP {
    void transmit(byte[] data);
    void receive(byte[] data);
    void setOnReceiveListener(ReceiveListener listener);
    interface ReceiveListener {
        void onReceive(byte[] data);
    }
}
