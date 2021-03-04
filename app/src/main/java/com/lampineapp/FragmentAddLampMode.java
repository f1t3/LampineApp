package com.lampineapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;

import com.lampineapp.frag_configure_lamp.FragmentConfigureLampModes;
import com.lampineapp.graphics.colorgraphinputview.ColorGraphInputView;
import com.lampineapp.graphics.colorgraphinputview.ColorTimeSeries;

public class FragmentAddLampMode extends Fragment {

    private final static String TAG = FragmentAddLampMode.class.getSimpleName();

    ColorGraphInputView mColorGraphInputView;
    FragmentConfigureLampModes mFragmentConfigureLamp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater
                .inflate(R.layout.fragment_add_lamp_mode, container, false);

        // Color graph input
        mColorGraphInputView = v.findViewById(R.id.add_lamp_config_color_graph_input);
        mColorGraphInputView.setCurveCompleteCallbackFun(
                new ColorGraphInputView.CurveCompleteCallbackFun() {
                    @Override
                    public void onCurveComplete(ColorTimeSeries curve) {
                        String configItemString = generateConfigItemString(curve);
                        Log.d(TAG, configItemString);
                    }
                });

        return v;
    }

//    @Override
//    public void onViewCreated(View.v, Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Color graph input callback
//        mColorGraphInputView.setColorCurveCompleteCallbackFunction(
//                new ColorGraphInputView.ColorCurveCompleteCallbackFunction() {
//                    @Override
//                    public void onColorCurveComplete(ColorGraphInputView.ColorCurve curve) {
//                        generateConfigItemString(curve);
//                    }
//                });
//    }

    private String generateConfigItemString(ColorTimeSeries curve) {
        return "test";
    }
}
