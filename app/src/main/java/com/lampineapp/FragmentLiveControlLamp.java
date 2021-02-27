package com.lampineapp;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.Slider;

public class FragmentLiveControlLamp extends Fragment {

    Switch mSwitchColoredMode;
    ConstraintLayout mConstraintLayoutColorPicker;
    View mSliderColorIndicatorLine;
    Slider mSliderIntensity, mSliderColor;
    Slider.OnChangeListener mSliderColorOnChangeListener;
    Slider.OnChangeListener mSliderIntensityOnChangeListener;

    TextView mTextViewColorPicker;
    ActivityLampConnected mSenderActivity;

    int rainbowState = 0;
    boolean mAckReceived = true;

    int rLast = 0, gLast = 0, bLast = 0;
    int iIntensityLase = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Save parent activity through wich BT commands are send
        mSenderActivity = ((ActivityLampConnected)getActivity());

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_live_control_lamp, container, false);

        // Color mode switch
        mSwitchColoredMode = v.findViewById(R.id.live_control_color_mode_switch);
        mSwitchColoredMode.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // Colored mode
                    mConstraintLayoutColorPicker.setVisibility(View.VISIBLE);
               // TODO
                    //     mSenderActivity.getTransmitter().sendSerialString("lctl c 0 0 0\r\n");
                } else {
                    // No colored mode
                    mConstraintLayoutColorPicker.setVisibility(View.GONE);
                  // TODO
                    //  mSenderActivity.getTransmitter().sendSerialString("lctl w 200\r\n");

                }
            }
        });

        mSliderIntensity = v.findViewById(R.id.live_control_intensity_slider);
        mSliderIntensityOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Remove listener for time of processing current change
                mSliderIntensity.removeOnChangeListener(mSliderIntensityOnChangeListener);

                // Max value 1000

                final int iIntensity = (int) (value);
                final int intensityDeltaThreshold = 50;
                if (Math.abs(iIntensityLase - iIntensity) > intensityDeltaThreshold) {
                    iIntensityLase = iIntensity;
                   // TODO
                    //  mSenderActivity.getTransmitter().sendSerialString("lctl w " + iIntensity + "\r\n");
                    sleep_ms(40);
                }

                // Re-add listener after short delay
                // TODO: DO SOME VALUE DELTA BASED PREVENT OF SENDING CHANGE VIA SERIAL
                mSliderIntensity.addOnChangeListener(mSliderIntensityOnChangeListener);
            }
        };
        mSliderIntensity.addOnChangeListener(mSliderIntensityOnChangeListener);


        // Color slider
        mConstraintLayoutColorPicker = v.findViewById(R.id.live_control_color_picker_layout);
        mSliderColorIndicatorLine = v.findViewById(R.id.color_slider_indicator_line);
        mTextViewColorPicker = v.findViewById(R.id.live_control_color_picker_title);
        mSliderColor = v.findViewById(R.id.live_control_color_slider);
        mSliderColorOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Remove listener for time of processing current change
                mSliderColor.removeOnChangeListener(mSliderColorOnChangeListener);

                // Redraw line over slider
                //int indicatorXPos = (int)(mSliderColor.getPaddingStart() + mSliderColor.getTranslationX());
                int indicatorXPos = (int)(mSliderColor.getPaddingLeft() + value / 359 * mSliderColor.getTrackWidth());
                mSliderColorIndicatorLine.setTranslationX(indicatorXPos);

                // Prepare listener for ACK
//                mSenderActivity.setSerialReceiveCallbackFunction(new ActivityLampConnected.SerialReceiveCallbackFunction() {
//                    @Override
//                    public void onSerialDataReceived(String data) {
//                        if (data.contains("ACK"))
//                            mAckReceived = true;
//                    }
//                });

                // If last message was not ACK'd yet, skip this one
                if (mAckReceived == false) {
                    //mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);
                    //return;
                }

                // ACK was received, so clear
                mAckReceived = false;

                // Calculate RGB values scaled from 0 to 1 as linear spectrum
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
                } else if (value >= 300 && value <= 360) {
                    red = 1;
                    blue = 1 - (value-300) / 60;
                }
                final int iRed = (int)(255 * red);
                final int iGreen = (int)(255 * green);
                final int iBlue = (int)(255 * blue);
                final int colorDeltaThreshold = 20;
                if (
                        Math.abs(rLast - iRed)   > colorDeltaThreshold ||
                        Math.abs(gLast - iGreen) > colorDeltaThreshold ||
                        Math.abs(bLast - iBlue)  > colorDeltaThreshold ) {
                    rLast = iRed;
                    gLast = iGreen;
                    bLast = iBlue;
// TODO
                    //mSenderActivity.getTransmitter().sendSerialString("lctl c " + iRed + " " + iGreen + " " + iBlue + "\r\n");
                    sleep_ms(40);
                }

                // Re-add listener after short delay
                // TODO: DO SOME VALUE DELTA BASED PREVENT OF SENDING CHANGE VIA SERIAL
                mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);
            }
        };
        mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);
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
