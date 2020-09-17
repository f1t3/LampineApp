package com.lampineapp.helper;

import android.util.Log;

import com.lampineapp.graphics.ColorGraphInputView;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public class DataHelpers {

    private final static String TAG = ColorGraphInputView.class.getSimpleName();

    public static int[] parseStringArrayToIntArray(String[] stringArray) {
        int[] ret = new int[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            ret[i] = Integer.parseInt(stringArray[i]);
        }
        return ret;
    }

    public static float getMinValue(float[] array) {
        float minValue = array[0];;
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    public static float getMaxValue(float[] array) {
        float maxValue = array[0];;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    public static float[] getMovingAverage(float[] array, int n) {
        if (n > floor(array.length/2)) {
            return array;
        }
        float ret[] = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            int x0 = (int)(i - floor(n/2));
            int x1 = (int)(i + floor(n/2));
            if (x0 < 0) {
                x1 = x1 + abs(x0);
                x0 = 0;
            }
            if (x1 >= array.length) {
                x0 = x0 - (x1-array.length-1);
                x1 = array.length-1;
            }
            float sum = 0;
            for (int j = x0; j <= x1; j++) {
                sum += array[j];
            }
            sum = sum / (x1-x0+1);
            ret[i] = sum;
        }
        return ret;
    }

    public static int getSpectrumColorFromAngle(float angle_deg) {
        final float r = angle_deg / 360;
        return getSpectrumColorFromRelative(r);
    }

    public static int getSpectrumColorFromRelative(float a) {
        while (a > 1)
            a -= 1;
        while  (a < 0)
            a += 1;

        // Calculate RGB values scaled from 0 to 1 as linear spectrum
        float r = 0f, g = 0f, b = 0f;
        if (a < 0f) {
            // Dummy
        } else if (a >= 0f/6f && a < 1f/6f) {
            r = 1f;
            g = a * 6f;
        } else if (a >= 1f/6f && a < 2f/6f) {
            r = 1f - (a - 1f/6f) * 6f;
            g = 1f;
        } else if (a >= 2f/6f && a < 3f/6f) {
            g = 1f;
            b = (a - 2f/6f) * 6f;
        } else if (a >= 3f/6f && a < 4f/6f) {
            g = 1f - (a - 3f/6f) * 6f;
            b = 1f;
        } else if (a >= 4f/6f && a < 5f/6f) {
            r = (a - 4f/6f) * 6f;
            b = 1f;
        } else if (a >= 5f/6f && a <= 6f/6f) {
            r = 1f;
            b = 1f - (a - 5f/6f) * 6f;
        }
        final int A = 255;
        final int R = (int) (255f * r);
        final int G = (int) (255f * g);
        final int B = (int) (255f * b);
        final int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        return color;
    }

    public static int getR(int color) {
        return (color >> 16) & 0xff;
    }
    public static int getG(int color) {
        return (color >> 8) & 0xff;
    }
    public static int getB(int color) {
        return (color >> 0) & 0xff;
    }
    public static int setA(int color, int A) {
        int ret = color & ~(0xFF << 24);
        ret |= (A & 0xFF) << 24;
        return ret;
    }
}
