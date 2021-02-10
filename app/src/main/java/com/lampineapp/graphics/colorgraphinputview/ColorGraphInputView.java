/*
Credits to https://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
 */

package com.lampineapp.graphics.colorgraphinputview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

public class ColorGraphInputView extends View {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    enum State {
        S_GRAPH_EMPTY,
        S_CURSOR_ON_START_IND,
        S_START_INDICATOR_CATCHED,
        S_START_INDICATOR_MOVING,
        S_STOP_INDICATOR_CATCHED,
        S_GRAPH_DRAWING,
        S_CURSOR_ON_STOP_IND,
        S_GRAPH_COMPLETE
    } State mState = State.S_GRAPH_EMPTY;

    private GestureDetectorCompat mGestureDetector;
    private GestureListener mGestureListener;
    private CurvePointIndicator mStartInd = new CurvePointIndicator("1");
    private CurvePointIndicator mStopInd = new CurvePointIndicator("2");
    private CurvePositionIndicator mPosInd = new CurvePositionIndicator();
    private ColorGraph mColorGraph;
    private static CurveCompleteCallbackFun mCurveCompleteCallbackFun;

    // Working variables
    private float mX, mY;

    /***********************************************************************************************
     *
     * Interfaces
     */
    public interface CurveCompleteCallbackFun {
        void onCurveComplete(ColorTimeSeries curve);
    }

    /***********************************************************************************************
     *
     * Public methods
     */
    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureListener = new GestureListener(this);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
    }

    public void setCurveCompleteCallbackFun(CurveCompleteCallbackFun fun) {
        mCurveCompleteCallbackFun = fun;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ColorGraphInputView v;

        public GestureListener(ColorGraphInputView v) {
            super();
            this.v = v;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

            final float x0 = e.getX();
            final float y0 = e.getY();
            // Long press on start indicator
            if (mStartInd.isOnCatchArea(x0, y0)) {
                // TODO: MOVE CHECK TO IND OBJ AND RETURN STATE, WORK WITH STATE HERE?
                mState = State.S_START_INDICATOR_CATCHED;
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            // Long press on stop indicator
            if (mStopInd.isOnCatchArea(x0, y0)) {
                mState = State.S_STOP_INDICATOR_CATCHED;
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            if (mState == State.S_GRAPH_EMPTY) {
                InputFrame.scrollBackground(getX(), getY(), dY);
            }
            return true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        InputFrame.onSizeChanged(w, h);
        mStartInd.setPos(InputFrame.getX0(), InputFrame.getYCenter());
        mStopInd.setPos(InputFrame.getX1(), InputFrame.getYCenter());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        // Detect long presses
        mGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(x, y);
                invalidate();
                break;
        }
        return true;
    }

    private void onTouchDown(float x, float y) {
        // Cursor on start indicator
        if (mStartInd.isOnCatchArea(x, y)) {
            mState = State.S_CURSOR_ON_START_IND;
            mX = InputFrame.cutX(x);
            mY = InputFrame.cutY(y);
        }

    }

    private void onTouchMove(float x, float y) {
        // Correct x or y if on or outside frame
        x = InputFrame.cutX(x);
        y = InputFrame.cutY(y);

        switch (mState) {
            case S_START_INDICATOR_CATCHED:
                InputFrame.clearFrame();
                mStartInd.Y = y;
                break;
            case S_STOP_INDICATOR_CATCHED:
                InputFrame.clearFrame();
                mStopInd.Y = y;
                break;
            case S_CURSOR_ON_START_IND:
                if (!mStartInd.isOnCatchArea(x, y)) {
                    // Cursor is moving
                    mPosInd.show();
                    mState = State.S_GRAPH_DRAWING;
                    mColorGraph = new ColorGraph(mX, mY, x, y);
                    this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                    break;
                } 
                break;
            case S_GRAPH_DRAWING:
                mColorGraph.addSegment(x, y);
                mPosInd.setPos(x, y);
                if (mStopInd.isOnCatchArea(x, y)) {
                    // Stop indicator reached
                    mState = State.S_CURSOR_ON_STOP_IND;
                    this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                }

                break;
            case S_CURSOR_ON_STOP_IND:
                mColorGraph.finish(x,y);
                mPosInd.hide();
        }
    }

    private void onTouchUp(float x, float y) {
        switch (mState) {
            case S_START_INDICATOR_CATCHED:
            case S_STOP_INDICATOR_CATCHED:
            case S_CURSOR_ON_START_IND:
            case S_GRAPH_DRAWING:
                mState = State.S_GRAPH_EMPTY;
                // TODO: SURROUND CLEAR DRAWING BY DRAWING CACHE DISABLE / ENABLE + INVALIDATE()?
                InputFrame.clearFrame();
                break;
            case S_GRAPH_COMPLETE:
                Log.d(TAG, "Data points: " + mColorGraph.CURVE.size());
                mCurveCompleteCallbackFun.onCurveComplete(mColorGraph.CURVE);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        InputFrame.draw(canvas);
        mPosInd.draw(canvas);
        mStartInd.draw(canvas);
        mStopInd.draw(canvas);
    }
}
