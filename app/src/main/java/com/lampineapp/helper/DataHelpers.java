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
}
