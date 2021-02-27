package com.lampineapp.helper;

import android.util.Log;

import com.lampineapp.ActivityLampConnected;

public class Helper {
    private final static String TAG = ActivityLampConnected.class.getSimpleName();

    public static void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            Log.e(TAG, "Exception in sleep_ms()");
        }
    }
}
