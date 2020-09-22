package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.lampineapp.helper.DataHelpers;

public class InputFrame {

    // Parameter
    protected static float GRAPHLINE_W = 18;
    protected static float PAD_T = 50;
    protected static float PAD_B = 50;
    protected static float PAD_L = 50;
    protected static float PAD_R = 50;
    protected static int COLOR_OFFSET = 0;
    protected static float OUTLINE_W = 5;
    protected static int OUTLINE_COLOR = Color.BLACK;
    protected static float GRAD_LINE_W = 10;
    protected static int GRAD_ALPHA = 50;

    // Members
    protected static Canvas CANVAS;
    private static Bitmap mBmp;
    private static int mH = 0, mW = 0;
    private static Paint mGradPaint = new Paint();
    private static Paint mOutlinePaint = new Paint();


    protected static int getColorFromY(float y) {
        // TODO: CUT Y TO FRAME
        return DataHelpers
                .getSpectrumColorFromRelative((y-PAD_T+COLOR_OFFSET) / (mH-PAD_T-PAD_B));
    }

    protected static void clearFrame() {
        // Bitmap needed for graph
        // TODO: SHORTEN METHOD? WHAT IS REALLY NECESSARY FOR CLEAR?
        mBmp = Bitmap.createBitmap(mW, mH, Bitmap.Config.ARGB_8888);
        CANVAS = new Canvas(mBmp);
        drawBackgroundGradient();
    }

    protected static boolean isXLeftOfFrame(float x) {
        if (x - GRAPHLINE_W/2 < OUTLINE_W + PAD_L) return true;
        return false;
    }
    protected static boolean isXRightOfFrame(float x) {
        if (x + GRAPHLINE_W/2 > mW - OUTLINE_W - PAD_L) return true;
        return false;
    }
    protected static float cutX(float x) {
        // Correct x if on or outside frame
        if (isXLeftOfFrame(x)) x = GRAPHLINE_W/2 + OUTLINE_W + PAD_L;
        if (isXRightOfFrame(x)) x = mW - GRAPHLINE_W/2 - OUTLINE_W - PAD_R;
        return x;
    }
    protected static boolean isYTopOfFrame(float y) {
        if (y - GRAPHLINE_W/2 < OUTLINE_W /2 + PAD_T) return true;
        return false;
    }
    protected static boolean isYBotOfFrame(float y) {
        if (y + GRAPHLINE_W/2 > mH - OUTLINE_W - PAD_B - GRAPHLINE_W/2) return true;
        return false;
    }

    protected static float cutY(float y) {
        // Correct y if on or outside frame
        if (isYTopOfFrame(y)) y = GRAPHLINE_W/2 + OUTLINE_W /2 + PAD_T;
        if (isYBotOfFrame(y)) y = mH - GRAPHLINE_W/2 - OUTLINE_W - PAD_B;
        return y;
    }

    protected static void scrollBackground(float x, float y, float dY) {
        if (Helpers.isInsideRadius(x, y, getXCenter(), getYCenter(), mH)) {
            clearFrame();
            COLOR_OFFSET += dY;
        }
    }

    protected static void onSizeChanged(int w, int h) {
        mW = w;
        mH = h;
        mBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        CANVAS = new Canvas(mBmp);
        drawBackgroundGradient();
    }

    // TODO: LINEWIDTH?
    protected static float getXCenter() { return PAD_L + getFrameW() / 2; }
    // TODO: LINEWIDTH?
    protected static float getYCenter() { return PAD_T + getFrameH() / 2; }
    protected static float getX0() { return PAD_L + OUTLINE_W / 2; }
    protected static float getX1() { return mW - OUTLINE_W / 2 - PAD_R; }
    protected static float getY0() { return PAD_T + OUTLINE_W / 2; }
    protected static float getY1() { return mH - OUTLINE_W / 2 - PAD_B; }
    protected static float getFrameW() { return mW - PAD_R - PAD_R; }
    protected static float getFrameH() { return mH - PAD_T - PAD_B; }

    protected static float getRelXPos(float x) {
        return (cutX(x) - PAD_L - OUTLINE_W) / getFrameW();
    }

    protected static void draw(Canvas canvas){
        canvas.drawBitmap(mBmp, 0, 0, new Paint());
        drawOutline(canvas);
    }


    /***********************************************************************************************
     *
     * Private methods
     */
    // TODO: POSSIBLE TO USE DYNAMIC CANVAS PASSED AS ARGUMENT?
    private static void drawBackgroundGradient() {
        mGradPaint.setStrokeCap(Paint.Cap.BUTT);
        mGradPaint.setStrokeWidth(GRAD_LINE_W);

        // First line edge directly at top frame outline
        float y = getY0() + GRAD_LINE_W / 2 ;
        // Last line edge not lower than bottom frame outline
        final float yStop = getY1() - GRAD_LINE_W / 2;

        // Draw line after line with colors in respect to y position in frame
        for ( ; y <= yStop; y += GRAD_LINE_W) {
            int color = getColorFromY(y);
            mGradPaint.setColor(DataHelpers.setA(color, GRAD_ALPHA));
            CANVAS.drawLine(getX0(), y, getX1(), y, mGradPaint);
        }

        // If last line edge is not directly at bottom border frame outline,
        // draw a final line that exactly fills the space left

        // Revert last increment
        y -= GRAD_LINE_W;
        // Free space height
        final float freeSpace = (mH - PAD_B - OUTLINE_W) - (y + GRAD_LINE_W/2);
        if (freeSpace <= 0)
            return;

        // Set y to middle of free space between frame outline and last line edge
        y = y + GRAD_LINE_W / 2 + freeSpace / 2;
        // Calculate color and draw line
        int color = getColorFromY(y);
        mGradPaint.setColor(DataHelpers.setA(color, GRAD_ALPHA));
        mGradPaint.setStrokeWidth(freeSpace);
        CANVAS.drawLine(getX0(), y,  getX1(), y, mGradPaint);
    }

    private static void drawOutline(Canvas canvas) {
        // Frame
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        mOutlinePaint.setStrokeWidth(OUTLINE_W);
        mOutlinePaint.setColor(OUTLINE_COLOR);
        canvas.drawRect(getX0(), getY0(), getX1(), getY1(), mOutlinePaint);
    }

}
