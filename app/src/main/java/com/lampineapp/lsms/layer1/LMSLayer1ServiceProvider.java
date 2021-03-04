package com.lampineapp.lsms.layer1;

import android.util.Log;

import java.util.Arrays;

import static com.lampineapp.helper.Helper.sleep_ms;

public class LMSLayer1ServiceProvider implements LMSLayer1SAP {
    private final static String TAG = LMSLayer1ServiceProvider.class.getSimpleName();

    private ReceiveListener mResponseListener;
    private final LMSLayer1HardwareInterface mHwInterface;

    public LMSLayer1ServiceProvider(LMSLayer1HardwareInterface hwInterface) {
        mHwInterface = hwInterface;
    }

    @Override
    public void transmit(byte[] data) {
          mHwInterface.transmit(data);
    }

    @Override
    public void receive(byte[] data) {
        if (mResponseListener != null) {
            mResponseListener.onReceive(data);
        }
    }

    @Override
    public void setOnReceiveListener(ReceiveListener listener) {
        mResponseListener = listener;
    }
}