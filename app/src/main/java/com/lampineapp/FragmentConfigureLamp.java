package com.lampineapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentConfigureLamp extends Fragment {

    ActivityLampConnected mSenderActivity;
    ListView mListView;
    ImageButton mButtonSelectWhiteModes, mButtonSelectColoredModes;
    LampModesConfigListViewAdapter mLampModesConfigsListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save parent activity through wich BT commands are send
        mSenderActivity = ((ActivityLampConnected) getActivity());

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_serial_terminal, container, false);

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
        mSenderActivity.setSerialReceiveCallbackFunction(new ActivityLampConnected.SerialReceiveCallbackFunction() {
            @Override
            public void onSerialDataReceived(String data) {
                // TODO: IMPLEMENT!
//                final FragmentLampConsole.SerialLine serialLine = new FragmentLampConsole.SerialLine(data, false);
//                mLampModesConfigsListViewAdapter.addLine(serialLine);
//                mLampModesConfigsListViewAdapter.notifyDataSetChanged();
//                mListView.smoothScrollToPosition(mLampModesConfigsListViewAdapter.getCount());
            }
        });

        return v;
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

            // TODO: CONTINUE
            FragmentLampConsole.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_serial_terminal_line_item, null);
                viewHolder = new FragmentLampConsole.ViewHolder();
                viewHolder.textViewTimeStamp = (TextView) view.findViewById(R.id.serial_terminal_line_text_edit_timestamp);
                viewHolder.textViewSerialLine = (TextView) view.findViewById(R.id.serial_terminal_line_text_edit_linetext);
                view.setTag(viewHolder);
            } else {
                viewHolder = (FragmentLampConsole.ViewHolder) view.getTag();
            }

            FragmentLampConsole.SerialLine line = mLampModeConfigurationItemList.get(i);
            viewHolder.textViewTimeStamp.setText(line.getTimeStamp());
            viewHolder.textViewSerialLine.setText(line.getSerialMessageWithoutLinefeedAtEnd());
            int color;
            if (line.isRxLine()) {
                color = getResources().getColor(R.color.colorAccent2);
            } else {
                color = getResources().getColor(R.color.colorAccent);
            }
            viewHolder.textViewSerialLine.setTextColor(color);

            return view;
        }
    }

    static class LampModeConfigurationItem {
        private String mTimeStamp;
        private String mSerialMessage;
        private boolean mIsRxLine;

        public LampModeConfigurationItem(String serialMessage, boolean isRxLine) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss.SSS");
            mTimeStamp = simpleDateFormat.format(new Date());
            mSerialMessage = serialMessage;
            mIsRxLine = isRxLine;
        }

        public String getTimeStamp() {
            return mTimeStamp;
        }

        public String getSerialMessage() {
            return mSerialMessage;
        }

        public String getSerialMessageWithoutLinefeedAtEnd() {
            return mSerialMessage.substring(0, mSerialMessage.length() - 2);
        }

        public boolean isRxLine() {
            return mIsRxLine;
        }
    }
}
