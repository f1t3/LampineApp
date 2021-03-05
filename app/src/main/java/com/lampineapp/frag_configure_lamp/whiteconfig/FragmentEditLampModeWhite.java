package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lampineapp.R;

public class FragmentEditLampModeWhite extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.activity_edit_lamp_mode_white, container, false);

        return v;
    }
}
