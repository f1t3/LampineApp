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
        if (!mHwInterface.isConnected()) {
            Log.d(TAG, "HW not connected");
            return;
        }
        byte [] pack;
        int symbolsSend = 0;

        while (data.length > mHwInterface.getMaxTxSize()) {
            // TODO: Use ByteStream?
            pack = Arrays.copyOfRange(data, symbolsSend,  symbolsSend + mHwInterface.getMaxTxSize() - 1);
            mHwInterface.transmit(pack);
            symbolsSend += mHwInterface.getMaxTxSize();
            // Make sure receiver has time to read message. Ugly, so make sure byte[] len is < 20!
            sleep_ms(40);
        }
        // Transmit rest of chars
        pack = Arrays.copyOfRange(data, symbolsSend,  data.length);
        mHwInterface.transmit(pack);
    }

    @Override
    public void receive(byte[] data) {
        // Pass directly to layer 2
        if (mResponseListener != null) {
            mResponseListener.onReceive(data);
        }
    }

    @Override
    public void setOnReceiveListener(ReceiveListener listener) {
        mResponseListener = listener;
    }
}