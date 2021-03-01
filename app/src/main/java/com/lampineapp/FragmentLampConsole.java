package com.lampineapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lampineapp.lsms.LSMStack;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentLampConsole extends Fragment {
    private final static String TAG = FragmentLampConsole.class.getSimpleName();

    ListView mListView;
    ImageButton mButtonSendSerialLine;
    SerialTerminalListViewAdapter mSerialTerminalListViewAdapter;

    LSMStack mLSMStack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v =  inflater.inflate(R.layout.fragment_serial_terminal, container, false);

        mLSMStack = ((ActivityLampConnected)getActivity()).getLSMStack();

        // List view
        mSerialTerminalListViewAdapter = new SerialTerminalListViewAdapter();
        mListView = v.findViewById(R.id.list_view_serial_terminal);
        mListView.setAdapter(mSerialTerminalListViewAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO: DO SOMETHING ON CLICK?
            }
        });

        // Send button
        mButtonSendSerialLine = v.findViewById(R.id.serial_terminal_send_button);
        mButtonSendSerialLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String line = ((TextView)v.findViewById(R.id.serial_terminal_send_input)).getText().toString();
                // Send line
                mLSMStack.send(line);

                // Add line to list view and scroll to bottom
                final SerialLine serialLine = new SerialLine(line, false);
                mSerialTerminalListViewAdapter.addLine(serialLine);
                mSerialTerminalListViewAdapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(mSerialTerminalListViewAdapter.getCount());
            }
        });

        // Receive listener
        mLSMStack.setOnReceiveListener(new LSMStack.ReceiveListener() {
            @Override
            public void onReceive(byte[] data) {
                final String dataStr = new String(data, StandardCharsets.US_ASCII);
                Log.d(TAG, "Received: " + dataStr);
                final SerialLine serialLine = new SerialLine(dataStr, true);
                mSerialTerminalListViewAdapter.addLine(serialLine);
                mSerialTerminalListViewAdapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(mSerialTerminalListViewAdapter.getCount());
            }
        });
        return v;
    }

    // Adapter for holding serial lines
    private class SerialTerminalListViewAdapter extends BaseAdapter {
        private ArrayList<SerialLine> mSerialLinesArrayList;
        private LayoutInflater mInflater;

        public SerialTerminalListViewAdapter() {
            super();
            mSerialLinesArrayList = new ArrayList<SerialLine>();
            // TODO: IS THIS CORRECT??
            mInflater = getActivity().getLayoutInflater();
        }

        public void addLine(SerialLine serialLine) {
                mSerialLinesArrayList.add(serialLine);
        }

        public SerialLine getLine(int position) {
            return mSerialLinesArrayList.get(position);
        }

        public void clear() {
            mSerialLinesArrayList.clear();
        }

        @Override
        public int getCount() {
            return mSerialLinesArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return mSerialLinesArrayList.get(i);
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
                view = mInflater.inflate(R.layout.fragment_serial_terminal_line_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textViewTimeStamp = (TextView) view.findViewById(R.id.serial_terminal_line_text_edit_timestamp);
                viewHolder.textViewSerialLine = (TextView) view.findViewById(R.id.serial_terminal_line_text_edit_linetext);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            SerialLine line = mSerialLinesArrayList.get(i);
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

    static class ViewHolder {
        TextView textViewTimeStamp;
        TextView textViewSerialLine;
    }

    static class SerialLine {
        private String mTimeStamp;
        private String mSerialMessage;
        private boolean mIsRxLine;

        public SerialLine(String serialMessage, boolean isRxLine) {
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
            while (mSerialMessage.endsWith("\n") || mSerialMessage.endsWith("\r") && mSerialMessage.length() > 0) {
                mSerialMessage = mSerialMessage.substring(0, mSerialMessage.length() - 1);
            }
            return mSerialMessage;
        }

        public boolean isRxLine() {
            return mIsRxLine;
        }
    }

}
