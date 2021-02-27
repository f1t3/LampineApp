package com.lampineapp.lsms.layer2;

public interface LampineTxFrameBuffer {
    void putFrame(LampineFrame frame);
    LampineFrame getNextFrame();
    void clearNextFrame();
    void clearAll();
    int getCount();
}
