package com.lampineapp.frag_configure_lamp;

public class WhiteModeItem {
    private String mName;
    private String mCurrent;
    private int mPeriodPerPoint_ms;
    private int mNRgbwPoints;
    private int[] mRPoints, mGPoints, mBPoints, mWPoints;

    public WhiteModeItem() {
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCurrent() {
        return mCurrent;
    }

    public void setCurrent(String current) {
        mCurrent = current;
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
