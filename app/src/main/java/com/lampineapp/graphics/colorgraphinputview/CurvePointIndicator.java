package com.lampineapp.graphics.colorgraphinputview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;

public class CurvePointIndicator {
    
    private float mX = 0, mY = 0;
    enum State {
        MOVING,

    }
    State mState;

    protected void drawStartInd(Canvas canvas) {
        final float x0 = mX;
        final float y0 = mY;

        // Draw moving indicator
        if (mState == State.MOVING) {
            final int colors[] = {getCurrentColorFromYPos(y0), Color.TRANSPARENT};
            final float positions[] = {0, 1};
            final RadialGradient grad = new RadialGradient(
                    x0, y0, mStartIndMovingIndRadius, colors, positions, Shader.TileMode.CLAMP);
            mStartIndMovCirclePaint.setShader(grad);
            canvas.drawCircle(x0,y0, mStartIndMovingIndRadius, mStartIndMovCirclePaint);
        }

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
}
