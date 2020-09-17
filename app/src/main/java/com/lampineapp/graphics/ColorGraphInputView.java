/*
Credits to https://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
 */

package com.lampineapp.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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
        S_STOP_INDICATOR_CATCHED,
        S_GRAPH_DRAWING,
        S_GRAPH_COMPLETE
    }
    State mState;

    private GestureDetectorCompat mGestureDetector;
    private GestureListener mGestureListener;


    private Paint mBackgroundGradientPaint = new Paint();

    private Paint mGraphPaint;
    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas  mCanvas;

    // Frame
    private Paint mFramePaint = new Paint();
    private Path mFramePath = new Path();

    // Graph
    private Path mGraphPath = new Path();
    // Background
    private Paint mBitmapPaint = new Paint();
    // Position indicator
    private Paint mPosIndPaint = new Paint();
    private Path mPosIndPath = new Path();
    private Paint mPosIndCircPaint = new Paint() ;
    private Path mPosIndCircPath = new Path();
    // Start indicator
    private Paint mStartIndCirclePaint = new Paint();
    private Paint mStartIndTextPaint = new Paint();
    private Path mStartIndCirclePath = new Path();
    // Stop indicator
    private Paint mStopIndCirclePaint = new Paint();
    private Paint mStopIndTextPaint = new Paint();
    private Path mStopIndCirclePath = new Path();
    // Indicator match line
    private Paint mIndMatchLinePaint = new Paint();
    private Path mIndMatchLinePath = new Path();

    // Working variables
    private float mX, mY;
    private float mXPosInd, mYPosInd;
    private float mYStartInd, mXStartInd;
    private float mYStopInd, mXStopInd;
    private float colorOffset = 0;

    // Settable parameters graph
    private float mGraphLineWidth = 18;

    // Settable parameters frame
    private float mFrameWidth = 5;
    private int mFrameColor = Color.BLACK;

    // Settable parameters indicator
    private float mIndicatorLineWidth = 4;
    private float mIndicatorCircleLineWidth = 20;
    private float mIndicatorRadius = 10;
    private int mIndicatorColor = Color.BLACK;

    // Settable parameters background
    private float mBackgroundGradientLineWidth = 10;
    private int   mBackgroundGradientAlpha = 50;

    // Settable parameters start indicator
    private float mStartIndCatchRadius = 40;
    private float mStartIndRadius = 30;
    private int mStartIndCircleColor = Color.BLACK;
    private int mStartIndTextColor = Color.WHITE;
    private float mStartIndTextSize = 40;

    // Settable parameters stop indicator
    private float mStopIndCatchRadius = 40;
    private float mStopIndRadius = 30;
    private int mStopIndCircleColor = Color.BLACK;
    private int mStopIndTextColor = Color.WHITE;
    private float mStopIndTextSize = 40;

    // Padding
    private float padT = 50;
    private float padB = 50;
    private float padL = 50;
    private float padR = 50;

    // Tolerances
    private static final float TOUCH_TOLERANCE = 6;

    // TODO: IMPLEMENT PROPER CLASS
    private List<Integer> colors = new ArrayList<>();

    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureListener = new GestureListener(this);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

        mGraphPaint = new Paint();
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraphPath = new Path();

        // Frame
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeCap(Paint.Cap.ROUND);

        // Position indicator
        mPosIndPaint.setStyle(Paint.Style.STROKE);
        mPosIndCircPaint.setStyle(Paint.Style.FILL);

        // Start indicator initialization
        mStartIndCirclePaint.setStyle(Paint.Style.FILL);
        mStartIndCirclePaint.setStrokeWidth(1);

        // Stop indicator initialization
        mStopIndCirclePaint.setStyle(Paint.Style.FILL);
        mStopIndCirclePaint.setStrokeWidth(1);

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
            if (isCursorInsideRadius
                    (x0, y0, mXStartInd, mYStartInd, mStartIndCatchRadius)) {
                mState = State.S_START_INDICATOR_CATCHED;
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            // Long press on start indicator
            if (isCursorInsideRadius
                    (x0, y0, mXStopInd, mYStopInd, mStopIndCatchRadius)) {
                mState = State.S_STOP_INDICATOR_CATCHED;
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            final float x0 = getX();
            final float y0 = getY();

            if (isCursorInsideRadius(x0, y0, width/2, height/2, height) &&
                    mState == State.S_GRAPH_EMPTY) {
                clearDrawing();
                colorOffset += dY;
            }
            return true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        drawBackgroundGradient();
        mYStartInd = height / 2;
        mXStartInd = mFrameWidth/2 + padL;
        mYStopInd = height / 2;
        mXStopInd = width - padR - mFrameWidth/2;
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
        if (isCursorInsideRadius(x, y, mXStartInd, mYStartInd, mStartIndCatchRadius)) {
            mState = State.S_CURSOR_ON_START_IND;
            mX = correctXToDrawingArea(x);
            mY = correctXToDrawingArea(y);
        }

    }

    private void onTouchMove(float x, float y) {
        // Correct x or y if on or outside frame
        x = correctXToDrawingArea(x);
        y = correctYToDrawingArea(y);
        mXPosInd = x;
        mYPosInd = y;

        switch (mState) {
            case S_CURSOR_ON_START_IND:
                // Cursor is moving
                if (!isCursorInsideRadius(x, y, mX, mY, TOUCH_TOLERANCE)) {
                    mState = State.S_GRAPH_DRAWING;
                    colors.clear();
                    break;
                } 
                break; 
            case S_START_INDICATOR_CATCHED:
                clearDrawing();
                mYStartInd = y;
                break;
            case S_STOP_INDICATOR_CATCHED:
                clearDrawing();
                mYStopInd = y;
                break;
            case S_GRAPH_DRAWING:
                // Allow only forward movements in x direction
                if (x <= mX)
                    break;
                if (!isCursorInsideRadius(x, y, mX, mY, TOUCH_TOLERANCE)) {
                    // Update color based on y position
                    int color = DataHelpers
                            .getSpectrumColorFromRelative((y - padT + colorOffset) / (height - padT - padB));
                    colors.add(color);
                    mGraphPaint.setColor(color);
                    mGraphPaint.setStrokeWidth(mGraphLineWidth);

                    mGraphPath.moveTo(mX, mY);
                    mGraphPath.lineTo(x, y);

                    // Stop indicator reached
                    if (isCursorInsideRadius(x, y, mXStopInd, mYStopInd, mStopIndCatchRadius)) {
                        mGraphPath.lineTo(mXStopInd, mYStopInd);
                        mCanvas.drawPath(mGraphPath, mGraphPaint);
                        mState = State.S_GRAPH_COMPLETE;
                    }

                    // Patch must be drawn directly for using different colors
                    mCanvas.drawPath(mGraphPath, mGraphPaint);

                    mGraphPath.reset();

                    // Update cursor position
                    mX = x;
                    mY = y;
                }
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
                clearDrawing();
                break;
            case S_GRAPH_COMPLETE:
                Log.d(TAG, "Data points: " + colors.size());
                break;

        }
    }

    private void clearDrawing() {
        setDrawingCacheEnabled(false);
        // Bitmap needed for graph
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        drawBackgroundGradient();
        invalidate();
        setDrawingCacheEnabled(true);
    }

    private void drawFrame(Canvas canvas) {
        mFramePaint.setStrokeWidth(mFrameWidth);
        mFramePaint.setColor(mFrameColor);

        final float x0 = mFrameWidth/2 + padL;
        final float x1 = width - mFrameWidth/2 - padR;
        final float y0 =  mFrameWidth/2 + padT;
        final float y1 = height - mFrameWidth/2 - padB;

        mFramePath.reset();
        mFramePath.moveTo(x0, y0);
        mFramePath.lineTo(x1, y0);
        mFramePath.lineTo(x1, y1);
        mFramePath.lineTo(x0, y1);
        mFramePath.lineTo(x0, y0);
        canvas.drawPath(mFramePath, mFramePaint);
    }

    private void drawBackgroundGradient() {
        mBackgroundGradientPaint.setStrokeCap(Paint.Cap.BUTT);
        mBackgroundGradientPaint.setStrokeWidth(mBackgroundGradientLineWidth);

        float y = mBackgroundGradientLineWidth/2 + padT;

        for ( ; y < height - padB - mBackgroundGradientLineWidth; y += mBackgroundGradientLineWidth) {
            int color = DataHelpers.getSpectrumColorFromRelative((y-padT - mBackgroundGradientLineWidth/2 + colorOffset)/(height-padB-padT));
            color = DataHelpers.setA(color, mBackgroundGradientAlpha);
            mBackgroundGradientPaint.setColor(color);
            mCanvas.drawLine(0 + padL + mFrameWidth/2,
                    y, width - padR - mFrameWidth/2, y, mBackgroundGradientPaint);
        }

        // Revert last y increment
        y = y - mBackgroundGradientLineWidth;
        // Rest width between frame and last line border
        float restWidth = (height - padB - mFrameWidth/2) - (y + mBackgroundGradientLineWidth/2);
        // Set y to middle of free space between frame and last line border
        y = y + mBackgroundGradientLineWidth/2 + restWidth/2;
        // Last line shall fill the free space
        mBackgroundGradientPaint.setStrokeWidth(restWidth);
        mCanvas.drawLine(0 + padL, y,  width - padR, y, mBackgroundGradientPaint);
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
        mPosIndPath.moveTo(x0, 0);
        mPosIndPath.lineTo(x0, height);
        // Horizontal
        mPosIndPath.moveTo(0, y0);
        mPosIndPath.lineTo(width, y0);
        canvas.drawPath(mPosIndPath, mPosIndPaint);

        // Draw circle
        mPosIndCircPaint.setStrokeWidth(mIndicatorCircleLineWidth);
        mPosIndCircPaint.setColor(mIndicatorColor);
        mPosIndCircPath.addCircle(x0, y0, mIndicatorRadius, Path.Direction.CW);
        canvas.drawPath(mPosIndCircPath, mPosIndCircPaint);
    }

    private void drawStartInd(Canvas canvas) {
        final float x0 = mXStartInd;
        final float y0 = mYStartInd;

        // Draw circle
        mStartIndCirclePath.reset();
        mStartIndCirclePaint.setColor(mStartIndCircleColor);
        mStartIndCirclePath.addCircle(x0, y0 , mStartIndRadius, Path.Direction.CW);
        canvas.drawPath(mStartIndCirclePath, mStartIndCirclePaint);

        // Draw text
        mStartIndTextPaint.setTextSize(mStartIndTextSize);
        mStartIndTextPaint.setColor(mStartIndTextColor);
        Rect textBounds = new Rect();
        mStartIndTextPaint.getTextBounds("1", 0, 1, textBounds);
        canvas.drawText("1", x0 - textBounds.width(), y0 + textBounds.height()/2,
                mStartIndTextPaint);
    }

    private void drawStopInd(Canvas canvas) {
        final float x0 = mXStopInd;
        final float y0 = mYStopInd;

        // Draw circle
        mStopIndCirclePath.reset();
        mStopIndCirclePaint.setColor(mStopIndCircleColor);
        mStopIndCirclePath.addCircle(x0 , y0, mStopIndRadius, Path.Direction.CW);
        canvas.drawPath(mStopIndCirclePath, mStopIndCirclePaint);

        // Draw text
        mStopIndTextPaint.setTextSize(mStopIndTextSize);
        mStopIndTextPaint.setColor(mStopIndTextColor);
        Rect textBounds = new Rect();
        mStopIndTextPaint.getTextBounds("2", 0, 1, textBounds);
        canvas.drawText("2", x0 - textBounds.width()/2,y0 + textBounds.height()/2,
                mStopIndTextPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        drawFrame(canvas);
        drawPosInd(canvas);
        drawStartInd(canvas);
        drawStopInd(canvas);
    }

    private boolean isXLeftOfFrame(float x) {
        if (x - mGraphLineWidth/2 < mFrameWidth + padL) return true;
        return false;
    }

    private boolean isXRightOfFrame(float x) {
        if (x + mGraphLineWidth/2 > width  - mFrameWidth - padL) return true;
        return false;
    }

    private float correctXToDrawingArea(float x) {
        // Correct x if on or outside frame
        if (isXLeftOfFrame(x)) x = mGraphLineWidth/2 + mFrameWidth + padL;
        if (isXRightOfFrame(x)) x = width  - mGraphLineWidth/2 - mFrameWidth - padR;
        return x;
    }

    private boolean isYTopOfFrame(float y) {
        if (y - mGraphLineWidth/2 < mFrameWidth/2 + padT) return true;
        return false;
    }

    private boolean isYBotOfFrame(float y) {
        if (y + mGraphLineWidth/2 > height - mFrameWidth - padB - mGraphLineWidth/2) return true;
        return false;
    }

    private float correctYToDrawingArea(float y) {
        // Correct y if on or outside frame
        if (isYTopOfFrame(y)) y = mGraphLineWidth/2 + mFrameWidth/2 + padT;
        if (isYBotOfFrame(y)) y = height - mGraphLineWidth/2 - mFrameWidth - padB;
        return y;
    }

    private boolean isCursorInsideRadius(float x, float y, float xC, float yC, float R) {
        final float dX = x - xC;
        final float dY = y - yC;
        if (dX*dX + dY*dY <= R*R) {
            return true;
        }
        return false;
    }

}
