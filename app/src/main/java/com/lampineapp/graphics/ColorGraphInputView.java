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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lampineapp.helper.DataHelpers;

import java.util.ArrayList;
import java.util.List;

public class ColorGraphInputView extends View {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    private Paint mPaint;
    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint mIndicatorPaint;
    private Paint mFramePaint;
    private Paint mIndicatorCirclePaint;
    private Path mIndicatorPath;
    private Path mFramePath;
    private Path mIndicatorCirclePath;

    private float mX, mY;
    private int mColor = 0;

    private boolean graphIsEmpty = true;

    // Settable parameters graph
    private float mGraphLineWidth = 10;

    // Settable parameters frame
    private float mFrameWidth = 5;
    private int mFrameColor = Color.BLACK;

    // Settable parameters indicator
    private float mIndicatorLineWidth = 4;
    private float mIndicatorCircleLineWidth = 40;
    private float mIndicatorRadius = 20;
    private int mIndicatorColor = Color.BLACK;

    // Settable parameters background
    private float mBackgroundGradientLineWidth = 10;
    private int   mBackgroundGradientAlpha = 30;

    // Tolerances
    private static final float TOUCH_TOLERANCE = 4;
    private static final int COLOR_TOLERANCE = 0;

    // TODO: IMPLEMENT PROPER CLASS
    private List<Integer> colors = new ArrayList<>();

    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mFramePath = new Path();
        mFramePaint = new Paint();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(mFrameWidth);
        mFramePaint.setStrokeCap(Paint.Cap.ROUND);

        mIndicatorPaint = new Paint();
        mIndicatorCirclePaint = new Paint();
        mIndicatorPath = new Path();
        mIndicatorCirclePath = new Path();
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeJoin(Paint.Join.MITER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        drawBackgroundGradient();
        drawFrame(width, height);
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
        // Remove old line on new touchdown
        clearDrawing();
        mPath.reset();

        mX = correctXToDrawingArea(x);
        mY = correctYToDrawingArea(y);

        colors.clear();
    }

    private void onTouchMove(float x, float y) {

        // Correct x or y if on or outside frame
        x = correctXToDrawingArea(x);
        y = correctYToDrawingArea(y);

        // Allow only forward movements in x direction
        final float dX = x - mX;
        final float dY = y - mY;
        if (x < mX)
            return;

        if (Math.abs(dX) >= TOUCH_TOLERANCE || Math.abs(dY) >= TOUCH_TOLERANCE) {

            // Update color based on y position
            final float angle = 360 * y / height;
            int color = DataHelpers.angleToSpectrumColor(angle);
            mColor = color;
            colors.add(color);
            mPaint.setColor(color);
            mPaint.setStrokeWidth(mGraphLineWidth);

            mPath.moveTo(mX, mY);
            mPath.lineTo(x, y);
            mX = x;
            mY = y;

            // Patch must be drawn directly for using different colors
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();

            drawIndicator(x, y);
        }
    }

    private void onTouchUp(float x, float y) {
        mIndicatorPath.reset();
        mIndicatorCirclePath.reset();

        mX = 0;
        mY = 0;
        Log.d(TAG, "Data Points: " + colors.size());
    }

    private boolean isXLeftOfFrame(float x) {
        if (x - mGraphLineWidth/2 < mFrameWidth/2)
            return true;
        else
            return false;
    }

    private boolean isXRightOfFrame(float x) {
        if (x + mGraphLineWidth/2 > width  - mFrameWidth/2)
            return true;
        else
            return false;
    }

    private float correctXToDrawingArea(float x) {
        // Correct x if on or outside frame
        if (isXLeftOfFrame(x))
            x = mGraphLineWidth/2 + mFrameWidth/2;
        if (isXRightOfFrame(x))
            x = width  - mGraphLineWidth/2 - mFrameWidth/2;
        return x;
    }

    private boolean isYTopOfFrame(float y) {
        if (y - mGraphLineWidth/2 < mFrameWidth/2)
            return true;
        else
            return false;
    }

    private boolean isYBotOfFrame(float y) {
        if (y + mGraphLineWidth/2 > height - mFrameWidth/2)
            return true;
        else
            return false;
    }

    private float correctYToDrawingArea(float y) {
        // Correct y if on or outside frame
        if (isYTopOfFrame(y))
            y = mGraphLineWidth/2 + mFrameWidth/2;
        if (isYBotOfFrame(y))
            y = height - mGraphLineWidth/2 - mFrameWidth/2;
        return y;
    }

    private void clearDrawing() {
        setDrawingCacheEnabled(false);
        onSizeChanged(width, height, width, height);
        invalidate();
        setDrawingCacheEnabled(true);
    }

    private void drawFrame(float width, float height) {
        mFramePaint.setStrokeWidth(mFrameWidth);
        mFramePaint.setColor(mFrameColor);

        final float x0 = mFrameWidth/2;
        final float x1 = width - mFrameWidth/2;
        final float y0 =  mFrameWidth/2;
        final float y1 = height - mFrameWidth/2;

        mFramePath.reset();
        mFramePath.moveTo(x0, y0);
        mFramePath.lineTo(x1, y0);
        mFramePath.lineTo(x1, y1);
        mFramePath.lineTo(x0, y1);
        mFramePath.lineTo(x0, y0);
    }

    private void drawBackgroundGradient() {
        mPaint.setStrokeWidth(mBackgroundGradientLineWidth);
        float y = mBackgroundGradientLineWidth/2;
        for ( ; y < height; y += mBackgroundGradientLineWidth) {
            int color = DataHelpers.angleToSpectrumColor(360 * y/height);
            color = DataHelpers.setA(color, mBackgroundGradientAlpha);
            mPaint.setColor(color);
            mCanvas.drawLine(0, y, width, y, mPaint);
        }
        // Redraw last line with thicker stroke if necessary
        if (y + mBackgroundGradientLineWidth/2 < height) {
            mPaint.setStrokeWidth((height - y) * 2);
            mCanvas.drawLine(0, y, width, y, mPaint);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mIndicatorPath, mIndicatorPaint);
        canvas.drawPath(mIndicatorCirclePath, mIndicatorCirclePaint);
        canvas.drawPath(mFramePath, mFramePaint);
    }
}
