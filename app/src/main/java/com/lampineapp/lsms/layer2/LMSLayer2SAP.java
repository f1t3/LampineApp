package com.lampineapp.lsms.layer2;

public interface LMSLayer2SAP {
    void transmit(byte[] data);
    void receive(byte[] data);
    void setOnReceiveListener(ReceiveListener listener);
    interface ReceiveListener {
        void onReceive(byte[] data);
    }
}

