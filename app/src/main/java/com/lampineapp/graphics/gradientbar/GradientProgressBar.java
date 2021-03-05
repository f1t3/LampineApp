package com.lampineapp.graphics.gradientbar;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class GradientProgressBar extends View {

    private final static String TAG = GradientProgressBar.class.getSimpleName();

    private int mH = 100,  mW = 100;
    private double mProgress = 0.5;
    private int[] mColors = {0,0};
    private Canvas mCanvas;
    private GradientBarFrame mOutlineFrame;
    private GradientBarFrame mProgressFrame;

    public GradientProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCanvas= new Canvas();
        mOutlineFrame = new GradientBarFrame();
        mProgressFrame = new GradientBarFrame();
    }

    public GradientProgressBar setGradientColorRange(int[] colors) {
        mColors = colors;
        setProgress(mProgress);
        return this;
    }
    
    public GradientProgressBar setProgress(double progress) {
        if (progress > 1) {
            progress = 1;
        }
        mProgress = progress;
        // TODO: Search for correct interval in colors array
        int colorAtProgress = (int) new ArgbEvaluator().evaluate((float)mProgress, mColors[0], mColors[1]);
        mProgressFrame.setFillingGradientColors(new int[]{mColors[0], colorAtProgress});
        mProgressFrame.onSizeChanged((int)(mH + (mW-mH)*mProgress), mH);
        invalidate();
        return this;
    }

    public GradientProgressBar setOutlineWidth(int width) {
        mOutlineFrame.OUTLINE_W = width;
        mProgressFrame.OUTLINE_W = width;
        return this;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w < h) {
            w = h;
        }
        mW = w;
        mH = h;
        super.onSizeChanged(w, h, oldw, oldh);
        mOutlineFrame.onSizeChanged(w, h);
        mProgressFrame.onSizeChanged((int)(h + (w-h)*mProgress), h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mProgressFrame.draw(canvas);
        mOutlineFrame.draw(canvas);
    }

}

