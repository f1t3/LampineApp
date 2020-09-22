package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;

public class CurvePointIndicator {
    
    // Parameter
    protected float X = 0, Y = 0;
    protected float MOV_IND_RADIUS = 150;
    protected int COLOR = Color.BLACK;
    protected float RADIUS = 30;
    protected float TEXT_SIZE = 40;
    protected int TEXT_COLOR = Color.WHITE;
    protected String LABEL = "0";
    protected float CATCH_RADIUS = 20;
    
    // Members
    private Paint mMovIndPaint = new Paint();
    private Paint mIndPaint = new Paint();
    private Paint mTextPaint = new Paint();
    
    // Control
    enum State {MOVING}
    State mState;
    
    protected CurvePointIndicator(String label) {
        LABEL = label;
    }
    
    protected void draw(Canvas canvas) {

        // Draw moving indicator
        if (mState == State.MOVING) {
            final int colors[] = {InputFrame.getColorFromY(Y), Color.TRANSPARENT};
            final float positions[] = {0, 1};
            final RadialGradient grad = new RadialGradient(
                    X, Y, MOV_IND_RADIUS, colors, positions, Shader.TileMode.CLAMP);
            mMovIndPaint.setShader(grad);
            canvas.drawCircle(X,Y, MOV_IND_RADIUS, mMovIndPaint);
        }

        // Draw circle
        mIndPaint.setColor(COLOR);
        canvas.drawCircle(X, Y , RADIUS, mIndPaint);

        // Draw text
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setColor(TEXT_COLOR);
        Rect r = new Rect();
        mTextPaint.getTextBounds(LABEL, 0, 1, r);
        canvas.drawText(LABEL, X - r.width()/2, Y + r.height()/2, mTextPaint);
    }

    protected boolean isOnCatchArea(float x, float y) {
        if (Helpers.isInsideRadius(x, y, X, Y, CATCH_RADIUS))
            return true;
        return false;
    }

    protected void setPos(float x, float y) {
        X = x;
        Y = y;
    }

}
