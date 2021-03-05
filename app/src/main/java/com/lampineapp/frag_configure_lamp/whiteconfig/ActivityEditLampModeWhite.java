package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.lampineapp.R;
import com.lampineapp.graphics.gradientbar.GradientProgressBar;
import com.lampineapp.helper.DataHelpers;
import com.lampineapp.lamp.LampineHeadlamp;
import com.lampineapp.lamp.LampineK9RGB;
import com.lampineapp.lamp.LampineLamp;

public class ActivityEditLampModeWhite extends AppCompatActivity {
    private final static String TAG = ActivityEditLampModeWhite.class.getSimpleName();

    public final static String EXTRA_EDIT_MODE = "EXTRA_EDIT_MODE";
    private enum EditMode {
        MODE_ADD, MODE_EDIT
    };
    private EditMode mMode;

    private LampineLamp mLamp;
    private float mIntensity;

    // UI items
    private Slider mSliderIntensity;
    private Slider.OnChangeListener mSliderIntensityOnChangeListener;
    private GradientProgressBar mIntensityPreviewBar;
    private TextView mPercentageText;
    private TextView mPowerText;
    private TextView mCurrentText;
    private TextView mLifetimeText;
    private TextView mWarningText;

    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lamp_mode_white);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_24px);

        if (this.getIntent().getStringExtra(EXTRA_EDIT_MODE).equals("ADD_MODE")) {
            mMode = EditMode.MODE_ADD;
            mActionBar.setTitle(R.string.act_edit_mode_white_title_add);
        } else {
            mMode = EditMode.MODE_EDIT;
            mActionBar.setTitle(R.string.act_edit_mode_white_title_add);
        }

        // TODO: ADJUST TO LAMP
        mLamp = new LampineK9RGB();
        buildGui();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act_edit_lamp_mode_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.act_edit_lamp_mode_menu_save_btn) {
            // Save
            WhiteModeConfigHolder holder = new WhiteModeConfigHolder();
            holder.reloadFromFile(this);
            holder.addAtEnd(new WhiteLampMode(mIntensity));
            holder.saveToFile(this);
            onBackPressed();
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildGui() {
        // Preview bar
        mIntensityPreviewBar = findViewById(R.id.act_edit_mode_white_intensity_previewbar);
        mIntensityPreviewBar.setGradientColorRange(new int[]{getColor(R.color.colorIntensityStart), getColor(R.color.colorIntensityStop)})
                .setProgress(0)
                .setOutlineWidth(5);
        // Text fields
        mPercentageText = findViewById(R.id.act_edit_mode_white_intensity_percentage_text);
        mPowerText      = findViewById(R.id.act_edit_mode_white_led_parameter_power_text);
        mCurrentText    = findViewById(R.id.act_edit_mode_white_led_parameter_current_text);
        mLifetimeText   = findViewById(R.id.act_edit_mode_white_battery_lifetime_text);
        mWarningText    = findViewById(R.id.act_edit_mode_white_battery_warning_text);

        // Init GUI
        updateGuiWithNewIntensity(0);

        // Slider
        mSliderIntensity = findViewById(R.id.act_edit_mode_white_intensity_slider);
        mSliderIntensity.setValue(0);
        mSliderIntensityOnChangeListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Value from 0 to 1
                updateGuiWithNewIntensity(value);
                mIntensity = value;
            }
        };
        mSliderIntensity.addOnChangeListener(mSliderIntensityOnChangeListener);

    }

    private void updateGuiWithNewIntensity(float value) {
        mIntensityPreviewBar.setProgress(value);
        setPercentageAndFluxText(value);
        setPower(value);
        setCurrent(value);
        if (mLamp.getLampType() == LampineLamp.LampType.HEADLAMP) {
            // TODO Handle non headlamp
            setLifetime(value);
            setWarningText(value);
        }
    }

    private void setPercentageAndFluxText(float value) {
        int percentage = (int)(value*100);
        int flux = mLamp.getFlux_lm(value);
        String text = getString(R.string.act_edit_mode_white_intensity_percentage_string, percentage, flux);
        mPercentageText.setText(text);
    }

    private void setPower(float value) {
        int power = mLamp.getLedInputPower_mW(value);
        String text = getString(R.string.act_edit_mode_white_led_parameter_power_string, power);
        mPowerText.setText(text);
    }

    private void setCurrent(float value) {
        int current = mLamp.getLedCurrent_mA(value);
        String text = getString(R.string.act_edit_mode_white_led_parameter_current_string, current);
        mCurrentText.setText(text);
    }

    private void setLifetime(float value) {
        int lifetime_min =  ((LampineHeadlamp)mLamp).getBatLifetime_min(value);
        int[] hh_mm = DataHelpers.getHoursAndMinutes(lifetime_min);
        String text = getString(R.string.act_edit_mode_white_battery_lifetime_string, hh_mm[0], hh_mm[1]);
        mLifetimeText.setText(text);
    }

    private void setWarningText(float value) {
        int maxRuntime = ((LampineHeadlamp)mLamp).getMaxIntensityRuntime(value);
        if (maxRuntime < 0) {
            mWarningText.setVisibility(View.INVISIBLE);
        } else {
            String text = getString(R.string.act_edit_mode_white_battery_warning_string, maxRuntime);
            mWarningText.setText(text);
            mWarningText.setVisibility(View.VISIBLE);

        }
    }
}
