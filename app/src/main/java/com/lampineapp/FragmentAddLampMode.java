package com.lampineapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;

import com.lampineapp.graphics.ColorGraphInputView;

public class FragmentAddLampMode extends Fragment {

    ActivityLampConnected mSenderActivity;
    ColorGraphInputView mColorGraphInputView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save parent activity through which BT commands are send
        mSenderActivity = ((ActivityLampConnected) getActivity());

        // Inflate the layout for this fragment
        final View v = inflater
                .inflate(R.layout.fragment_add_lamp_mode, container, false);

        // Color graph input
        mColorGraphInputView = mSenderActivity.findViewById(R.id.add_lamp_config_color_graph_input);

        return v;
    }
}
