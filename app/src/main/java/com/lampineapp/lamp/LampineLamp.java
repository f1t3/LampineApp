package com.lampineapp.lamp;

public interface LampineLamp {

    enum LampType {
        HEADLAMP
    }
    LampType getLampType();
    int getFlux_lm(float percentage);
    int getLedInputPower_mW(float relIntensity);
    int getLedCurrent_mA(float relIntensity);
}

