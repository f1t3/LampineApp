package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lampineapp.R;
//import com.lampineapp.frag_configure_lamp.whiteconfig.WhiteModesListViewAdapter;

public class FragmentConfigureLampModesWhite extends Fragment {

    FloatingActionButton mFab;
    ListView mListView;
    WhiteModesListViewAdapter mListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_configure_lamp_mode_white, container, false);

        // List view
        mListViewAdapter = new WhiteModesListViewAdapter(getActivity());
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
                WhiteModeItem item = new WhiteModeItem("ABCD", mListViewAdapter.getCount()*10);
                mListViewAdapter.addItem(item);
                mListViewAdapter.notifyDataSetChanged();
            }
        });


        return v;
    }
}
