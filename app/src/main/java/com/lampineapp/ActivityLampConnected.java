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

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import android.app.Fragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.btle.BluetoothLeService;
import com.btle.SampleGattAttributes;

import java.util.ArrayList;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class ActivityLampConnected extends AppCompatActivity {

	private DialogWithTwoButtons waitForLampConnectedDialog;

	private Activity mActivity;
	private ImageButton mButtonLiveControlLamp, mButtonConfigureLamp, mButtonDisplayLampInfo, mButtonLampConsole;
	private Fragment mCurrentUiAreaFragment, mLastUiAreaFragment;
	private ActionBar mActionBar;

	private final static String TAG = ActivityLampConnected.class
			.getSimpleName();

	// TODO: CHECK IMPLEMENTATION
	private String mReceiveBuffer = "";
	SerialReceiveCallbackFunction mSerialReceiveCallbackFunction;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	private String mDeviceAddress;
	private String mDeviceName;

	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	// blechat - characteristics for HM-10 serial
	private BluetoothGattCharacteristic characteristicTX;
	private BluetoothGattCharacteristic characteristicRX;

	// Fragments of activity
	private FragmentLiveControlLamp mFragmentLiveControlLamp = new FragmentLiveControlLamp();
	private FragmentConfigureLamp mFragmentConfigureLamp = new FragmentConfigureLamp();
	private FragmentDisplayLampInfo mFragmentDisplayLampInfo = new FragmentDisplayLampInfo();
	private FragmentLampConsole mFragmentLampConsole = new FragmentLampConsole();

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);

			while (!mBluetoothLeService.isConnected(mDeviceAddress)) {
				sleep_ms(2000);
				mBluetoothLeService.connect(mDeviceAddress);
			}
			if (waitForLampConnectedDialog != null) {
				waitForLampConnectedDialog.cancel();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.

				// blechat
				// set serial chaacteristics
				setupSerial();

				// displayGattServices(mBluetoothLeService
				// .getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				// Receiver
				final String receivedMessage = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
				Log.d(TAG,"Received data: \"" + receivedMessage + "\"");

				// Notify listeners only when line is completed,
				// otherwise attach new message to buffer
				mReceiveBuffer += receivedMessage;
				if (receivedMessage.endsWith("\r\n")) {
					if (mSerialReceiveCallbackFunction != null) {
						mSerialReceiveCallbackFunction.onSerialDataReceived(mReceiveBuffer);
					}
					mReceiveBuffer = "";
				}
			}
		}
	};

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.
	private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(groupPosition).get(childPosition);
				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					// If there is an active notification on a characteristic,
					// clear
					// it first so it doesn't update the data field on the user
					// interface.
					if (mNotifyCharacteristic != null) {
						mBluetoothLeService.setCharacteristicNotification(
								mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					mBluetoothLeService.readCharacteristic(characteristic);
				}
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
				}

				return true;
			}
			return false;
		}
	};

	private void clearUI() {
		//mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		//mDataField.setText(R.string.no_data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// get selected device's data
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// setup view
		super.onCreate(savedInstanceState);
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

		waitForLampConnectedDialog = new DialogWithTwoButtons(this,
				getString(R.string.connectong_to_lamp), "OK", "",
				true) {
			@Override
			void onPositiveBtnClick() {
				// Request permission.
				this.cancel();
			}

			@Override
			void onNegativeBtnClick() {
				return;
			}
		};

		// Display live control fragment
		replaceHighlightedNavigationButton(mButtonLiveControlLamp);
		replaceCurrentUiAreaFragment(mFragmentLiveControlLamp);

		// bind service
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	// setupSerial
	//
	// set serial characteristics
	//
	private void setupSerial() {

		// blechat - set serial characteristics
		String uuid;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);

		for (BluetoothGattService gattService : mBluetoothLeService
				.getSupportedGattServices()) {
			// HashMap<String, String> currentServiceData = new HashMap<String,
			// String>();
			uuid = gattService.getUuid().toString();

			// If the service exists for HM 10 Serial, say so.
			if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {

				// get characteristic when UUID matches RX/TX UUID
				characteristicTX = gattService
						.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
				characteristicRX = gattService
						.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);

				mBluetoothLeService.setCharacteristicNotification(
						characteristicRX, true);
				break;

			} // if

		} // for
	}

	// blechat
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
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
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO: CLEANUP
				//mConnectionState.setText(resourceId);
			}
		});
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	protected void sendSerialString(String string) {
		Log.d(TAG, "Sending: " + string);
		String str = string;
		final int PACK_SIZE = 20;
		// Transmit in packages of 20, since characteristic cannot exceed 20 bytes
		while (str.length() > PACK_SIZE) {
			// Transmit first 20 chars per iteration
			if (mConnected) {
				characteristicTX.setValue(str.substring(0, PACK_SIZE-1).getBytes());
				mBluetoothLeService.writeCharacteristic(characteristicTX);
			} else {
				return;
			}
			str = str.substring(PACK_SIZE);
			sleep_ms(40);
		}
		// Transmit rest of chars
		if (mConnected) {
			characteristicTX.setValue(str.getBytes());
			mBluetoothLeService.writeCharacteristic(characteristicTX);
		}	else {
			return;
		}
	}

	protected interface SerialReceiveCallbackFunction {
		void onSerialDataReceived(String data);
	}

	protected void setSerialReceiveCallbackFunction(SerialReceiveCallbackFunction callbackFunction) {
		mSerialReceiveCallbackFunction = callbackFunction;
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

	private void sleep_ms(int time_ms) {
		try {
			Thread.sleep(time_ms);
		} catch (Exception e) {
			// TODO: catch
		}
	}
}
