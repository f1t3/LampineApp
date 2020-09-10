package com.lampineapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentConfigureLamp extends Fragment {

    ActivityLampConnected mSenderActivity;
    ListView mListView;
    Button mButtonSelectWhiteModes, mButtonSelectColoredModes;
    LampModesConfigListViewAdapter mLampModesConfigsListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save parent activity through wich BT commands are send
        mSenderActivity = ((ActivityLampConnected) getActivity());

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_configure_lamp, container, false);

        // List view
        mLampModesConfigsListViewAdapter = new LampModesConfigListViewAdapter();
        mListView = v.findViewById(R.id.list_view_lamp_modes_configs);
        mListView.setAdapter(mLampModesConfigsListViewAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO: IMPLEMENT!
            }
        });

        // White modes select button
        mButtonSelectWhiteModes = v.findViewById(R.id.button_lamp_config_white_modes);
        mButtonSelectWhiteModes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: IMPLEMENT!
                // Request white configuration modes from lamp
                mSenderActivity.sendSerialString("confctl print whitemodes\r\n");
            }
        });

        // Colored modes select button
        mButtonSelectColoredModes = v.findViewById(R.id.button_lamp_config_color_modes);
        mButtonSelectColoredModes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: IMPLEMENT!
            }
        });

        // Position of values in config-CSV
        final int NAME_POS = 0;
        final int CURRENT_POS = 1;
        final int N_RGBW_POINTS = 2;
        final int START_RGBW_POINTS = 3;
        // Receive modes from lamp listener
        mSenderActivity.setSerialReceiveCallbackFunction(new ActivityLampConnected.SerialReceiveCallbackFunction() {
            @Override
            public void onSerialDataReceived(String data) {
                // TODO: PARSE CSV CONFIGS.
                // VALUE DELIMITER: ,
                // CONFIG DELIMITER: \n
                // Individual configs are separated by \n
                final String[] configArray = data.split("\n");
                for (String value : configArray) {
                    // Values in config are separated by ,
                    String[] valueArray = value.split(",");

                }
                final LampModeConfigurationItem configurationItem = new LampModeConfigurationItem(data, data);
                mLampModesConfigsListViewAdapter.addModeConfigItem(configurationItem);
                mLampModesConfigsListViewAdapter.notifyDataSetChanged();
            }
        });

        return v;
    }


    public void onResume() {
        super.onResume();
    }

    // Adapter for configuration items
    private class LampModesConfigListViewAdapter extends BaseAdapter {
        private ArrayList<LampModeConfigurationItem> mLampModeConfigurationItemList;
        private LayoutInflater mInflater = mSenderActivity.getLayoutInflater();

        public LampModesConfigListViewAdapter() {
            super();
            mLampModeConfigurationItemList = new ArrayList<>();
            // TODO: IS THIS CORRECT??
            mInflater = mSenderActivity.getLayoutInflater();
        }

        public void addModeConfigItem(LampModeConfigurationItem item) {
            mLampModeConfigurationItemList.add(item);
        }

        public LampModeConfigurationItem getModeConfigItem(int position) {
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

            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_configure_lamp_config_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textViewLampConfigItemName = (TextView) view.findViewById(R.id.text_view_lamp_config_item_name);
                viewHolder.textViewLampConfigItemCurrent = (TextView) view.findViewById(R.id.text_view_lamp_config_item_current);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            LampModeConfigurationItem configurationItem = mLampModeConfigurationItemList.get(i);
            viewHolder.textViewLampConfigItemName.setText(configurationItem.getName());
            viewHolder.textViewLampConfigItemCurrent.setText(configurationItem.getCurrent());

            return view;
        }
    }

    static class ViewHolder {
        TextView textViewLampConfigItemName;
        TextView textViewLampConfigItemCurrent;
    }

    static class LampModeConfigurationItem {
        private String mName;
        private String mCurrent;

        public LampModeConfigurationItem() {
            super();
        }

        public LampModeConfigurationItem(String name, String current) {
            mName = name;
            mCurrent = current;
        }

        public String getName() {
            return mName;
        }

        public String getCurrent() {
            return mCurrent;
        }
    }

    private void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            // TODO: catch
        }
    }
}
