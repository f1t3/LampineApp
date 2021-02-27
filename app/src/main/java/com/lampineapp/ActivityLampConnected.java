/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lampineapp;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import android.app.Fragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lampineapp.lsms.LSMStack;
import com.lampineapp.lsms.androidbtle.HM10TransparentBTLEBroadcastReceiver;

import java.util.ArrayList;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class ActivityLampConnected extends AppCompatActivity {
	private final static String TAG = ActivityLampConnected.class.getSimpleName();

	private ProgressDialog mWaitForLampConnectedDialog;
	private AlertDialog mConnectionLostDialog;
	private int reconnectTryCounter = 0;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private ImageButton mButtonLiveControlLamp, mButtonConfigureLamp, mButtonDisplayLampInfo, mButtonLampConsole;
	private Fragment mCurrentUiAreaFragment, mLastUiAreaFragment;
	private ActionBar mActionBar;

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;

	// Fragments of activity
	private FragmentLiveControlLamp mFragmentLiveControlLamp = new FragmentLiveControlLamp();
	private FragmentConfigureLamp mFragmentConfigureLamp = new FragmentConfigureLamp();
	private FragmentDisplayLampInfo mFragmentDisplayLampInfo = new FragmentDisplayLampInfo();
	private FragmentLampConsole mFragmentLampConsole = new FragmentLampConsole();

	private HM10TransparentBTLEBroadcastReceiver mHwInterface;
	private LSMStack mLSMStack;
	private String mDeviceName;
	private String mDeviceAddress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get selected device's data
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// Build GUI and display live control fragment
		buildGui(savedInstanceState);
		mConnectionLostDialog = buildConnectionLostDialog();
		mWaitForLampConnectedDialog = buildWaitForLampConnectedDialog();
		replaceHighlightedNavigationButton(mButtonLiveControlLamp);
		replaceCurrentUiAreaFragment(mFragmentLiveControlLamp);

		// Build connection to lamp
		final HM10TransparentBTLEBroadcastReceiver mHwInterface = new HM10TransparentBTLEBroadcastReceiver();
		mHwInterface.bindToDevice(this, mDeviceName, mDeviceAddress);
		mHwInterface.connect();
		mLSMStack = new LSMStack(mHwInterface);

		final Handler handler = new Handler();
		final Runnable r = new Runnable() {
			public void run() {
				if (!mHwInterface.isConnected()) {
					mWaitForLampConnectedDialog.show();
					mHwInterface.connect();
					reconnectTryCounter++;
					if (reconnectTryCounter >= 2000 / 200) {
						Log.d(TAG, "Connection timeout reached, giving up");
						mWaitForLampConnectedDialog.cancel();
						mConnectionLostDialog.show();
						// Do not post anymore
						// TODO: Auto reconnect?
						return;
					}
					handler.postDelayed(this, 200);
				} else {
					reconnectTryCounter = 0;
					mWaitForLampConnectedDialog.hide();
					handler.postDelayed(this, 200);
				}
			}
		};
		handler.postDelayed(r, 200);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//registerReceiver(mLampineBluetoothLeTransmitter, mLampineBluetoothLeTransmitter.makeGattUpdateIntentFilter());
	}

	@Override
	protected void onPause() {
		super.onPause();
		//unregisterReceiver(mLampineBluetoothLeTransmitter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO: Move to hwInterface
		unbindService(mHwInterface.getServiceConnection());
		mHwInterface = null;
		mLSMStack = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			return true;
		case R.id.menu_disconnect:
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void replaceCurrentUiAreaFragment(Fragment fragment) {
		final FragmentManager fm = getFragmentManager();

		// Get and destroy current fragment
		final Fragment currFragment = fm.findFragmentById(R.id.lamp_connected_ui_fragment_area);
		if (currFragment != null)
			fm.beginTransaction().remove(currFragment).commit();

		// Replace with new fragment
		fm.beginTransaction().replace(R.id.lamp_connected_ui_fragment_area, fragment).commit();
	}

	protected LSMStack getLSMStack() {
		return mLSMStack;
	}

	private void replaceHighlightedNavigationButton(ImageButton button) {
		// Set all icons inactive
		final int colorInactive = this.getColor(R.color.colorIconInactive);
		mButtonLiveControlLamp.setColorFilter(colorInactive);
		mButtonConfigureLamp.setColorFilter(colorInactive);
		mButtonDisplayLampInfo.setColorFilter(colorInactive);
		// Set desired icon active
		button.setColorFilter(this.getColor(R.color.colorIconActive));
	}

	private void buildGui(Bundle savedInstanceState) {
		// Setup view
		setContentView(R.layout.activity_lamp_connected);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(mDeviceName);
		//mActionBar.setSubtitle(R.string.title_live_control_lamp);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Get UI elements, define listeners
		mButtonLiveControlLamp = findViewById(R.id.button_lamp_live_control);
		mButtonLiveControlLamp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonLiveControlLamp);
				replaceCurrentUiAreaFragment(mFragmentLiveControlLamp);
				//mActionBar.setSubtitle(R.string.title_live_control_lamp);
			}
		});
		mButtonConfigureLamp = findViewById(R.id.button_configure_lamp);
		mButtonConfigureLamp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonConfigureLamp);
				replaceCurrentUiAreaFragment(mFragmentConfigureLamp);
				//mActionBar.setSubtitle(R.string.title_configure_lamp);
			}
		});
		mButtonDisplayLampInfo = findViewById(R.id.button_lamp_info);
		mButtonDisplayLampInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonDisplayLampInfo);
				replaceCurrentUiAreaFragment(mFragmentDisplayLampInfo);
				//mActionBar.setSubtitle(R.string.title_lamp_info);

			}
		});
		mButtonLampConsole = findViewById(R.id.button_lamp_console);
		mButtonLampConsole.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				replaceHighlightedNavigationButton(mButtonLampConsole);
				replaceCurrentUiAreaFragment(mFragmentLampConsole);
			}
		});
	}

	private ProgressDialog buildWaitForLampConnectedDialog() {
		// Wait for connection (re)established dialog
		final ProgressDialog dialog = new ProgressDialog(this, R.style.AppTheme_ProgressDialog);
		dialog.setTitle(getResources().getString(R.string.Dialog_WaitForLampConn_title));
		dialog.setMessage(getResources().getString(R.string.Dialog_WaitForLampConn_msg));
		dialog.setCancelable(false);
		dialog.setIcon(R.drawable.ic_bluetooth_searching_24dp);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialog.cancel();
				// Exit to previous activity
				finish();
				return;
			}
		});
		return dialog;
	}

	private AlertDialog buildConnectionLostDialog() {
		// Connection lost dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLampConnected.this, R.style.AppTheme_AlertDialog);
		builder.setTitle(getResources().getString(R.string.lamp_connection_lost))
				.setMessage(getResources().getString(R.string.lamp_connection_lost_msg))
				.setCancelable(false)
				.setIcon(R.drawable.ic_bluetooth_disabled_black_24dp)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// Exit to previous activity
						finish();
						return;
					}
				});
		return builder.create();
	}
}
