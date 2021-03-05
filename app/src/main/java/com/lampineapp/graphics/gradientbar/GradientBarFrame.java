package com.lampineapp.graphics.gradientbar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.shapes.RoundRectShape;

public class GradientBarFrame {

    private int mW = 100;
    private int mH = 100;


    protected int OUTLINE_W = 8;
    protected int OUTLINE_COLOR = Color.BLACK;

    private Bitmap mBmp;
    private Paint mOutlinePaint = new Paint();
    private Paint mFillPaint = new Paint();
    private int[] mFillingColors = {0,0};

    public GradientBarFrame() {
        mBmp = Bitmap.createBitmap(mW, mH, Bitmap.Config.ARGB_8888);
    }

    protected void draw(Canvas canvas){
        canvas.drawBitmap(mBmp, 0, 0, new Paint());
        drawFilling(canvas);
        drawOutline(canvas);
    }

    protected void setFillingGradientColors(int[] colors) {
        if (colors.length > 1) {
            mFillingColors = colors;
        } else {
            mFillingColors = new int[]{colors[0], colors[0]};
        }

    }

    private void drawOutline(Canvas canvas) {
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        mOutlinePaint.setStrokeWidth(OUTLINE_W);
        mOutlinePaint.setColor(OUTLINE_COLOR);
        canvas.drawRoundRect(OUTLINE_W/2, OUTLINE_W/2, mW -OUTLINE_W/2, mH -OUTLINE_W/2, mH /2, mH /2,mOutlinePaint);
    }

    private void drawFilling(Canvas canvas) {
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setShader(new LinearGradient(0, 0, mW, 0, mFillingColors, null, Shader.TileMode.MIRROR));
        mFillPaint.setStrokeCap(Paint.Cap.ROUND);
        mFillPaint.setStrokeWidth(0);
        canvas.drawRoundRect(OUTLINE_W/2, OUTLINE_W/2, mW -OUTLINE_W/2, mH -OUTLINE_W/2, mH /2, mH /2,mFillPaint);
    }

        protected void onSizeChanged(int w, int h) {
            if (w < h) {
                w = h;
            }
            mW = w;
            mH = h;
            if (mW > 0) {
                mBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        CANVAS = new Canvas(mBmp);
//        drawBackgroundGradient();
            } else {
                mBmp = Bitmap.createBitmap(1, h, Bitmap.Config.ARGB_8888);
            }
        }

    protected Bitmap getBitmap() {
        return mBmp;
    }

    protected Paint getPaint() {
        return mOutlinePaint;
    }
}
