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
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.constraintlayout.solver.widgets.Helper;

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
    private Paint circlePaint;
    private Path circlePath;

    private float mX, mY;
    private int mColor = 0;

    // Tolerance must lead to worst case number of 100 datapoints, otherwise dataset is to large
    // for MCU
    private static final float TOUCH_TOLERANCE = 10;
    private static final int COLOR_TOLERANCE = 16;

    // TODO: IMPLEMENT PROPER CLASS
    private List<Integer> colors = new ArrayList<>();

    public ColorGraphInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
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
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        final float angle = 360 * y / height;
        colors.clear();
        int mColor = DataHelpers.angleToSpectrumColor(angle);
    }

    private void onTouchMove(float x, float y) {

        final float dX = x - mX;
        final float dY = y - mY;
        // Allow only forward movements in x direction
        if (x <= mX)
            return;

        if (Math.abs(dX) >= TOUCH_TOLERANCE || Math.abs(dY) >= TOUCH_TOLERANCE) {
            // Update color based on y position
            final float angle = 360 * y / height;
            int color = DataHelpers.angleToSpectrumColor(angle);
            if (Math.abs(DataHelpers.getColorR(color) - DataHelpers.getColorR(mColor)) > COLOR_TOLERANCE ||
                    Math.abs(DataHelpers.getColorG(color) - DataHelpers.getColorG(mColor)) > COLOR_TOLERANCE ||
                    Math.abs(DataHelpers.getColorB(color) - DataHelpers.getColorB(mColor)) > COLOR_TOLERANCE
            ) {
                mColor = color;
                mPaint.setColor(color);
                // TODO: USE LIST??
                colors.add(color);
                mPath.moveTo(mX, mY);
                mPath.lineTo(x, y);
                mX = x;
                mY = y;

                // commit the path to our offscreen
                mCanvas.drawPath(mPath, mPaint);
                // kill this so we don't double draw
                mPath.reset();

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }
    }

    private void onTouchUp(float x, float y) {
        mPath.moveTo(x, y);
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        Log.d(TAG, "Data Points: " + colors.size());
    }

    private void clearDrawing() {
        setDrawingCacheEnabled(false);
        onSizeChanged(width, height, width, height);
        invalidate();
        setDrawingCacheEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(circlePath, circlePaint);
    }
}
