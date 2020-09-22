package com.lampineapp.graphics.colorgraphinputview;

import com.lampineapp.helper.DataHelpers;

public class Helpers {

    protected boolean isCursorInsideRadius(float x, float y, float xC, float yC, float R) {
        final float dX = x - xC;
        final float dY = y - yC;
        if (dX*dX + dY*dY <= R*R) {
            return true;
        }
        return false;
    }




}
