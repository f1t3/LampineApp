package com.lampineapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.btle.LampineBluetoothLeTransmitter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lampineapp.graphics.ColorGraphView;
import com.lampineapp.helper.DataHelpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.lampineapp.helper.DataHelpers.getMaxValue;
import static com.lampineapp.helper.DataHelpers.parseStringArrayToIntArray;

public class FragmentConfigureLamp extends Fragment {

    FloatingActionButton mFab;
    ActivityLampConnected mSenderActivity;
    ListView mListView;
    Button mButtonSelectWhiteModes, mButtonSelectColoredModes;
    LampModesConfigListViewAdapter mLampModesConfigsListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save parent activity through which BT commands are send
        mSenderActivity = ((ActivityLampConnected) getActivity());

        // Inflate the layout for this fragment
        final View v = inflater
                .inflate(R.layout.fragment_configure_lamp, container, false);

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
        mButtonSelectColoredModes = v.findViewById(R.id.button_lamp_config_color_modes);
        mButtonSelectColoredModes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: IMPLEMENT!
            }
        });

        // Receive modes from lamp listener
        mSenderActivity.getTransmitter().setSerialReceiveCallbackFunction(new LampineBluetoothLeTransmitter.SerialReceiveCallbackFunction() {
            @Override
            public void onSerialDataReceived(String data) {
                // TODO: PARSE CSV CONFIGS.
                LampModeConfigurationItem[] configItemArray = parseLampModeConfigsFromString(data);
                for (LampModeConfigurationItem configItem : configItemArray) {
                    mLampModesConfigsListViewAdapter.addModeConfigItem(configItem);
                    mLampModesConfigsListViewAdapter.notifyDataSetChanged();
                }
            }
        });

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

        return v;
    }

    public LampModeConfigurationItem[] parseLampModeConfigsFromString(String str) {
        // Delimiters
        final String MODE_DELIM = "\n";
        final String VALUE_DELIM = ",";
        final String END_OF_DATA = "\r";
        // Position of values in config-CSV
        final int NAME_POS = 0;
        final int CURRENT_POS = 1;
        final int PERIOD_PER_POINT_MS_POS = 2;
        final int N_RGBW_POINTS = 3;
        final int START_RGBW_POINTS = 4;

        final String[] configArray = str.split(MODE_DELIM);
        LampModeConfigurationItem[] ret = new LampModeConfigurationItem[configArray.length];

        for (int i = 0; i < configArray.length; i++) {
            if (configArray[i].contains(END_OF_DATA))
                break;
            String[] valueArray = configArray[i].split(VALUE_DELIM);
            LampModeConfigurationItem configurationItem = new LampModeConfigurationItem();
            configurationItem.setName(valueArray[NAME_POS]);
            configurationItem.setCurrent(valueArray[CURRENT_POS]);
            configurationItem.setPeriodPerPoint_ms(Integer.parseInt(valueArray[PERIOD_PER_POINT_MS_POS]));
            int nRgbwPoints = Integer.parseInt(valueArray[N_RGBW_POINTS]);
            configurationItem.setNRgbwPoints(nRgbwPoints);

            // Parse RGBW points subarrays
            int[][] colorMatrix = new int[4][N_RGBW_POINTS];
            for (int colorIndex = 0; colorIndex < 4; colorIndex++) {
                final int startIndex = START_RGBW_POINTS + colorIndex * nRgbwPoints;
                final int stopIndex = START_RGBW_POINTS + (colorIndex + 1) * nRgbwPoints;
                final String[] colorValueArray = Arrays.
                        copyOfRange(valueArray, startIndex, stopIndex - 1);
                colorMatrix[colorIndex] = parseStringArrayToIntArray(colorValueArray);
            }
            configurationItem.setRPoints(colorMatrix[0]);
            configurationItem.setGPoints(colorMatrix[1]);
            configurationItem.setBPoints(colorMatrix[2]);
            configurationItem.setWPoints(colorMatrix[3]);
            ret[i] = configurationItem;
        }
        return ret;
    }


    public void onResume() {
        super.onResume();
    }

    static class ViewHolder {
        TextView textViewLampConfigItemName;
        TextView textViewLampConfigItemCurrent;
        ColorGraphView colorGraphView;
    }

    static class LampModeConfigurationItem {
        private String mName;
        private String mCurrent;
        private int mPeriodPerPoint_ms;
        private int mNRgbwPoints;
        private int[] mRPoints, mGPoints, mBPoints, mWPoints;

        public LampModeConfigurationItem() {
            super();
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getCurrent() {
            return mCurrent;
        }

        public void setCurrent(String current) {
            mCurrent = current;
        }

        public int getPeriodPerPoint_ms() {
            return mPeriodPerPoint_ms;
        }

        public void setPeriodPerPoint_ms(int period) {
            mPeriodPerPoint_ms = period;
        }

        public void setNRgbwPoints(int nPoints) {
            mNRgbwPoints = nPoints;
        }

        public void setRPoints(int[] rPoints) {
            mRPoints = rPoints;
        }

        public void setGPoints(int[] gPoints) {
            mGPoints = gPoints;
        }

        public void setBPoints(int[] bPoints) {
            mBPoints = bPoints;
        }

        public void setWPoints(int[] wPoints) {
            mWPoints = wPoints;
        }
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
                ColorGraphView colorGraphView = (ColorGraphView) view.findViewById(R.id.color_graph_view_lanp_config_item);
                float mDummyY[] = {0, 255, 10, 0, 20, 255, 60};
                colorGraphView.setData(mDummyY);
                final int colors[] = {R.color.colorAccent, R.color.colorAccent2, R.color.colorAccent3, R.color.colorAccent4, R.color.colorAccent5, R.color.colorAccent6, R.color.colorAccent7};
                colorGraphView.setColor(colors);
                viewHolder.colorGraphView = colorGraphView;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            LampModeConfigurationItem configurationItem = mLampModeConfigurationItemList.get(i);
            viewHolder.textViewLampConfigItemName.setText(configurationItem.getName());
            viewHolder.textViewLampConfigItemCurrent.setText(configurationItem.getCurrent());

            // Draw W Graph
            // TODO: CALCULATE ACTUAL COLOR VALUES

            return view;
        }
    }
}
