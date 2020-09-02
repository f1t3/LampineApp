package com.lampineapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;
import android.widget.Button;

public class FragmentLiveControlLamp extends Fragment {

    Button mButtonRainbow50, mButtonRainbow200;
    boolean rainbowOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_live_control_lamp, container, false);

        mButtonRainbow50 = v.findViewById(R.id.button_rainbow_50);
        mButtonRainbow50.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str;
                if (rainbowOn) {
                    str = "ledctl -lsd 50";
                    rainbowOn = false;
                }
                else {
                    str = "ledctl -lsd 300";
                    rainbowOn = true;
                }
                ((ActivityLampConnected)getActivity()).sendSerialString(str);
            }
        });
        mButtonRainbow200 = v.findViewById(R.id.button_rainbow_200);
        mButtonRainbow200.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ActivityLampConnected)getActivity()).sendSerialString("ledctl -help");
            }
        });

        return v;
    }
}
