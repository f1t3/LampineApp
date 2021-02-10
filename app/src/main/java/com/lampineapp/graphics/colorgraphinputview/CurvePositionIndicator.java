package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class CurvePositionIndicator {

    // TODO: Indicator in current color? Maybe with fading edge?

    /***********************************************************************************************
     *
     * Members
     */

    // Parameter
    protected float X = 0, Y = 0;
    protected float IND_LINE_W = 4;
    protected float RADIUS = 10;
    protected int IND_COLOR = Color.BLACK;

    private Paint mIndPaint = new Paint();
    private Paint mIndLinePaint = new Paint();
    private boolean mVisible = false;

    /***********************************************************************************************
     *
     * Public Methods
     */
    public CurvePositionIndicator() {
        mIndPaint.setStyle(Paint.Style.STROKE);
        mIndPaint.setStyle(Paint.Style.FILL);
    }

    /***********************************************************************************************
     *
     * Protected Methods
     */
    protected void setPos(float x, float y) {
        X = x;
        Y = y;
    }

    protected void draw(Canvas canvas) {
        if (!mVisible)
            return;
        // Draw vertical and horizontal lines
        mIndLinePaint.setStrokeWidth(IND_LINE_W);
        mIndLinePaint.setColor(IND_COLOR);
        canvas.drawLine(X, InputFrame.getY0(), X, InputFrame.getY1(), mIndLinePaint);
        canvas.drawLine(InputFrame.getX0(), Y, InputFrame.getX1(), Y, mIndLinePaint);

        // Draw circle
        mIndPaint.setColor(IND_COLOR);
        canvas.drawCircle(X, Y, RADIUS, mIndPaint);
    }

    protected void hide() {
        mVisible = false;
    }

    protected void show() {
        mVisible = true;
    }
}
