package com.lampineapp.frag_configure_lamp.colorconfig;

import android.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import android.app.Fragment;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lampineapp.ActivityLampConnected;
import com.lampineapp.R;
import com.lampineapp.frag_configure_lamp.FragmentConfigureLampModes;
import com.lampineapp.graphics.ColorGraphView;
//import com.lampineapp.frag_configure_lamp.whiteconfig.WhiteModesListViewAdapter;

public class FragmentConfigureLampModesColor extends Fragment {

//    FloatingActionButton mFab;
//    ActivityLampConnected mSenderActivity;
//    ListView mWhiteLampModesListView;
//    WhiteModesListViewAdapter mLampModesConfigsListViewAdapter;
//
//
//    // Floating action button
//    mFab = v.findViewById(R.id.fab_add_config_item);
//        mFab.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            // Replace UI with FragmentAddLampMode, add current UI to stack
//            final FragmentAddLampMode fragmentAddLampMode = new FragmentAddLampMode();
//            final FragmentManager fm = mSenderActivity.getFragmentManager();
//            fm.beginTransaction()
//                    .replace(R.id.lamp_connected_ui_fragment_area, fragmentAddLampMode)
//                    .addToBackStack(null)
//                    .commit();
//        }
//    });

    // TODO: FROM ADAPTER BEFORE SPLIT!
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Reuse converted view if existing
//        View v = convertView == null ? mInflater.inflate(R.layout.fragment_configure_lamp_mode_white_listitem, parent, false) : convertView;
//
//        mItemList.get(position);
//
//        // General ListView optimization code.
//        if (view == null) {
//            view = mInflater.inflate(R.layout.fragment_configure_lamp_config_item, null);
//            viewHolder = new FragmentConfigureLampModes.ViewHolder();
//            viewHolder.textViewLampConfigItemName = (TextView) view.findViewById(R.id.text_view_lamp_config_item_name);
//            viewHolder.textViewLampConfigItemCurrent = (TextView) view.findViewById(R.id.text_view_lamp_config_item_current);
//            ColorGraphView colorGraphView = (ColorGraphView) view.findViewById(R.id.color_graph_view_lanp_config_item);
//            float mDummyY[] = {0, 255, 10, 0, 20, 255, 60};
//            colorGraphView.setData(mDummyY);
//            final int colors[] = {R.color.colorAccent, R.color.colorAccent2, R.color.colorAccent3, R.color.colorAccent4, R.color.colorAccent5, R.color.colorAccent6, R.color.colorAccent7};
//            colorGraphView.setColor(colors);
//            viewHolder.colorGraphView = colorGraphView;
//            view.setTag(viewHolder);
//        } else {
//            viewHolder = (FragmentConfigureLampModes.ViewHolder) view.getTag();
//        }
//
//        FragmentConfigureLampModes.LampModeConfigurationItem configurationItem = mItemList.get(i);
//        viewHolder.textViewLampConfigItemName.setText(configurationItem.getName());
//        viewHolder.textViewLampConfigItemCurrent.setText(configurationItem.getCurrent());
//
//        // Draw W Graph
//        // TODO: CALCULATE ACTUAL COLOR VALUES
//
//        return view;
//    }



}
