package com.lampineapp.graphics.gradientbar;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class GradientSliderBar extends View {

    private final static String TAG = GradientSliderBar.class.getSimpleName();

    private int mH = 100,  mW = 100;
    private double mProgress = 0.5;
    private int[] mColors = {0,0};
    private Canvas mCanvas;
    private GradientBarFrame mOutlineFrame;
    private GradientBarFrame mProgressFrame;
    private DotIndicator mIndicator;

    public GradientSliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCanvas= new Canvas();
        mOutlineFrame = new GradientBarFrame();
        mProgressFrame = new GradientBarFrame();
        mProgressFrame.setFillingGradientColors(new int[]{Color.YELLOW, Color.RED});
        mIndicator = new DotIndicator();
        mIndicator.setGradientColorRange(Color.RED, 0xB0000000);
    }

    public GradientSliderBar setGradientColorRange(int[] colors) {
        mColors = colors;
        setProgress(mProgress);
        return this;
    }

    public GradientSliderBar setProgress(double progress) {
        if (progress > 1) {
            progress = 1;
        }
        mProgress = progress;
        // TODO: Search for correct interval in colors array
        int colorAtProgress = (int) new ArgbEvaluator().evaluate((float)mProgress, mColors[0], mColors[1]);
        mProgressFrame.setFillingGradientColors(new int[]{mColors[0], colorAtProgress});
        mProgressFrame.onSizeChanged  ((int)(mH + (mW-mH)*mProgress), mH);
        mIndicator.setTranslationRight((int)((mW-mH)*mProgress));
        mIndicator.setGradientColorRange(Color.YELLOW, colorAtProgress);
        return this;
    }

    public GradientSliderBar setOutlineWidth(int width) {
        mOutlineFrame.OUTLINE_W = width;
        mProgressFrame.OUTLINE_W = width;
        mIndicator.mWOutline = width;
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
        mProgressFrame.onSizeChanged(  (int)(mH + (mW-mH)*mProgress), h);
        mIndicator.setTranslationRight((int)((mW-mH)*mProgress));
        mIndicator.onSizeChanged(h, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mProgressFrame.draw(canvas);
        mOutlineFrame.draw(canvas);
        mIndicator.draw(canvas);
    }

}
