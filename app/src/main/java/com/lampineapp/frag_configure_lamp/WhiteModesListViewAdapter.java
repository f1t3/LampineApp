package com.lampineapp.frag_configure_lamp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lampineapp.R;
import com.lampineapp.graphics.ColorGraphView;

import java.util.ArrayList;

public class WhiteModesListViewAdapter extends BaseAdapter {
        private ArrayList<FragmentConfigureLampModes.LampModeConfigurationItem> mLampModeConfigurationItemList;
        private LayoutInflater mInflater = mSenderActivity.getLayoutInflater();

        public WhiteModesListViewAdapter() {
            super();
            mLampModeConfigurationItemList = new ArrayList<>();
            // TODO: IS THIS CORRECT??
            mInflater = mSenderActivity.getLayoutInflater();
        }

        public void addModeConfigItem(FragmentConfigureLampModes.LampModeConfigurationItem item) {
            mLampModeConfigurationItemList.add(item);
        }

        public FragmentConfigureLampModes.LampModeConfigurationItem getModeConfigItem(int position) {
            return mLampModeConfigurationItemList.get(position);
        }

        public void clear() {
            mLampModeConfigurationItemList.clear();
        }

        @Override
        public int getCount() {
            return mLampModeConfigurationItemList.size();
        }

        @Override
        public Object getItem(int i) {
            return mLampModeConfigurationItemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            FragmentConfigureLampModes.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_configure_lamp_config_item, null);
                viewHolder = new FragmentConfigureLampModes.ViewHolder();
                viewHolder.textViewLampConfigItemName = (TextView) view.findViewById(R.id.text_view_lamp_config_item_name);
                viewHolder.textViewLampConfigItemCurrent = (TextView) view.findViewById(R.id.text_view_lamp_config_item_current);
                ColorGraphView colorGraphView = (ColorGraphView) view.findViewById(R.id.color_graph_view_lanp_config_item);
                float mDummyY[] = {0, 255, 10, 0, 20, 255, 60};
                colorGraphView.setData(mDummyY);
                final int colors[] = {R.color.colorAccent, R.color.colorAccent2, R.color.colorAccent3, R.color.colorAccent4, R.color.colorAccent5, R.color.colorAccent6, R.color.colorAccent7};
                colorGraphView.setColor(colors);
                viewHolder.colorGraphView = colorGraphView;
                view.setTag(viewHolder);
            } else {
                viewHolder = (FragmentConfigureLampModes.ViewHolder) view.getTag();
            }

            FragmentConfigureLampModes.LampModeConfigurationItem configurationItem = mLampModeConfigurationItemList.get(i);
            viewHolder.textViewLampConfigItemName.setText(configurationItem.getName());
            viewHolder.textViewLampConfigItemCurrent.setText(configurationItem.getCurrent());

            // Draw W Graph
            // TODO: CALCULATE ACTUAL COLOR VALUES

            return view;
        }
    }
}
