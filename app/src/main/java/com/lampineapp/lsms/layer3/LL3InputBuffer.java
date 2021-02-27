package com.lampineapp.lsms.layer3;

public interface LL3InputBuffer {
    void put(byte[] frame);
    void onDATMessageComplete(LampineMessage message);
    void onACKMessageComplete();
    void onNACKMessageComplete();
}
