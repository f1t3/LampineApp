package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Paint;
import android.graphics.Path;

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;

public class ColorGraph {

    /***********************************************************************************************
     *
     * Members
     */
    // Parameter
    protected float ADD_SEGMENT_THRESHOLD = 10;
    protected int COLOR_VALUE_THRESHOLD = 256/16;
    protected float X = 0, Y = 0;

    // Member
    private Path mGraphPath = new Path();
    private Paint mGraphPaint = new Paint();
    protected ColorTimeSeries CURVE = new ColorTimeSeries();

    /***********************************************************************************************
     *
     * Protected methods
     */
    protected boolean addSegment(float x, float y) {

        // Allow only forward movements in x direction
        // TODO: ADD THRESHOLD TO X?
        if (x <= X)
            return false;

        // Update graph if threshold exceeded
        if (!Helpers.isInsideRadius(x, y, X, Y, ADD_SEGMENT_THRESHOLD)) {
            // Update color on screen based on y position
            int color = InputFrame.getColorFromY(y);

            // TODO: FEEDBACK ON CONTROLLER!
//            // Perform feedback on start of graph drawing
//            if (getLastColorVal() == 0)
//                .performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            // Store as new data point if color delta exceeds threshold

            if (DataHelpers
                    .isColorDeltaGreaterThan(color, CURVE.getLastColorVal(),
                            COLOR_VALUE_THRESHOLD)) {
                CURVE.addPair(color, InputFrame.getRelXPos(x));
            }
            mGraphPaint.setStyle(Paint.Style.STROKE);
            mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
            mGraphPaint.setStrokeWidth(InputFrame.GRAPHLINE_W);
            mGraphPaint.setColor(CURVE.getLastColorVal());
            mGraphPath.moveTo(X, Y);
            mGraphPath.lineTo(x, y);

            // TODO: MOVE TO STOP INDICATOR
//            // Stop indicator reached
//            if (mStopInd.isOnCatchArea(x, y)) {
//                mGraphPath.lineTo(mStopInd.X, mStopInd.Y);
//                InputFrame.CANVAS.drawPath(mGraphPath, mGraphPaint);
//                mState = ColorGraphInputView.State.S_GRAPH_COMPLETE;
//                this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
//            }

            // Path must be drawn directly for using different colors
            InputFrame.CANVAS.drawPath(mGraphPath, mGraphPaint);

            mGraphPath.reset();

            // Update cursor position
            X = x;
            Y = y;
            return true;
        }
        return false;
    }

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

    /***********************************************************************************************
     *
     * Private methods
     */
}
