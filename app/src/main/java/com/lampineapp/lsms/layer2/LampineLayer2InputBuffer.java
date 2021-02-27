package com.lampineapp.lsms.layer2;

public interface LampineLayer2InputBuffer {
    void put(byte[] str);
    void onDATFrameComplete(LampineFrame frame);
    void onACKFrameComplete();
    void onNACKFrameComplete();
}
