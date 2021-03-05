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
import com.lampineapp.graphics.gradientbar.GradientProgressBar;
import com.lampineapp.lamp.LampineHeadlamp;
import com.lampineapp.lamp.LampineK9RGB;
import com.lampineapp.lamp.LampineLamp;

import java.util.ArrayList;

public class WhiteModesListViewAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private WhiteModeConfigHolder mConfigHolder;

    private Context mContext;

    public WhiteModesListViewAdapter(Context context, WhiteModeConfigHolder configHolder) {
        super();
        mContext = context;
        mInflater = ((Activity)context).getLayoutInflater();
        mConfigHolder = configHolder;
    }

    public void addItem(WhiteLampMode mode) {
        mConfigHolder.addAtEnd(mode);
    }

    public void updateConfigHolder(WhiteModeConfigHolder configHolder) {
        mConfigHolder = configHolder;
    }


    @Override
    public int getCount() {
        return mConfigHolder.getNumConfigs();
    }

    @Override
    public Object getItem(int i) {
        return mConfigHolder.getAt(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse converted view if existing
        View v = convertView == null ? mInflater.inflate(R.layout.fragment_configure_lamp_mode_white_listitem, parent, false) : convertView;

        final GradientProgressBar intensityBar = v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_intensitybar);

        final WhiteLampMode mode = mConfigHolder.getAt(position);

        intensityBar.setGradientColorRange(new int[]{mContext.getColor(R.color.colorIntensityStart), mContext.getColor(R.color.colorIntensityStop)});
        intensityBar.setProgress(mode.getIntensity());
        intensityBar.setOutlineWidth(10);
        LampineLamp lamp = new LampineK9RGB();
        setItemTitle((int)(mode.getIntensity()*100), lamp.getLedCurrent_mA(mode.getIntensity()), v);

        // TODO: Do something with layout based on item!

        return v;
    }

    private void setItemTitle(int percentage, int current, View v) {
        TextView title = v.findViewById(R.id.fragment_configure_lamp_mode_white_listitem_infotext);
        title.setText(mContext.getString(R.string.frag_conf_lamp_mode_white_listitem_text) + " " + percentage + "% (" + current + "mA)");
    }
}
