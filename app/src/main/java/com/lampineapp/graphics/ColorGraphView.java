package com.lampineapp.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.provider.SyncStateContract;
import android.util.AttributeSet;
import android.view.View;

import com.lampineapp.R;
import com.lampineapp.helper.DataHelpers;

import java.util.Arrays;

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
        // Span of yMax - yMin in px
        final float ySpan = (this.getMeasuredHeight() - 2*STROKE_WIDTH) /
                (DataHelpers.getMaxValue(mY) - DataHelpers.getMinValue(mY));
        // Space between xPoints in px
        float xSpace = this.getMeasuredWidth() / mX.length;
        // Draw each line segment
        for (int i = 0; i < mX.length-1; i++) {

            // TODO: UGLY CORNERS:
            // You probably don't want to lineTo(c, d) and then immediately moveTo(c, d) which is the same point. 
            // If you do this, you won't get a nice corner join on the two line segments, 
            // which may look like an ugly gap. !!!
            Path path = new Path();
            Paint mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(STROKE_WIDTH);
            float x0 = mX[i] * xSpace + STROKE_WIDTH;
            float x1 = mX[i+1] * xSpace + STROKE_WIDTH;
            float y0 = mY[i] * ySpan;
            float y1 = mY[i+1] * ySpan;
            // TODO: ACCEPT ACTUAL COLORS ONLY AND REMOVE GETRESOURCES().GETCOLOR();
            int color1 = getResources().getColor(mColor[i]);
            int color2 = getResources().getColor(mColor[i+1]);
            // Use only in first run to enable smooth corners
            if (i == 0) {
                path.moveTo(x0, y0);
            }
            path.lineTo(x1, y1);
            mPaint.setShader(new LinearGradient(x0, y0, x1, y1, color1, color2, Shader.TileMode.CLAMP));
            canvas.drawPath(path, mPaint);
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