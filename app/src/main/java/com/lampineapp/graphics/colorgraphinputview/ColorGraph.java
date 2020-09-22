package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Paint;
import android.graphics.Path;
import android.view.HapticFeedbackConstants;

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;

public class ColorGraph {

    // Parameter
    protected static Path mGraphPath = new Path();
    protected static float ADD_SEGMENT_THRESHOLD = 10;

    // Member
    protected static float X = 0, Y = 0;

    // TODO: CONTAINER FOR ARRAY LISTS COLOR_CURVE
    private static ArrayList<Integer> mColorVals = new ArrayList<>();
    private static ArrayList<Float> mRelativeTimestamp = new ArrayList<>();
    private static ColorGraphCompleteCallbackFunction mColorGraphCompleteCallbackFunction;

    public interface ColorGraphCompleteCallbackFunction {
        void onColorGraphComplete(ColorGraph graph);
    }

    public void setColorCurveCompleteCallbackFunction(ColorGraphCompleteCallbackFunction f) {
        mColorGraphCompleteCallbackFunction = f;
    }

    protected static boolean addSegment(float x, float y) {

        // Allow only forward movements in x direction
        // TODO: ADD THRESHOLD TO X?
        if (x <= X)
            return false;

        // Update graph if threshold exceeded
        if (!Helpers.isInsideRadius(x, y, X, Y, ADD_SEGMENT_THRESHOLD)) {
            // Update color on screen based on y position
            int color = InputFrame.getColorFromY(y);
            // Perform feedback on start of graph drawing
            if (getLastColorVal() == 0)
                .performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            // Store as new data point if color delta exceeds threshold
            if (DataHelpers.isColorDeltaGreaterThan(color, lastColorValue, 255/16)) {
                mColorGraph.addPair(color, InputFrame.getRelXPos(x));
            }
            // TODO: USE REAL COLOR AT POS?
            mGraphPaint.setColor(lastColorValue);
            mGraphPath.moveTo(mX, mY);
            mGraphPath.lineTo(x, y);

            // Stop indicator reached
            if (mStopInd.isOnCatchArea(x, y)) {
                mGraphPath.lineTo(mStopInd.X, mStopInd.Y);
                InputFrame.CANVAS.drawPath(mGraphPath, mGraphPaint);
                mState = ColorGraphInputView.State.S_GRAPH_COMPLETE;
                this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            // Path must be drawn directly for using different colors
            InputFrame.CANVAS.drawPath(mGraphPath, mGraphPaint);

            mGraphPath.reset();

            // Update cursor position
            mX = x;
            mY = y;
        }
    }

    mGraphPaint = new Paint();
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraphPaint.setStrokeWidth(mGraphLineWidth);
    mGraphPath = new Path();

//    public boolean setColorVals(ArrayList<Integer> colorVals) {
//        if (mRelativeTimestamp.isEmpty() || mRelativeTimestamp.size() == colorVals.size()) {
//            mColorVals = colorVals;
//            return true;
//        }
//        // Length mismatch
//        return false;
//    }
//
//    public boolean setRelativeTimestampVals(ArrayList<Float> timeVals_ms) {
//        if (mColorVals.isEmpty() || mColorVals.size() == timeVals_ms.size()) {
//            mRelativeTimestamp = mRelativeTimestamp;
//            return true;
//        }
//        // Length mismatch
//        return false;
//    }
//
//    public boolean addPair(int color, float time_ms) {
//        if (mRelativeTimestamp.size() == 0) {
//            mColorVals.add(color);
//            mRelativeTimestamp.add(time_ms);
//            return true;
//        }
//        if (mRelativeTimestamp.get(mRelativeTimestamp.size() - 1) < time_ms) {
//            mColorVals.add(color);
//            mRelativeTimestamp.add(time_ms);
//            return true;
//        }
//        // Time not advancing
//        return false;
//    }
//
//    public void clear() {
//        mColorVals.clear();
//        mRelativeTimestamp.clear();
//    }
//
//
//    public void setPeriod_ms() {
//        // TODO: implement
//    }

    protected static void finish() {
        //mColorGraphCompleteCallbackFunction.onColorGraphComplete(this);
    }

    protected static int getLastColorVal() {
    if (mColorVals.isEmpty())
        return 0;
    else
        return mColorVals.get(mColorVals.size() - 1 );
    }

//    public int size() { return mColorVals.size(); }
}
