package com.lampineapp.frag_configure_lamp.whiteconfig;

import java.io.ByteArrayOutputStream;

public class WhiteLampMode {

    private float mIntensity;

    public WhiteLampMode (float intensity) {
        mIntensity = intensity;
    }

    protected float getIntensity() { return mIntensity; }


    protected int getId() {
        return (int)(255*mIntensity);
    }
}
