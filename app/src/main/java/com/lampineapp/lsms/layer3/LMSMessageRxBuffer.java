package com.lampineapp.lsms.layer3;

public interface LMSMessageRxBuffer {
    void put(byte[] frame);
    void onDATMessageComplete(LMSMessage message);
    void onACKMessageComplete();
    void onNACKMessageComplete();
}
