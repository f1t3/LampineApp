package com.lampineapp;

import android.os.Bundle;
import android.os.Handler;
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

import static java.nio.charset.StandardCharsets.*;

public class FragmentLiveControlLamp extends Fragment {
    private final static String TAG = FragmentLiveControlLamp.class.getSimpleName();

    Switch mSwitchColoredMode;
    ConstraintLayout mConstraintLayoutColorPicker;
    View mSliderColorIndicatorLine;
    Slider mSliderIntensity, mSliderColor;
    Slider.OnChangeListener mSliderColorOnChangeListener;
    Slider.OnChangeListener mSliderIntensityOnChangeListener;

    TextView mTextViewColorPicker;
    ActivityLampConnected mSenderActivity;

    private int mR = 99, mG = 0, mB = 0;
    private int mRLast = 99, mGLast = 99, mBLast = 99;
    private int mIntensity = 0;
    private int mIntensityLast = 99;

    // TODO ???
    int iIntensityLase = 0;

    final Handler mSliderHandler = new Handler();
    final Runnable mSliderHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSenderActivity.getLSMStack().isConnected()) {
                if (mR != mRLast || mG != mGLast || mB != mBLast) {
                    mSenderActivity.getLSMStack().send(getSetColorCMD(mR, mG, mB));
                    mRLast = mR;
                    mGLast = mG;
                    mBLast = mB;
                }

                if (mIntensity != mIntensityLast) {
                    mSenderActivity.getLSMStack().send(getSetIntensityCMD(mIntensity));
                    mIntensityLast = mIntensity;
                }
            }
            /*
             * The roundtrip time for MSG send -> ACK received is approx. 40ms.
             * Make sure we do not produce more than 1 MSG / 40 ms, since delay is
             * added for every packet that is not immediately ACKd!
             */
            // TODO: ESTIMATE ROUND TRIP TIME IN STACK TO PROVIDE PROPPER INTERVAL!
            mSliderHandler.postDelayed(this, mSenderActivity.getLSMStack().getReliableInterval_ms());
        }
    };

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
                    // Colored mode, set color to red with current intensity
                    mConstraintLayoutColorPicker.setVisibility(View.VISIBLE);
                    mSenderActivity.getLSMStack().send(getSetColorCMD(99, 0, 0));
                    mSenderActivity.getLSMStack().send(getSetIntensityCMD(mIntensity));
                } else {
                    // No colored mode
                    mConstraintLayoutColorPicker.setVisibility(View.GONE);
                  // TODO
                    //  mSenderActivity.getTransmitter().sendSerialString("lctl w 200\r\n");
                    String cmd = "lc w 10";
                    mSenderActivity.getLSMStack().send(cmd.getBytes(US_ASCII));

                }
            }
        });

        mSliderIntensity = v.findViewById(R.id.act_edit_mode_white_intensity_previewbar);
        mSliderIntensityOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Max value 99 as expected by firmware
                mIntensity = (int) (value);
            }
        };
        mSliderIntensity.addOnChangeListener(mSliderIntensityOnChangeListener);
        mSliderIntensity.setValue(0);

        // Color slider
        mConstraintLayoutColorPicker = v.findViewById(R.id.live_control_color_picker_layout);
        mSliderColorIndicatorLine = v.findViewById(R.id.color_slider_indicator_line);
        mTextViewColorPicker = v.findViewById(R.id.live_control_color_picker_title);
        mSliderColor = v.findViewById(R.id.live_control_color_slider);
        mSliderColorOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Redraw line over slider
                //int indicatorXPos = (int)(mSliderColor.getPaddingStart() + mSliderColor.getTranslationX());
                int indicatorXPos = (int)(mSliderColor.getPaddingLeft() + value / 359 * mSliderColor.getTrackWidth());
                mSliderColorIndicatorLine.setTranslationX(indicatorXPos);

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
                mR = (int)(99 * red);
                mG = (int)(99 * green);
                mB = (int)(99 * blue);
            }
        };
        mSliderColor.addOnChangeListener(mSliderColorOnChangeListener);
        mSliderColor.setValue(0);
        mSliderHandler.postDelayed(mSliderHandlerRunnable,1000);
        return v;
    }

    private byte[] getSetColorCMD(int r, int g, int b)
    {
            String cmdStr = "lc c " + r + " " + g + " " + b;
            return cmdStr.getBytes(US_ASCII);
    }

    private byte[] getSetIntensityCMD(int intensity)
    {
        String cmdStr = "lc i " + intensity;
        return cmdStr.getBytes(US_ASCII);
    }
}
