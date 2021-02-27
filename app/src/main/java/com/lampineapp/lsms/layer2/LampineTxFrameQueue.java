package com.lampineapp.lsms.layer2;

import java.util.LinkedList;
import java.util.Queue;

public class LampineTxFrameQueue implements LampineTxFrameBuffer {
    private static Queue<LampineFrame> mQueue = new LinkedList<>();

    @Override
    public void putFrame(LampineFrame frame) {
        mQueue.add(frame);
    }

    @Override
    public LampineFrame getNextFrame() {
        return mQueue.peek();
    }

    @Override
    public void clearNextFrame() {
        mQueue.remove();
    }

    @Override
    public void clearAll() {
        mQueue.clear();
    }

    @Override
    public int getCount() { return mQueue.size(); }

}
