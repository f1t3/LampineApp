package com.lampineapp.graphics.colorgraphinputview;

import java.util.ArrayList;

public class ColorTimeSeries {

    /***********************************************************************************************
     *
     * Members
     */
    private ArrayList<Integer> mColorValues = new ArrayList<>();
    private ArrayList<Float> mRelativeTimestamp = new ArrayList<>();
    private int mPeriod_ms = 0;

    /***********************************************************************************************
     *
     * Public Methods
     */
    public int size() { return mColorValues.size(); }

    public String getCurveJson() {
        String ret = "nPoints=" + mColorValues.size() + ";";
        ret += "rVals"

    }

    /***********************************************************************************************
     *
     * Protected Methods
     */
    protected int getLastColorVal() {
        if (mColorValues.isEmpty())
            return 0;
        else
            return mColorValues.get(mColorValues.size() - 1 );
    }

    protected boolean addPair(int color, float time_ms) {
        if (mRelativeTimestamp.size() == 0) {
            mColorValues.add(color);
            mRelativeTimestamp.add(time_ms);
            return true;
        }
        if (mRelativeTimestamp.get(mRelativeTimestamp.size() - 1) < time_ms) {
            mColorValues.add(color);
            mRelativeTimestamp.add(time_ms);
            return true;
        }
        // Time not advancing
        return false;
    }

    protected void setPeriod_ms(int period) {
        mPeriod_ms = period;
    }
}
