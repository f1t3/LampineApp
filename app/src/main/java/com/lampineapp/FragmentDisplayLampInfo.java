package com.lampineapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentDisplayLampInfo extends Fragment {
    TextView mTextViewBatteryVoltageValue;
    ActivityLampConnected mSenderActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_display_lamp_info, container, false);

        // Save parent activity through wich BT commands are send
        mSenderActivity = ((ActivityLampConnected)getActivity());

        mTextViewBatteryVoltageValue = v.findViewById(R.id.text_view_battery_voltage_value);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Display battery voltage
        mSenderActivity.getTransmitter().setSerialReceiveCallbackFunction(
                new LampineTransmitter.SerialReceiveCallbackFunction() {
            @Override
            public void onSerialDataReceived(String data) {
                final String batVoltage = data;
                mTextViewBatteryVoltageValue.setText("Battery volatage: " + batVoltage + "mV");
            }
        });
        mSenderActivity.getTransmitter().sendSerialString("sysctl print vbat\r\n");
    }

    // TODO: MOVE TO HELPER
    private void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            // TODO: catch
        }
    }
}
