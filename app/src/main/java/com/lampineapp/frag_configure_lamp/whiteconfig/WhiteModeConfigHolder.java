package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class WhiteModeConfigHolder {
    private ArrayList<WhiteLampMode> mConfigList = new ArrayList<>();

    public void reloadFromFile(Context context) {
        WhiteLampMode[] modes = WhiteLampModesFile.readFile(context);
        if (modes != null) {
            mConfigList.clear();
            mConfigList.addAll(Arrays.asList(modes));
        }
    }

    public void saveToFile(Context context) {
        WhiteLampModesFile.writeFile(context, this);
    }

    protected void addAtEnd(WhiteLampMode mode) {
        mConfigList.add(mode);
    }

    protected WhiteLampMode getAt(int i) {
        return mConfigList.get(i);
    }

    protected int getNumConfigs() {
        return mConfigList.size();
    }

}
