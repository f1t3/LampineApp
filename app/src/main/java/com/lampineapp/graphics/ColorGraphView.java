package com.lampineapp.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.lampineapp.helper.DataHelpers;


public class ColorGraphView extends View {
    float mX[];
    float mY[];
    int mColor[];

    public ColorGraphView(Context context, AttributeSet attrs) {
        super (context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        final float STROKE_WIDTH = 15;
        // Scalers: px per datavalue. Scale data to fill graph vertically / horizontally.
        final float yScaler = (this.getMeasuredHeight() - 2*STROKE_WIDTH) /
                (DataHelpers.getMaxValue(mY) - DataHelpers.getMinValue(mY));
        float xScaler = (this.getMeasuredWidth() - 2*STROKE_WIDTH) / (mX.length - 1);
        // Draw each line segment
        for (int i = 0; i < mX.length-1; i++) {
            Path path = new Path();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
            final float x0 = mX[i] * xScaler + STROKE_WIDTH;
            final float x1 = mX[i+1] * xScaler + STROKE_WIDTH;
            final float y0 = mY[i] * yScaler + STROKE_WIDTH;
            final float y1 = mY[i+1] * yScaler + STROKE_WIDTH;
            final int color1 = mColor[i];
            final int color2 = mColor[i+1];
            path.moveTo(x0, y0);
            path.lineTo(x1, y1);
            paint.setShader(
                    new LinearGradient(x0, y0, x1, y1, color1, color2, Shader.TileMode.CLAMP));
            canvas.drawPath(path, paint);
        }
    }

    public void setData(float yPoints[]) {
        mY = yPoints;
        // Assume all x equidistant
        mX = new float[yPoints.length];
        for (int i = 0; i < yPoints.length; i++) {
            mX[i] = i;
        }
    }

    public void setColor(int color[]) {
        mColor = color;
    }
}