/*
Credits to https://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
 */

package com.lampineapp.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.view.ViewConfiguration.getLongPressTimeout;

public class ColorGraphInputView extends View {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    enum State {
        S_EMPTY,
        S_START_INDICATOR_CATCHED,
        S_STOP_INDICATOR_CATCHED,
        S_START_DRAW_GRAPH,
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

    // TODO: INIT PAINTS AND PATHS HERE; NOT IN CONSTRUCTOR!!!
    private Path mGraphPath;
    private Paint  mBitmapPaint;
    private Paint mIndicatorPaint;
    private Paint mFramePaint;
    private Paint mIndicatorCirclePaint;
    private Paint mStartIndCirclePaint;
    private Paint mStartIndTextPaint;
    private Paint mStopIndCirclePaint;
    private Paint mStopIndTextPaint;
    private Path mIndicatorPath;
    private Path mIndicatorCirclePath;
    private Path mFramePath;
    private Path mStartIndCirclePath;
    private Path mStopIndCirclePath;

    // Start indicator
    private Path mStartIndDrawerPath = new Path();
    private Paint mStartIndDrawerPaint = new Paint();

    // Stop indicator
    private Path mStopIndDrawerPath = new Path();
    private Paint mStopIndDrawerPaint = new Paint();

    private float mX, mY;
    private float mYStartIndicator;
    private float mXStartIndicator;
    private float mYStopIndicator;
    private float mXStopIndicator;

    // Long press detection
    private boolean mLongPressDetected = false;

    // Settable parameters graph
    private float mGraphLineWidth = 10;

    // Settable parameters frame
    private float mFrameWidth = 5;
    private int mFrameColor = Color.BLACK;

    // Settable parameters indicator
    private float mIndicatorLineWidth = 4;
    private float mIndicatorCircleLineWidth = 20;
    private float mIndicatorRadius = 10;
    private int mIndicatorColor = Color.BLACK;

    // Settable parameters background
    private float mBackgroundGradientLineWidth = 4;
    private int   mBackgroundGradientAlpha = 50;

    // Settable parameters start indicator
    private float mStartIndDrawCatchRadius = 50;
    private float mStartIndCircleWidth = 50;
    private float mStartIndCircleRadius = 25;
    private int mStartIndCircleColor = Color.BLACK;
    private float mStartIndDrawerCatchMargin = 50;
    private int mStartIndTextColor = Color.WHITE;
    private float mStartIndTextSize = 30;
    private float mStartIndDrawerW1 = 60;
    private float mStartIndDrawerW2 = 60;
    private float mStartIndDrawerH = 60;
    private int mStartIndDrawerColor = Color.BLACK;

    // Settable parameters stop indicator
    private float mStopIndCircleWidth = 40;
    private float mStopIndCircleRadius = 20;
    private int mStopIndCircleColor = Color.BLACK;
    private float mStopIndDrawerCatchMargin = 50;
    private int mStopIndTextColor = Color.WHITE;
    private float mStopIndTextSize = 30;
    private float mStopIndDrawerW1 = 60;
    private float mStopIndDrawerW2 = 60;
    private float mStopIndDrawerH = 60;
    private int mStopIndDrawerColor = Color.BLACK;

    // Padding
    private float padT = 150;
    private float padB = 150;
    private float padL = 150;
    private float padR = 150;

    // Tolerances
    private static final float TOUCH_TOLERANCE = 1;
    private static final int COLOR_TOLERANCE = 0;

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

        mFramePaint = new Paint();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeCap(Paint.Cap.ROUND);
        mFramePath = new Path();

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPath = new Path();
        mIndicatorCirclePaint = new Paint();
        mIndicatorCirclePaint.setStyle(Paint.Style.STROKE);
        mIndicatorCirclePath = new Path();

        mStartIndCirclePath = new Path();
        mStartIndCirclePaint = new Paint();
        mStartIndTextPaint = new Paint();

        mStopIndCirclePath = new Path();
        mStopIndCirclePaint = new Paint();
        mStopIndTextPaint = new Paint();

        // Start indicator
        mStartIndDrawerPaint.setStyle(Paint.Style.FILL);

        // Stop indicator
        mStartIndDrawerPaint.setStyle(Paint.Style.FILL);

        mState = State.S_EMPTY;

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

            // Long press on start indicator
            if (isCursorInStartIndDrawCatchArea(e.getX(), e.getY())) {
                mState = State.S_START_INDICATOR_CATCHED;
                Log.d(TAG, "Long press detected!");
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }

        // TODO: Handle touch events here????

        @Override
        public boolean onDown(MotionEvent e) {
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
        drawFrame();
        mYStartIndicator = height / 2;
        mXStartIndicator = mFrameWidth/2 + padL;
        mYStopIndicator = height / 2;
        mXStopIndicator = width - padR - mFrameWidth/2;
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
        if (mLongPressDetected == true) {
            Log.d(TAG, "LongPressDetected!");
            mLongPressDetected = false;
        }
        return true;
    }

    private void onTouchDown(float x, float y) {

        switch (mState) {
            case S_EMPTY:
                // Start indicator move catch?
                if (isCursorInStartIndCatchArea(x, y)) {
                    mState = State.S_START_INDICATOR_CATCHED;
                    clearDrawing();
                }
                // Stop indicator move match?
                if (isCursorInStopIndCatchArea(x, y)) {
                    mState = State.S_STOP_INDICATOR_CATCHED;
                    clearDrawing();
                }
                // Start indicator draw catch?
                if (isCursorInStartIndDrawCatchArea(x, y)) {
                    mState = State.S_START_DRAW_GRAPH;
                    clearDrawing();
                }

        }
        mX = correctXToDrawingArea(x);
        mY = correctYToDrawingArea(y);
        return;

        // Remove old line on new touchdown

//        mGraphPath.reset();
//        colors.clear();
    }

    private void onTouchMove(float x, float y) {
        // Correct x or y if on or outside frame
        x = correctXToDrawingArea(x);
        y = correctYToDrawingArea(y);

        final float dX = x - mX;
        final float dY = y - mY;

        switch (mState) {
            case S_START_INDICATOR_CATCHED:
                mYStartIndicator = y;
                break;
            case S_STOP_INDICATOR_CATCHED:
                mYStopIndicator = y;
                break;
            case S_START_DRAW_GRAPH:
                Log.d(TAG, "Draw graph");
                // Allow only forward movements in x direction
                if (x < mX)
                    break;
                if (Math.abs(dX) >= TOUCH_TOLERANCE || Math.abs(dY) >= TOUCH_TOLERANCE) {
                    // Update color based on y position
                    int color = DataHelpers
                            .getSpectrumColorFromRelative((y - padT) / (height - padT - padB));
                    colors.add(color);
                    mGraphPaint.setColor(color);
                    mGraphPaint.setStrokeWidth(mGraphLineWidth);

                    mGraphPath.moveTo(mX, mY);
                    mGraphPath.lineTo(x, y);

                    // Patch must be drawn directly for using different colors
                    mCanvas.drawPath(mGraphPath, mGraphPaint);
                    mGraphPath.reset();

                    drawIndicator(x, y);
                }
                break;
        }

        mY = y;
        mX = x;

        return;


    }

    private void onTouchUp(float x, float y) {

        switch (mState) {
            case S_START_INDICATOR_CATCHED:
            case S_STOP_INDICATOR_CATCHED:
            case S_START_DRAW_GRAPH:
                mState = State.S_EMPTY;
                return;
        }

        mIndicatorPath.reset();
        mIndicatorCirclePath.reset();

        mX = 0;
        mY = 0;
    }

    private void clearDrawing() {
        setDrawingCacheEnabled(false);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        drawBackgroundGradient();
        drawFrame();
        invalidate();
        setDrawingCacheEnabled(true);
    }

    private void drawFrame() {
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
    }

    private void drawBackgroundGradient() {
        mBackgroundGradientPaint.setStrokeCap(Paint.Cap.BUTT);
        mBackgroundGradientPaint.setStrokeWidth(mBackgroundGradientLineWidth);

        float y = mBackgroundGradientLineWidth/2 + padT;

        for ( ; y < height - padB; y += mBackgroundGradientLineWidth) {
            int color = DataHelpers.getSpectrumColorFromRelative((y-padT - mBackgroundGradientLineWidth/2)/(height-padB-padT));
            color = DataHelpers.setA(color, mBackgroundGradientAlpha);
            mBackgroundGradientPaint.setColor(color);
            mCanvas.drawLine(0 + padL + mFrameWidth/2,
                    y, width - padR - mFrameWidth/2, y, mBackgroundGradientPaint);

            //Log.d(TAG, DataHelpers.getR(color) + " "
             //       + DataHelpers.getG(color) + " "
               //     + DataHelpers.getB(color));
        }

        // Redraw last line at y position wich fills rest of frame
        y = y + ((height - padB - mFrameWidth/2) - (y + mBackgroundGradientLineWidth/2));
        mCanvas.drawLine(0 + padL, y,  width - padR, y, mBackgroundGradientPaint);

    }

    private void drawIndicator(float x, float y) {
        // Draw vertical and horizontal lines
        mIndicatorPaint.setStrokeWidth(mIndicatorLineWidth);
        mIndicatorPaint.setColor(mIndicatorColor);
        mIndicatorPath.reset();
        // Vertical
        mIndicatorPath.moveTo(x, 0);
        mIndicatorPath.lineTo(x, height);
        // Horizontal
        mIndicatorPath.moveTo(0, y);
        mIndicatorPath.lineTo(width, y);

        // Draw circle
        mIndicatorCirclePaint.setStrokeWidth(mIndicatorCircleLineWidth);
        mIndicatorCirclePaint.setColor(mIndicatorColor);
        mIndicatorCirclePath.reset();
        mIndicatorCirclePath.addCircle(x, y, mIndicatorRadius, Path.Direction.CW);
    }

    private void drawStartIndicator(Canvas canvas) {
        final float x0 = mXStartIndicator;
        final float y0 = mYStartIndicator;

//        // Draw Drawer
//        mStartIndDrawerPath.reset();
//        CornerPathEffect corEffect = new CornerPathEffect(10);
//        mStartIndDrawerPaint.setPathEffect(corEffect);
//        mStartIndDrawerPaint.setStrokeWidth(2);
//        mStartIndDrawerPaint.setColor(mStartIndDrawerColor);
//        mStartIndDrawerPath.moveTo(x0, y0);
//        mStartIndDrawerPath.rLineTo(-mStartIndDrawerW2, -mStartIndDrawerH/2);
//        mStartIndDrawerPath.rLineTo(-mStartIndDrawerW1,  +0);
//        mStartIndDrawerPath.rLineTo(+0, +mStartIndDrawerH);
//        mStartIndDrawerPath.rLineTo(+mStartIndDrawerW1, 0);
//        mStartIndDrawerPath.rLineTo(mStartIndDrawerW2, -mStartIndDrawerH/2);
//        canvas.drawPath(mStartIndDrawerPath, mStartIndDrawerPaint);

        // Draw circle
        mStartIndCirclePath.reset();
        mStartIndCirclePaint.setStrokeWidth(mStartIndCircleWidth);
        mStartIndCirclePaint.setColor(mStartIndCircleColor);
        mStartIndCirclePath.addCircle(
                mXStartIndicator, mYStartIndicator , mStartIndCircleRadius, Path.Direction.CW);
        canvas.drawPath(mStartIndCirclePath, mStartIndCirclePaint);

        // Draw text
        mStartIndTextPaint.setTextSize(mStartIndTextSize);
        mStartIndTextPaint.setColor(mStartIndTextColor);
        Rect textBounds = new Rect();
        mStartIndTextPaint.getTextBounds("1", 0, 1, textBounds);
        canvas.drawText("1", mXStartIndicator-textBounds.width(),
                mYStartIndicator+textBounds.height()/2, mStartIndTextPaint);
    }

    private void drawStopIndicator(Canvas canvas) {
        final float x0 = mXStopIndicator;
        final float y0 = mYStopIndicator;

        // Draw Drawer
        mStopIndDrawerPath.reset();
        CornerPathEffect corEffect = new CornerPathEffect(10);
        mStopIndDrawerPaint.setPathEffect(corEffect);
        mStopIndDrawerPaint.setStrokeWidth(2);
        mStopIndDrawerPaint.setColor(mStopIndDrawerColor);
        mStopIndDrawerPath.moveTo(x0, y0);
        mStopIndDrawerPath.rLineTo(+mStopIndDrawerW2, -mStopIndDrawerH/2);
        mStopIndDrawerPath.rLineTo(+mStopIndDrawerW1,  +0);
        mStopIndDrawerPath.rLineTo(+0, +mStopIndDrawerH);
        mStopIndDrawerPath.rLineTo(-mStopIndDrawerW1, 0);
        mStopIndDrawerPath.rLineTo(-mStopIndDrawerW2, -mStopIndDrawerH/2);
        canvas.drawPath(mStopIndDrawerPath, mStopIndDrawerPaint);

        // Draw circle
        mStopIndCirclePath.reset();
        mStopIndCirclePaint.setStrokeWidth(mStopIndCircleWidth);
        mStopIndCirclePaint.setColor(mStopIndCircleColor);
        mStopIndCirclePath.addCircle(
                mXStopIndicator, mYStopIndicator , mStopIndCircleRadius, Path.Direction.CW);
        canvas.drawPath(mStopIndCirclePath, mStopIndCirclePaint);

        // Draw text
        mStopIndTextPaint.setTextSize(mStopIndTextSize);
        mStopIndTextPaint.setColor(mStopIndTextColor);
        Rect textBounds = new Rect();
        mStopIndTextPaint.getTextBounds("2", 0, 1, textBounds);
        canvas.drawText("2", mXStopIndicator-textBounds.width(),
                mYStopIndicator+textBounds.height()/2, mStopIndTextPaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mIndicatorPath, mIndicatorPaint);
        canvas.drawPath(mIndicatorCirclePath, mIndicatorCirclePaint);
        canvas.drawPath(mFramePath, mFramePaint);
        drawStartIndicator(canvas);
        drawStopIndicator(canvas);
    }

    private boolean isXLeftOfFrame(float x) {
        if (x - mGraphLineWidth/2 < mFrameWidth/2 + padL)
            return true;
        else
            return false;
    }

    private boolean isXRightOfFrame(float x) {
        if (x + mGraphLineWidth/2 > width  - mFrameWidth/2 - padL)
            return true;
        else
            return false;
    }

    private float correctXToDrawingArea(float x) {
        // Correct x if on or outside frame
        if (isXLeftOfFrame(x))
            x = mGraphLineWidth/2 + mFrameWidth/2 + padL;
        if (isXRightOfFrame(x))
            x = width  - mGraphLineWidth/2 - mFrameWidth/2 - padR;
        return x;
    }

    private boolean isYTopOfFrame(float y) {
        if (y - mGraphLineWidth/2 < mFrameWidth/2 + padT)
            return true;
        else
            return false;
    }

    private boolean isYBotOfFrame(float y) {
        if (y + mGraphLineWidth/2 > height - mFrameWidth/2 - padB)
            return true;
        else
            return false;
    }

    private float correctYToDrawingArea(float y) {
        // Correct y if on or outside frame
        if (isYTopOfFrame(y))
            y = mGraphLineWidth/2 + mFrameWidth/2 + padT;
        if (isYBotOfFrame(y))
            y = height - mGraphLineWidth/2 - mFrameWidth/2 - padB;
        return y;
    }

    private boolean isCursorInStartIndDrawCatchArea(float x, float y) {
        final float dX = x - mXStartIndicator;
        final float dY = y - mYStartIndicator;
        if (dX*dX + dY*dY <= mStartIndDrawCatchRadius * mStartIndDrawCatchRadius) {
            Log.d(TAG, " start draw chatch");
            return true;
        }
        return false;
    }

    private boolean isCursorInStartIndCatchArea(float x, float y) {
//        if (y >= mYStartIndicator - mStartIndDrawerH/2 - mStartIndDrawerCatchMargin &&
//                y <= mYStartIndicator + mStartIndDrawerH/2 + mStartIndDrawerCatchMargin &&
//                x >= mXStartIndicator - mStartIndDrawerW2
//                        - mStartIndDrawerW1 - mStartIndDrawerCatchMargin &&
//                x <= mXStartIndicator - mStartIndDrawerW2)
//            return true;
        return false;
    }

    private boolean isCursorInStopIndCatchArea(float x, float y) {
        if (y >= mYStopIndicator - mStopIndDrawerH/2 - mStopIndDrawerCatchMargin &&
                y <= mYStopIndicator + mStopIndDrawerH/2 + mStopIndDrawerCatchMargin &&
                x <= mXStopIndicator + mStopIndDrawerW2
                        + mStopIndDrawerW1 + mStopIndDrawerCatchMargin &&
                x >= mXStopIndicator + mStopIndDrawerW2)
            return true;
        return false;
    }
}
