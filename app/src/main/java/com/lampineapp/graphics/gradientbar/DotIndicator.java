package com.lampineapp.graphics.gradientbar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class DotIndicator {

    private Bitmap mBmp;
    private Paint mFillingPaint = new Paint();
    private Paint mOutlinePaint = new Paint();

    private int mW = 100;
    private int mDx = 0;
    private int mCInner = Color.RED;
    private int mCOuter = Color.BLACK;
    protected int mWOutline = 8;
    private int mOutlineColor = Color.BLACK;
    private int[] mFillingColors = {0,0};


    public DotIndicator() {
        mBmp = Bitmap.createBitmap(mW, mW, Bitmap.Config.ARGB_8888);
    }

    protected void draw(Canvas canvas){
        canvas.drawBitmap(mBmp, 0, 0, new Paint());
        drawFilling(canvas);
        drawOutline(canvas);
    }

    protected void onSizeChanged(int w, int h) {
        mW = w;
        if (mW > 0) {
            mBmp = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        } else {
            mBmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

    protected DotIndicator setTranslationRight(int dx) {
        mDx = dx;
        return this;
    }

    protected void setGradientColorRange(int inner, int outer) {
        mCInner = inner;
        mCOuter = outer;
    }

    private void drawOutline(Canvas canvas) {
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        mOutlinePaint.setStrokeWidth(mWOutline);
        mOutlinePaint.setColor(mOutlineColor);
        drawIndicatorOutline(canvas, mOutlinePaint);
    }

    private void drawFilling(Canvas canvas) {
        mFillingPaint.setStyle(Paint.Style.FILL);
        mFillingPaint.setShader(new RadialGradient(mW/2+mDx+mWOutline/2, mW/2, mW/2+mWOutline, mCInner, mCOuter, Shader.TileMode.MIRROR));
        mFillingPaint.setStrokeCap(Paint.Cap.ROUND);
        mFillingPaint.setStrokeWidth(10);
        drawIndicatorOutline(canvas, mFillingPaint);

    }

    private void drawIndicatorOutline(Canvas canvas, Paint paint) {
        canvas.drawRoundRect(mWOutline/2 + mDx, mWOutline/2, mW - mWOutline/2 + mDx, mW - mWOutline/2, mW /2, mW /2,paint);
    }
}
