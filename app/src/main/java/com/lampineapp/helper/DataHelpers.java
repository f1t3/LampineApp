package com.lampineapp.helper;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public class DataHelpers {

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

    public static int angleToSpectrumColor(float angle_deg) {
        final float a = angle_deg;
        // Calculate RGB values scaled from 0 to 1 as linear spectrum
        float r = 0, g = 0, b = 0;
        if (a < 0) {
            // Dummy
        } else if (a >= 0 && a < 60) {
            r = 1;
            g = a / 60;
        } else if (a >= 60 && a < 120) {
            r = 1 - (a - 60) / 60;
            g = 1;
        } else if (a >= 120 && a < 180) {
            g = 1;
            b = (a - 120) / 60;
        } else if (a >= 180 && a < 240) {
            g = 1 - (a - 180) / 60;
            b = 1;
        } else if (a >= 240 && a < 300) {
            r = (a - 240) / 60;
            b = 1;
        } else if (a >= 300 && a <= 360) {
            r = 1;
            b = 1 - (a - 300) / 60;
        }
        final int A = 255;
        final int R = (int) (255 * r);
        final int G = (int) (255 * g);
        final int B = (int) (255 * b);
        final int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        return color;
    }

}
