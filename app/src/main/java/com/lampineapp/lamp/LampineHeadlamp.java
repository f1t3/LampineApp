package com.lampineapp.lamp;

public interface LampineHeadlamp extends LampineLamp {
    int getBatLifetime_min(float relIntensity);
    int getMaxIntensityRuntime(float relIntensity);
}

