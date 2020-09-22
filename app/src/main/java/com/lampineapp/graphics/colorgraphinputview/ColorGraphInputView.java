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

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;
import java.util.List;

public class ColorGraphInputView extends View {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    enum State {
        S_GRAPH_EMPTY,
        S_CURSOR_ON_START_IND,
        S_START_INDICATOR_CATCHED,
        S_START_INDICATOR_MOVING,
        S_STOP_INDICATOR_CATCHED,
        S_GRAPH_DRAWING,
        S_GRAPH_COMPLETE
    }
    State mState;

    private GestureDetectorCompat mGestureDetector;
    private GestureListener mGestureListener;
    CurvePointIndicator mStartInd = new CurvePointIndicator("1");
    CurvePointIndicator mStopInd = new CurvePointIndicator("2");

    // Position indicator
    private Paint mPosIndPaint = new Paint();
    private Path mPosIndPath = new Path();
    private Paint mPosIndCircPaint = new Paint() ;
    private Path mPosIndCircPath = new Path();

    // TODO: REMOVE UNECESSARY PATHS AND DRAW ON CANVAS DIRECTLY!!!

    // Working variables
    private float mX, mY;

    // Graph members
    private float mGraphLineWidth = 18;

    // TODO: REMOVE SEPARATE ARRAYS?
    private ColorGraph mColorGraph = new ColorGraph();
    private List<Integer> mColorValues = new ArrayList<>();
    private List<Float> mTimeValues_ms = new ArrayList<>();

    // Settable parameters indicator
    private float mIndicatorLineWidth = 4;
    private float mIndicatorCircleLineWidth = 20;
    private float mIndicatorRadius = 10;
    private int mIndicatorColor = Color.BLACK;

    // Tolerances
    private static final float TOUCH_TOLERANCE = 2;

    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureListener = new GestureListener(this);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

        // Position indicator
        mPosIndPaint.setStyle(Paint.Style.STROKE);
        mPosIndCircPaint.setStyle(Paint.Style.FILL);

        mState = State.S_GRAPH_EMPTY;
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
            case S_CURSOR_ON_START_IND:
                // Cursor is moving
                if (!Helpers.isInsideRadius(x, y, mX, mY, TOUCH_TOLERANCE)) {
                    mState = State.S_GRAPH_DRAWING;
                    // TODO: MOVE TO POSITION WHERE NOT 100 OBJECTS ARE CREATED!
                    mColorGraph = new ColorGraph();
                    //mColorCurve.clear();
                    break;
                } 
                break; 
            case S_START_INDICATOR_CATCHED:
                InputFrame.clearFrame();
                mStartInd.Y = y;
                break;
            case S_STOP_INDICATOR_CATCHED:
                InputFrame.clearFrame();
                mStopInd.Y = y;
                break;
            case S_GRAPH_DRAWING:
                ColorGraph.addSegment(x, y);
                break;
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
                Log.d(TAG, "Data points: " + mColorGraph.size());
                mColorGraph.finish();
                break;
        }
    }


    private void drawPosInd(Canvas canvas) {
        mPosIndPath.reset();
        mPosIndCircPath.reset();
        if (mState != State.S_GRAPH_DRAWING)
            return;

        final float x0 = mX;
        final float y0 = mY;

        // Draw vertical and horizontal lines
        mPosIndPaint.setStrokeWidth(mIndicatorLineWidth);
        mPosIndPaint.setColor(mIndicatorColor);
        // Vertical
        mPosIndPath.moveTo(x0, InputFrame.getY0());
        mPosIndPath.lineTo(x0, InputFrame.getY1());
        // Horizontal
        mPosIndPath.moveTo(InputFrame.getX0(), y0);
        mPosIndPath.lineTo(InputFrame.getX1(), y0);
        canvas.drawPath(mPosIndPath, mPosIndPaint);

        // Draw circle
        mPosIndCircPaint.setStrokeWidth(mIndicatorCircleLineWidth);
        mPosIndCircPaint.setColor(mIndicatorColor);
        mPosIndCircPath.addCircle(x0, y0, mIndicatorRadius, Path.Direction.CW);
        canvas.drawPath(mPosIndCircPath, mPosIndCircPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        InputFrame.draw(canvas);
        drawPosInd(canvas);
        mStartInd.draw(canvas);
        mStopInd.draw(canvas);
    }
}
