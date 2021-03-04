package com.lampineapp.frag_configure_lamp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lampineapp.frag_configure_lamp.colorconfig.FragmentConfigureLampModesColor;
import com.lampineapp.frag_configure_lamp.whiteconfig.FragmentConfigureLampModesWhite;
import com.lampineapp.R;

public class FragmentConfigureLampModes extends Fragment {

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

        // White modes select button
        mWhiteModeBtn = new ModeButton(getActivity(), v, R.id.frag_lamp_conf_white_btn, R.id.frag_lamp_conf_white_btn_icon, R.id.frag_lamp_conf_white_btn_text, R.id.frag_lamp_conf_white_btn_indicator);
        mWhiteModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setState(FragmentState.WHITE_CONFIG);
            }
        });

        // Colored modes select button
        mColorModeBtn = new ModeButton(getActivity(), v, R.id.frag_lamp_conf_color_btn, R.id.frag_lamp_conf_color_btn_icon, R.id.frag_lamp_conf_color_btn_text, R.id.frag_lamp_conf_color_btn_indicator);
        mColorModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setState(FragmentState.COLOR_CONFIG);
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
                replaceCurrentModeConfigFragmentWith(new FragmentConfigureLampModesWhite());
                break;
            case COLOR_CONFIG:
                replaceHighlightedModeButton(mColorModeBtn);
                replaceCurrentModeConfigFragmentWith(new FragmentConfigureLampModesColor());
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

    protected void replaceCurrentModeConfigFragmentWith(Fragment fragment) {
        final FragmentManager fm = getChildFragmentManager();

        // Get and destroy current fragment
        final Fragment currFragment = fm.findFragmentById(R.id.fragment_config_lamp_mode_fragment_config_area);
        if (currFragment != null)
            fm.beginTransaction().remove(currFragment).commit();

        // Replace with new fragment
        fm.beginTransaction().replace(R.id.fragment_config_lamp_mode_fragment_config_area, fragment).commit();
    }
}
