package com.lampineapp.graphics.colorgraphinputview;

import com.lampineapp.helper.DataHelpers;

public class GraphInputArea {

    // Padding
    protected static float padT = 50;
    protected static float padB = 50;
    protected static float padL = 50;
    protected static float padR = 50;

    // Dimensions
    protected static int colorOffset = 0;
    protected static int height, width;


    protected int getCurrentColorFromYPos(float y) {
        return DataHelpers
                .getSpectrumColorFromRelative((y-padT+colorOffset) / (height-padT-padB));
    }
}
