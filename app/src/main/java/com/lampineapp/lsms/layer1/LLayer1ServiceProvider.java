package com.lampineapp.lsms.layer1;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.lampineapp.helper.Helper.sleep_ms;

public class LLayer1ServiceProvider implements LLayer1SAP {
    private final static String TAG = LLayer1ServiceProvider.class.getSimpleName();

    private ResponseListener mResponseListener;

    private LLayer1HardwareInterface mHwInterface;

    public LLayer1ServiceProvider(LLayer1HardwareInterface hwInterface) {
        mHwInterface = hwInterface;
    }

    @Override
    public void requestTransmit(byte[] data) {
        Log.d(TAG, "Sending: " + new String(data, StandardCharsets.US_ASCII));

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
    public void responseReceive(byte[] resp) {
        // Pass directly to layer 2
        mResponseListener.onResponse(resp);
    }

    @Override
    public void setOnResponseListener(ResponseListener listener) {
        mResponseListener = listener;
    }
}