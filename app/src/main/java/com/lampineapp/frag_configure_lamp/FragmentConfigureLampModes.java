package com.lampineapp.frag_configure_lamp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lampineapp.ActivityLampConnected;
import com.lampineapp.FragmentAddLampMode;
import com.lampineapp.R;

public class FragmentConfigureLampModes extends Fragment {

    FloatingActionButton mFab;
    ActivityLampConnected mSenderActivity;
    ListView mWhiteLampModesListView;
    WhiteModesListViewAdapter mLampModesConfigsListViewAdapter;

    // Button elements
    private ModeButton mWhiteModeBtn;
    private ModeButton mColorModeBtn;

    private enum FragmentState {
        COLOR_CONFIG, WHITE_CONFIG;
    }
    private FragmentState mState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_configure_lamp, container, false);

        // List view
        mLampModesConfigsListViewAdapter = new WhiteModesListViewAdapter();
        mWhiteLampModesListView = v.findViewById(R.id.list_view_lamp_modes_configs);
        mWhiteLampModesListView.setAdapter(mLampModesConfigsListViewAdapter);
        mWhiteLampModesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO: IMPLEMENT!
            }
        });

        // White modes select button
        mWhiteModeBtn = new ModeButton(getActivity(), v, R.id.frag_lamp_conf_white_btn, R.id.frag_lamp_conf_white_btn_icon, R.id.frag_lamp_conf_white_btn_text, R.id.frag_lamp_conf_white_btn_indicator);
        mWhiteModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceHighlightedModeButton(mWhiteModeBtn);
                // TODO: IMPLEMENT!
                // Request white configuration modes from lamp
                // mSenderActivity.sendSerialString("confctl print whitemodes\r\n");

                //TODO: REMOVE DUMMY RX
                // DUMMY RX
                //                            0|  1|  2|3|       |       |       |
                final String dummydata = "Mode1,1mA,100,4,0,0,0,0,0,0,0,0,0,0,0,0,255,128,64,32\n";
                LampModeConfigurationItem[] configItemArray = parseLampModeConfigsFromString(dummydata);
                for (LampModeConfigurationItem configItem : configItemArray) {
                    mLampModesConfigsListViewAdapter.addModeConfigItem(configItem);
                    mLampModesConfigsListViewAdapter.notifyDataSetChanged();
                }
                // TODO: END OF DUMMY RX

            }
        });

        // Colored modes select button
        mColorModeBtn = new ModeButton(getActivity(), v, R.id.frag_lamp_conf_color_btn, R.id.frag_lamp_conf_color_btn_icon, R.id.frag_lamp_conf_color_btn_text, R.id.frag_lamp_conf_color_btn_indicator);
        mColorModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceHighlightedModeButton(mColorModeBtn);
                // TODO: IMPLEMENT!
            }
        });

// TODO
//        // Receive modes from lamp listener
//        mSenderActivity.getTransmitter().setSerialReceiveCallbackFunction(new LLayer1ServiceProvider.SerialReceiveCallbackFunction() {
//            @Override
//            public void onSerialDataReceived(String data) {
//                // TODO: PARSE CSV CONFIGS.
//                LampModeConfigurationItem[] configItemArray = parseLampModeConfigsFromString(data);
//                for (LampModeConfigurationItem configItem : configItemArray) {
//                    mLampModesConfigsListViewAdapter.addModeConfigItem(configItem);
//                    mLampModesConfigsListViewAdapter.notifyDataSetChanged();
//                }
//            }
//        });

        // Floating action button
        mFab = v.findViewById(R.id.fab_add_config_item);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace UI with FragmentAddLampMode, add current UI to stack
                final FragmentAddLampMode fragmentAddLampMode = new FragmentAddLampMode();
                final FragmentManager fm = mSenderActivity.getFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.lamp_connected_ui_fragment_area, fragmentAddLampMode)
                        .addToBackStack(null)
                        .commit();
            }
        });

        setState(FragmentState.WHITE_CONFIG);

        return v;
    }

    private void setState(FragmentState state) {
        mState = state;
        switch (state)
        {
            case WHITE_CONFIG:
                replaceHighlightedModeButton(mWhiteModeBtn);
                break;
            case COLOR_CONFIG:
                replaceHighlightedModeButton(mColorModeBtn);
                break;
        }
    }

    private void replaceHighlightedModeButton(ModeButton button) {
        ModeButton[] btnArray = {mColorModeBtn, mWhiteModeBtn};
        for (int i = 0; i < btnArray.length; i++) {
            if (btnArray[i].equals(button)) {
                btnArray[i].setActive();
            } else {
                btnArray[i].setInactive();
            }
        }
    }



}
