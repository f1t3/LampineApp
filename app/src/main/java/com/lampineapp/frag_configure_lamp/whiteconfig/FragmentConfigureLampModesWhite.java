package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lampineapp.ActivityLampConnected;
import com.lampineapp.R;
import com.lampineapp.lamp.LampineK9RGB;


public class FragmentConfigureLampModesWhite extends Fragment {

    FloatingActionButton mFab;
    ListView mListView;
    WhiteModesListViewAdapter mListViewAdapter;
    WhiteModeConfigHolder mModeConfigHolder;
    Fragment mFrag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_configure_lamp_mode_white, container, false);
        mFrag = this;

        // List view
        mModeConfigHolder = new WhiteModeConfigHolder();
        mListViewAdapter = new WhiteModesListViewAdapter(getActivity(), mModeConfigHolder);
        mListView = v.findViewById(R.id.fragment_configure_lamp_mode_white_listview);
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
            int position, long id) {
                // TODO: IMPLEMENT!
            }
        });

        // FAB
        mFab = v.findViewById(R.id.fragment_configure_lamp_mode_white_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getActivity(), ActivityEditLampModeWhite.class);
                intent.putExtra(ActivityEditLampModeWhite.EXTRA_EDIT_MODE, "ADD_MODE");
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload configs
        mModeConfigHolder.reloadFromFile(getActivity());
        mListViewAdapter.updateConfigHolder(mModeConfigHolder);
        mListViewAdapter.notifyDataSetChanged();

        ((ActivityLampConnected)getActivity()).getLSMStack().send(LampineK9RGB.getMemctlSaveWhiteconfigCmd(WhiteLampModesFile.readFileBytes(getActivity())));
    }


}
