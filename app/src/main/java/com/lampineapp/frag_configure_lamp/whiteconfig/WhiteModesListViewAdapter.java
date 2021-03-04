package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.lampineapp.R;
import com.lampineapp.frag_configure_lamp.FragmentConfigureLampModes;
import com.lampineapp.graphics.ColorGraphView;

import java.util.ArrayList;

public class WhiteModesListViewAdapter extends BaseAdapter {

    private ArrayList<WhiteModeItem> mItemList;
    private LayoutInflater mInflater;

    private Context mContext;

    public WhiteModesListViewAdapter(Context context) {
        super();
        mContext = context;
        mInflater = ((Activity)context).getLayoutInflater();
        mItemList = new ArrayList<>();

    }

        public void addItem(WhiteModeItem item) {
            mItemList.add(item);
        }
//
//        public FragmentConfigureLampModes.LampModeConfigurationItem getModeConfigItem(int position) {
//            return mLampModeConfigurationItemList.get(position);
//        }
//
//        public void clear() {
//            mLampModeConfigurationItemList.clear();
//        }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse converted view if existing
        View v = convertView == null ? mInflater.inflate(R.layout.fragment_configure_lamp_mode_white_listitem, parent, false) : convertView;

        final WhiteModeItem item = mItemList.get(position);
        setIntensityBarPercentage(item.getPercentage(), v);
        setItemTitle(item.getPercentage(), item.getCurrent(), v);
        // TODO: Do something with layout based on item!

        return v;
    }

    private void setIntensityBarPercentage(int percentage, View v) {
        // Set width
        int progressBarBorderWidth   = v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_intensity_border).getWidth();
        int progressBarProgressWidth = (progressBarBorderWidth * percentage) / 100;
        View progressBar =  v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_intensity_progress);
        ViewGroup.LayoutParams params = progressBar.getLayoutParams();
        params.width = progressBarProgressWidth;
        progressBar.setLayoutParams(params);

        // Set gradient
        int colorLeft  = mContext.getColor(R.color.colorIntensityStart);
        int colorRight = mContext.getColor(R.color.colorIntensityStop);
        int[] colorRange  = {colorLeft, (int) new ArgbEvaluator().evaluate((float)percentage/100, colorLeft, colorRight)};
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorRange);
        v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_intensity_background).setBackground(gradient);
    }

    private void setItemTitle(int percentage, int current, View v) {
        TextView title = v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_infotext);
        title.setText(mContext.getString(R.string.frag_conf_lamp_mode_white_listitem_text) + " " + percentage + "% (" + current + "mA)");
    }
}
