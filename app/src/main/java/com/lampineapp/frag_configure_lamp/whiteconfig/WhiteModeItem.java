package com.lampineapp.frag_configure_lamp.whiteconfig;

public class WhiteModeItem {
    private String mName;
    private int mCurrent;
    private int mPercentage;
    private int mPeriodPerPoint_ms;
    private int mNRgbwPoints;
    private int[] mRPoints, mGPoints, mBPoints, mWPoints;

    public WhiteModeItem(String str, int percentage) {
        mName = str;
        mPercentage = percentage;
        mCurrent = (int)(percentage*1.56);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getPercentage () {
        return mPercentage;
    }

    public int getCurrent() {
        return mCurrent;
    }

    public int getPeriodPerPoint_ms() {
        return mPeriodPerPoint_ms;
    }

    public void setPeriodPerPoint_ms(int period) {
        mPeriodPerPoint_ms = period;
    }

    public void setNRgbwPoints(int nPoints) {
        mNRgbwPoints = nPoints;
    }

    public void setRPoints(int[] rPoints) {
        mRPoints = rPoints;
    }

    public void setGPoints(int[] gPoints) {
        mGPoints = gPoints;
    }

    public void setBPoints(int[] bPoints) {
        mBPoints = bPoints;
    }

    public void setWPoints(int[] wPoints) {
        mWPoints = wPoints;
    }
}
