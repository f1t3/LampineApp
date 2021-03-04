package com.lampineapp.frag_configure_lamp;

import java.util.Arrays;

import static com.lampineapp.helper.DataHelpers.parseStringArrayToIntArray;

public class LampModesSynchronizer {
//    public FragmentConfigureLampModes.LampModeConfigurationItem[] parseLampModeConfigsFromString(String str) {
//        // Delimiters
//        final String MODE_DELIM = "\n";
//        final String VALUE_DELIM = ",";
//        final String END_OF_DATA = "\r";
//        // Position of values in config-CSV
//        final int NAME_POS = 0;
//        final int CURRENT_POS = 1;
//        final int PERIOD_PER_POINT_MS_POS = 2;
//        final int N_RGBW_POINTS = 3;
//        final int START_RGBW_POINTS = 4;
//
//        final String[] configArray = str.split(MODE_DELIM);
//        FragmentConfigureLampModes.LampModeConfigurationItem[] ret = new FragmentConfigureLampModes.LampModeConfigurationItem[configArray.length];
//
//        for (int i = 0; i < configArray.length; i++) {
//            if (configArray[i].contains(END_OF_DATA))
//                break;
//            String[] valueArray = configArray[i].split(VALUE_DELIM);
//            FragmentConfigureLampModes.LampModeConfigurationItem configurationItem = new FragmentConfigureLampModes.LampModeConfigurationItem();
//            configurationItem.setName(valueArray[NAME_POS]);
//            configurationItem.setCurrent(valueArray[CURRENT_POS]);
//            configurationItem.setPeriodPerPoint_ms(Integer.parseInt(valueArray[PERIOD_PER_POINT_MS_POS]));
//            int nRgbwPoints = Integer.parseInt(valueArray[N_RGBW_POINTS]);
//            configurationItem.setNRgbwPoints(nRgbwPoints);
//
//            // Parse RGBW points subarrays
//            int[][] colorMatrix = new int[4][N_RGBW_POINTS];
//            for (int colorIndex = 0; colorIndex < 4; colorIndex++) {
//                final int startIndex = START_RGBW_POINTS + colorIndex * nRgbwPoints;
//                final int stopIndex = START_RGBW_POINTS + (colorIndex + 1) * nRgbwPoints;
//                final String[] colorValueArray = Arrays.
//                        copyOfRange(valueArray, startIndex, stopIndex - 1);
//                colorMatrix[colorIndex] = parseStringArrayToIntArray(colorValueArray);
//            }
//            configurationItem.setRPoints(colorMatrix[0]);
//            configurationItem.setGPoints(colorMatrix[1]);
//            configurationItem.setBPoints(colorMatrix[2]);
//            configurationItem.setWPoints(colorMatrix[3]);
//            ret[i] = configurationItem;
//        }
//        return ret;
//    }

}
