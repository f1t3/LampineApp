package com.lampineapp.graphics.colorgraphinputview;

public class Helpers {

    protected static boolean isInsideRadius(float x, float y, float xC, float yC, float R) {
        final float dX = x - xC;
        final float dY = y - yC;
        if (dX*dX + dY*dY <= R*R) {
            return true;
        }
        return false;
    }




}
