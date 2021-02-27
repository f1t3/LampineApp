package com.lampineapp.lsms.layer3;

import com.lampineapp.lsms.layer2.LampineFrame;

public interface LampineTxMessageBuffer {
    void putMessage(LampineFrame frame);
    LampineFrame getNextFrame();
    void clearNextFrame();
    void clearAll();
    int getCount();
}
