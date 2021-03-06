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
import android.bluetooth.BluetoothDevice;
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

import com.lampineapp.frag_configure_lamp.FragmentConfigureLampModes;
import com.lampineapp.frag_configure_lamp.whiteconfig.WhiteLampModesFile;
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

	private final static boolean NO_BTLE = false;

	private ProgressDialog mWaitForLampConnectedDialog;
	private AlertDialog mConnectionLostDialog;
	private int reconnectTryCounter = 0;

	private ImageButton mButtonLiveControlLamp, mButtonConfigureLamp, mButtonDisplayLampInfo, mButtonLampConsole;
	private Fragment mCurrentUiAreaFragment, mLastUiAreaFragment;
	private ActionBar mActionBar;

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;

	// Fragments of activity
	private FragmentLiveControlLamp mFragmentLiveControlLamp = new FragmentLiveControlLamp();
	private FragmentConfigureLampModes mFragmentConfigureLamp = new FragmentConfigureLampModes();
	private FragmentDisplayLampInfo mFragmentDisplayLampInfo = new FragmentDisplayLampInfo();
	private FragmentLampConsole mFragmentLampConsole = new FragmentLampConsole();
	
	// Handler
	final Handler mConnectionRetryHandler = new Handler();
	final Handler mConnectionPriorityHandler = new Handler();

	// Runner to update connection priority after a short delay after connection is established to decrease connection interval.
	final Runnable mConnectionPriorityRunner = new Runnable() {
		public void run() {
			if (mHwInterface.isConnected()) {
				boolean succ = mHwInterface.setConnectionPriorityToHigh();
				Log.d(TAG, "Connection priority high: " + succ);
			}
		}
	};

	// Runnable for retry of connection
	final Runnable mConnectionRetryRunner = new Runnable() {
		public void run() {
			if (NO_BTLE) {
				mWaitForLampConnectedDialog.hide();
				return;
			}
			final int DELAY_MS = 500;
			if (mHwInterface == null) {
				mConnectionRetryHandler.postDelayed(this, DELAY_MS);
				return;
			}
			if (!mHwInterface.isConnected()) {
				mWaitForLampConnectedDialog.show();
				mHwInterface.connect();
				reconnectTryCounter++;
				if (reconnectTryCounter >= 8000 / DELAY_MS) {
					Log.d(TAG, "Connection timeout reached, giving up");
					mWaitForLampConnectedDialog.cancel();
					mConnectionLostDialog.show();
					// Do not post anymore
					// TODO: Auto reconnect?
					return;
				}
				mConnectionRetryHandler.postDelayed(this, DELAY_MS);
			} else {
				if (mWaitForLampConnectedDialog.isShowing()) {
					// Post multiple times after connection is established to make sure connection priority is switched.
					for (int i = 1; i < 5; i++) {
						mConnectionPriorityHandler.postDelayed(mConnectionPriorityRunner, i * 1000);
					}
				}
				mWaitForLampConnectedDialog.hide();
				reconnectTryCounter = 0;
				mConnectionRetryHandler.postDelayed(this, DELAY_MS);

			}
		}
	};

	private HM10TransparentBTLEBroadcastReceiver mHwInterface;
	private static BluetoothDevice mDevice;
	private LSMStack mLSMStack;

	protected LSMStack getStack() {
		return mLSMStack;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get selected device's data
		final Intent intent = getIntent();
		mDevice = HM10TransparentBTLEBroadcastReceiver.getDevice();

		// Build GUI and display live control fragment
		buildGui(savedInstanceState);
		mConnectionLostDialog = buildConnectionLostDialog();
		mWaitForLampConnectedDialog = buildWaitForLampConnectedDialog();
		replaceHighlightedNavigationButton(mButtonLiveControlLamp);
		replaceCurrentUiAreaFragment(mFragmentLiveControlLamp);

		// Build connection to lamp
		mHwInterface = new HM10TransparentBTLEBroadcastReceiver();
		mHwInterface.bindToDevice(this);
		mHwInterface.registerReceiver(this);
		if (!NO_BTLE) {
			mHwInterface.connect();
		}
		mConnectionRetryHandler.postDelayed(mConnectionRetryRunner, 10);
		mLSMStack = new LSMStack(mHwInterface);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHwInterface.registerReceiver(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHwInterface.unregisterReceiver(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHwInterface.unbindFromDevice(this);
		mHwInterface.disconnect();
		mWaitForLampConnectedDialog.cancel();
		mConnectionLostDialog.cancel();
		mConnectionPriorityHandler.removeCallbacks(mConnectionPriorityRunner);
		mConnectionRetryHandler.removeCallbacks(mConnectionRetryRunner);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			WhiteLampModesFile.erase(this);
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

	public LSMStack getLSMStack() {
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
		mActionBar.setTitle(mDevice.getName());
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
