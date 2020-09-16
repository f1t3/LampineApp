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
import android.view.MotionEvent;
import android.view.View;

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;
import java.util.List;

public class ColorGraphInputView extends View {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    enum State {
        S_EMPTY,
        S_START_INDICATOR_MOVING,
        S_STOP_INDICATOR_MOVING,
    }
    State mState;

    private Paint mGraphPaint;
    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas  mCanvas;

    // TODO: INIT PAINTS AND PATHS HERE; NOT IN CONSTRUCTOR!!!
    private Path mGraphPath;
    private Paint   mBitmapPaint;
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


    private float mX, mY;
    private float mYStartIndicator;
    private float mXStartIndicator;
    private float mYStopIndicator;
    private float mXStopIndicator;

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
    private float mBackgroundGradientLineWidth = 10;
    private int   mBackgroundGradientAlpha = 30;

    // Settable parameters start circle
    private float mStartIndCircleWidth = 60;
    private float mStartIndCircleRadius = 30;
    private int mStartIndCircleColor = Color.BLACK;
    private float mStartIndCircleMoveCatchRadius = 100;
    private int mStartIndTextColor = Color.WHITE;
    private float mStartIndTextSize = 30;

    // Settable parameters stop circle
    private float mStopIndCircleWidth = 60;
    private float mStopIndCircleRadius = 30;
    private int mStopIndCircleColor = Color.BLACK;
    private float mStopIndCircleMoveCatchRadius = 100;
    private int mStopIndTextColor = Color.WHITE;
    private float mStopIndTextSize = 30;

    // Padding
    private float padT = 60;
    private float padB = 60;
    private float padL = 60;
    private float padR = 60;

    // Tolerances
    private static final float TOUCH_TOLERANCE = 1;
    private static final int COLOR_TOLERANCE = 0;

    // TODO: IMPLEMENT PROPER CLASS
    private List<Integer> colors = new ArrayList<>();

    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

        mState = State.S_EMPTY;

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

        switch (mState) {
            case S_EMPTY:
                // Cursor near start indicator?
                if (isCursorInStartIndCatchArea(x, y))
                    mState = State.S_START_INDICATOR_MOVING;
                // Cursor near stop indicator?
                if (isCursorInStopIndCatchArea(x, y))
                    mState = State.S_STOP_INDICATOR_MOVING;
                return;

        }

        // Remove old line on new touchdown
        clearDrawing();
        mGraphPath.reset();

        mX = correctXToDrawingArea(x);
        mY = correctYToDrawingArea(y);

        colors.clear();
    }

    private void onTouchMove(float x, float y) {
        // Correct x or y if on or outside frame
        x = correctXToDrawingArea(x);
        y = correctYToDrawingArea(y);

        switch (mState) {
            case S_START_INDICATOR_MOVING:
                mYStartIndicator = y;
                return;
            case S_STOP_INDICATOR_MOVING:
                mYStopIndicator = y;
                return;
        }

        // Allow only forward movements in x direction
        final float dX = x - mX;
        final float dY = y - mY;
        if (x < mX)
            return;

        if (Math.abs(dX) >= TOUCH_TOLERANCE || Math.abs(dY) >= TOUCH_TOLERANCE) {

            // Update color based on y position
            int color = DataHelpers.getSpectrumColorFromRelative(y/height);
            colors.add(color);
            mGraphPaint.setColor(color);
            mGraphPaint.setStrokeWidth(mGraphLineWidth);

            mGraphPath.moveTo(mX, mY);
            mGraphPath.lineTo(x, y);
            mX = x;
            mY = y;

            // Patch must be drawn directly for using different colors
            mCanvas.drawPath(mGraphPath, mGraphPaint);
            mGraphPath.reset();

            drawIndicator(x, y);
        }
    }

    private void onTouchUp(float x, float y) {

        switch (mState) {
            case S_START_INDICATOR_MOVING:
            case S_STOP_INDICATOR_MOVING:
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
        onSizeChanged(width, height, width, height);
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
        mGraphPaint.setStrokeWidth(mBackgroundGradientLineWidth);
        float y = mBackgroundGradientLineWidth/2;
        for ( ; y < height; y += mBackgroundGradientLineWidth) {
            int color = DataHelpers.getSpectrumColorFromRelative(y/height);
            color = DataHelpers.setA(color, mBackgroundGradientAlpha);
            mGraphPaint.setColor(color);
            mCanvas.drawLine(0, y, width, y, mGraphPaint);
        }
        // Redraw last line with thicker stroke if necessary
        if (y + mBackgroundGradientLineWidth/2 < height) {
            mGraphPaint.setStrokeWidth((height - y) * 2);
            mCanvas.drawLine(0, y, width, y, mGraphPaint);
        }
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
        // Draw circle
        mStartIndCirclePath.reset();
        mStartIndCirclePaint.setStrokeWidth(mStartIndCircleWidth);
        mStartIndCirclePaint.setColor(mStartIndCircleColor);
        mStartIndCirclePath.addCircle(
                mXStartIndicator, mYStartIndicator , mStartIndCircleRadius, Path.Direction.CW);
        mStartIndTextPaint.setTextSize(mStartIndTextSize);
        canvas.drawPath(mStartIndCirclePath, mStartIndCirclePaint);

        // Draw text
        mStartIndTextPaint.setColor(mStartIndTextColor);
        Rect textBounds = new Rect();
        mStartIndTextPaint.getTextBounds("1", 0, 1, textBounds);
        canvas.drawText("1", mXStartIndicator-textBounds.width(),
                mYStartIndicator+textBounds.height()/2, mStartIndTextPaint);
    }

    private void drawStopIndicator(Canvas canvas) {
        // Draw circle
        mStopIndCirclePath.reset();
        mStopIndCirclePaint.setStrokeWidth(mStopIndCircleWidth);
        mStopIndCirclePaint.setColor(mStopIndCircleColor);
        mStopIndCirclePath.addCircle(
                mXStopIndicator, mYStopIndicator , mStopIndCircleRadius, Path.Direction.CW);
        mStopIndTextPaint.setTextSize(mStopIndTextSize);
        canvas.drawPath(mStopIndCirclePath, mStopIndCirclePaint);

        // Draw text
        mStopIndTextPaint.setColor(mStopIndTextColor);
        Rect textBounds = new Rect();
        mStopIndTextPaint.getTextBounds("2", 0, 1, textBounds);
        canvas.drawText("2", mXStopIndicator-textBounds.width()/2,
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

    private boolean isCursorInStartIndCatchArea(float x, float y) {
        final float dX = x - mXStartIndicator;
        final float dY = y - mYStartIndicator;
        return (Math.sqrt(dX*dX + dY*dY) <= mStartIndCircleMoveCatchRadius);
    }

    private boolean isCursorInStopIndCatchArea(float x, float y) {
        final float dX = x - mXStopIndicator;
        final float dY = y - mYStopIndicator;
        return (Math.sqrt(dX*dX + dY*dY) <= mStopIndCircleMoveCatchRadius);
    }
}
