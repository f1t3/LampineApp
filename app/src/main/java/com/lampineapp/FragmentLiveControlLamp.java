package com.lampineapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

public class FragmentLiveControlLamp extends Fragment {

    Slider mSliderIntensity, mSliderColor;
    Slider.OnChangeListener mSliderColorOnChangeListener;
    TextView mTextViewColorPicker;
    Button mButtonRainbow50, mButtonRainbow200;
    ActivityLampConnected mSenderActivity;

    boolean rainbowOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Save parent activity through wich BT commands are send
        mSenderActivity = ((ActivityLampConnected)getActivity());

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_live_control_lamp, container, false);


        // Color slider
        mTextViewColorPicker = v.findViewById(R.id.live_control_color_picker_title);
        mSliderColor = v.findViewById(R.id.live_control_color_slider);
        mSliderColorOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Remove listener for time of processing current change
                mSliderColor.removeOnChangeListener(mSliderColorOnChangeListener);
                // Calculate RGB values scaled from 0 to 1
                float red = 0, green = 0, blue = 0;
                if (value < 0) {
                    // Dummy
                } else if (value >= 0  && value < 60  ) {
                    red = 1;
                    green = value / 60;
                } else if (value >= 60 && value < 120 ) {
                    red = 1 - (value-60) / 60;
                    green = 1;
                } else if (value >= 120 && value < 180) {
                    green = 1;
                    blue = (value-120) / 60;
                } else if (value >= 180 && value < 240) {
                    green = 1 - (value-180) / 60;
                    blue = 1;
                } else if (value >= 240 && value < 300) {
                    red = (value-240) / 60;
                    blue = 1;
                } else if (value >= 300 && value < 360) {
                    red = 1;
                    blue = 1 - (value-300) / 60;
                }
                final int iRed = (int)(255 * red);
                final int iGreen = (int)(255 * green);
                final int iBlue = (int)(255 * blue);
                mTextViewColorPicker.setText("Slider Value: " + value + ". R: " + iRed + ". G: " + iGreen + ". B: " + iBlue + ".");
                mSenderActivity.sendSerialString("ledctl -color ");
                sleep_ms(1);
                mSenderActivity.sendSerialString(iRed + " ");
                sleep_ms(1);
                mSenderActivity.sendSerialString(iGreen + " ");
                sleep_ms(1);
                mSenderActivity.sendSerialString(iBlue + "\r\n");

                // Re-add listener after short delay
                // TODO: DO SOME VALUE DELTA BASED PREVENT OF SENDING CHANGE VIA SERIAL
                sleep_ms(20);
                mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);
            }
        };
        mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);

        // TODO: REMOVE
        mButtonRainbow50 = v.findViewById(R.id.button_rainbow_50);
        mButtonRainbow50.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str;
                if (rainbowOn) {
                    str = "ledctl -lsd 50\r\n";
                    rainbowOn = false;
                }
                else {
                    str = "ledctl -lsd 300\r\n";
                    rainbowOn = true;
                }
                mSenderActivity.sendSerialString(str);
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

    private void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            // TODO: catch
        }
    }
}
