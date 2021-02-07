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

import android.app.Activity;
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

import com.btle.BluetoothLeService;
import com.btle.LampineBluetoothLeTransmitter;

import java.util.ArrayList;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class ActivityLampConnected extends AppCompatActivity {

	private ProgressDialog mWaitForLampConnectedDialog;
	private AlertDialog mConnectionLostDialog;
	private int reconnectTryCounter = 0;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private ImageButton mButtonLiveControlLamp, mButtonConfigureLamp, mButtonDisplayLampInfo, mButtonLampConsole;
	private Fragment mCurrentUiAreaFragment, mLastUiAreaFragment;
	private ActionBar mActionBar;

	private final static String TAG = ActivityLampConnected.class.getSimpleName();

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	// Fragments of activity
	private FragmentLiveControlLamp mFragmentLiveControlLamp = new FragmentLiveControlLamp();
	private FragmentConfigureLamp mFragmentConfigureLamp = new FragmentConfigureLamp();
	private FragmentDisplayLampInfo mFragmentDisplayLampInfo = new FragmentDisplayLampInfo();
	private FragmentLampConsole mFragmentLampConsole = new FragmentLampConsole();

	private LampineBluetoothLeTransmitter mLampineBluetoothLeTransmitter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// get selected device's data
		final Intent intent = getIntent();
		//mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		//mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		//mActivity = this;
		// setup view
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lamp_connected);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(intent.getStringExtra(EXTRAS_DEVICE_NAME));
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

		// TODO: MOVE TO RUNNABLE??

		// Wait for connection (re)established dialog
		mWaitForLampConnectedDialog = new ProgressDialog(this,R.style.AppTheme_ProgressDialog);
		mWaitForLampConnectedDialog.setTitle(getResources().getString(R.string.Dialog_WaitForLampConn_title));
		mWaitForLampConnectedDialog.setMessage(getResources().getString(R.string.Dialog_WaitForLampConn_msg));
		mWaitForLampConnectedDialog.setCancelable(false);
		mWaitForLampConnectedDialog.setIcon(R.drawable.ic_bluetooth_searching_24dp);
		mWaitForLampConnectedDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				mWaitForLampConnectedDialog.cancel();
				// Exit to previous activity
				finish();
				return;
			}
		});


		// Connection lost dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLampConnected.this,R.style.AppTheme_AlertDialog);
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
		mConnectionLostDialog = builder.create();

		final Handler handler = new Handler();
		final Runnable r = new Runnable() {
			public void run() {
				if (mLampineBluetoothLeTransmitter == null) {
					mWaitForLampConnectedDialog.show();
					handler.postDelayed(this, 200);
					return;
				}
				if (!mLampineBluetoothLeTransmitter.isConnected()) {
					mWaitForLampConnectedDialog.show();
					mLampineBluetoothLeTransmitter.connect();
					reconnectTryCounter++;
					if (reconnectTryCounter >= 2000 / 200) {
						Log.d(TAG, "Connection timeout reached, giving up");
						mWaitForLampConnectedDialog.cancel();
						mConnectionLostDialog.show();
						// Do not post anymore
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


		if (!mConnected) {
			//mWaitForLampConnectedDialog.show();
		}

		// Display live control fragment
		replaceHighlightedNavigationButton(mButtonLiveControlLamp);
		replaceCurrentUiAreaFragment(mFragmentLiveControlLamp);

		// bind
		mLampineBluetoothLeTransmitter = new LampineBluetoothLeTransmitter(intent.getStringExtra(EXTRAS_DEVICE_NAME), intent.getStringExtra(EXTRAS_DEVICE_ADDRESS));
		mLampineBluetoothLeTransmitter.connect();
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mLampineBluetoothLeTransmitter.getServiceConnection(), BIND_AUTO_CREATE);
		registerReceiver(mLampineBluetoothLeTransmitter, mLampineBluetoothLeTransmitter.makeGattUpdateIntentFilter());
	}

	// blechat
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mLampineBluetoothLeTransmitter, mLampineBluetoothLeTransmitter.makeGattUpdateIntentFilter());
		//if (mBluetoothLeService != null) {
		//	final boolean result = mBluetoothLeService.connect(mDeviceAddress);
		//	Log.d(TAG, "Connect request result=" + result);
//
		//}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mLampineBluetoothLeTransmitter);
		//unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unbindService(mServiceConnection);
		unbindService(mLampineBluetoothLeTransmitter.getServiceConnection());
		//mBluetoothLeService = null;
		mLampineBluetoothLeTransmitter = null;
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
			//mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
		//	mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void replaceCurrentUiAreaFragment(Fragment fragment) {
		final FragmentManager fm = getFragmentManager();

		// get and destroy current fragment
		final Fragment currFragment = fm.findFragmentById(R.id.lamp_connected_ui_fragment_area);
		if (currFragment != null)
			fm.beginTransaction().remove(currFragment).commit();

		// replace with new fragment
		fm.beginTransaction().replace(R.id.lamp_connected_ui_fragment_area, fragment).commit();
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

	protected LampineBluetoothLeTransmitter getTransmitter() {
		return mLampineBluetoothLeTransmitter;
	}
}
