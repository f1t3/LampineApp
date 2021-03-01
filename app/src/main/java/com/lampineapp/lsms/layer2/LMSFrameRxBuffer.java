package com.lampineapp.lsms.layer2;

public interface LMSFrameRxBuffer {
    void put(byte[] str);
    void onDATFrameComplete(LMSFrame frame);
    void onACKFrameComplete();
    void onNACKFrameComplete();
}
