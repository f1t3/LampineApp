package com.lampineapp.helper;

public class GeneralHelpers {
    public static void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            // TODO: catch
        }
    }
}
