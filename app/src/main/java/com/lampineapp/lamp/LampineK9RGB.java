package com.lampineapp.lamp;

public class LampineK9RGB implements LampineHeadlamp {


    @Override
    public LampType getLampType() {
        return LampType.HEADLAMP;
    }

    @Override
    public int getFlux_lm(float percentage) {
        return (int)((percentage*10000) / 3);
    }

    @Override
    public int getLedInputPower_mW(float relIntensity) {
        return (int)(relIntensity * 3677);
    }

    @Override
    public int getLedCurrent_mA(float relIntensity) {
        return (int)(relIntensity * 2727);
    }

    @Override
    public int getBatLifetime_min(float relIntensity) {
        return (int)((1.1-relIntensity)*30);
    }

    @Override
    public int getMaxIntensityRuntime(float relIntensity) {
        if (relIntensity > 0.9) {
            return (int) ((1 - relIntensity) * 60);
        }
        return -1;
    }
}
