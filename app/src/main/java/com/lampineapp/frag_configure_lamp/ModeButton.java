package com.lampineapp.frag_configure_lamp;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.lampineapp.R;

public class ModeButton {
    private Context mContext;
    private View mView;
    private LinearLayout mLayout;
    private ImageView mIcon;
    private TextView mText;
    private CardView mIndicator;
    static private int mColorInactive;
    static private int mColorActive;


    public ModeButton(Context context, View v, int viewId, int iconId, int textId, int indicatorId) {
        mLayout = v.findViewById(viewId);
        mIcon = v.findViewById(iconId);
        mText = v.findViewById(textId);
        mIndicator = v.findViewById(indicatorId);
        mColorInactive = context.getColor(R.color.colorIconInactive);
        mColorActive   = context.getColor(R.color.colorIconActive);
        mView = v;
        mContext = context;
    }

    protected ModeButton setActive() {
        mIcon.setColorFilter(mColorActive);
        mText.setTextColor(mColorActive);
        mIndicator.setVisibility(View.VISIBLE);
        return this;
    }

    protected ModeButton setInactive() {
        mIcon.setColorFilter(mColorInactive);
        mText.setTextColor(mColorInactive);
        mIndicator.setVisibility(View.INVISIBLE);
        return this;
    }

    protected ModeButton setOnClickListener(View.OnClickListener listener) {
        mLayout.setOnClickListener(listener);
        return this;
    }
}
