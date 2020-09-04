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
import androidx.fragment.app.FragmentActivity;

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
	private ImageButton mButtonLiveControlLamp, mButtonConfigureLamp, mButtonDisplayLampInfo;
	private Fragment mCurrentUiAreaFragment, mLastUiAreaFragment;
	private ActionBar mActionBar;

	private final static String TAG = ActivityLampConnected.class
			.getSimpleName();

	// TODO: CHECK IMPLEMENTATION
	private String receiveBuffer;
	private boolean receiveBufferEmpty = true;
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
				// TODO: CHECK IMPLEMENTATION RECEIVER
				receiveBuffer = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
				Log.d(TAG,"Received data: \"" + receiveBuffer + "\"");
				mSerialReceiveCallbackFunction.onSerialDataReceived(receiveBuffer);
			}
		}
	};

	public String readAndClearRxBuffer() {
		if (receiveBufferEmpty)	{
			return null;
		} else {
			receiveBufferEmpty = true;
			return receiveBuffer;
		}
	}

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

		// TODO: causes crash
		// Default view: live control
		//replaceHighlightedNavigationButton(mButtonLiveControlLamp);
		//replaceCurrentUiAreaFragment(new FragmentLiveControlLamp());

		// Get UI elements, define listeners
		mButtonLiveControlLamp = findViewById(R.id.button_lamp_live_control);
		mButtonLiveControlLamp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonLiveControlLamp);
				replaceCurrentUiAreaFragment(new FragmentLiveControlLamp());
				//mActionBar.setSubtitle(R.string.title_live_control_lamp);
			}
		});
		mButtonConfigureLamp = findViewById(R.id.button_configure_lamp);
		mButtonConfigureLamp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonConfigureLamp);
				replaceCurrentUiAreaFragment(new FragmentConfigureLamp());
				//mActionBar.setSubtitle(R.string.title_configure_lamp);
			}
		});
		mButtonDisplayLampInfo = findViewById(R.id.button_lamp_info);
		mButtonDisplayLampInfo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				replaceHighlightedNavigationButton(mButtonDisplayLampInfo);
				replaceCurrentUiAreaFragment(new FragmentDisplayLampInfo());
				//mActionBar.setSubtitle(R.string.title_lamp_info);

			}
		});

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

	private void displayData(String data) {
		if (data != null) {
			//mDataField.setText(data);
			
			int nlIdx = data.indexOf('\n');  // index of newline
			
			// blechat
			// add received data to screen

		}
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

	// blechat
	
	// btnClick
	// 
	// Click handler for Send button
	//
	public void btnClick(View view) {
		sendSerial();
	}

	// blechat
	//
	// sendSerial
	//
	// Send string io out field
	// TODO: REMOVE OR MERGE WITH sendSerialString(String str)
	private void sendSerial() {
		TextView view = (TextView) findViewById(R.id.edit_text_out); 
		String message = view.getText().toString() + "\r \n";

		Log.d(TAG, "Sending: \"" + message + "\"");
		final byte[] tx = message.getBytes();
		if (mConnected) {
			characteristicTX.setValue(tx);
			mBluetoothLeService.writeCharacteristic(characteristicTX);
		}
	}

	protected void sendSerialString(String string) {
		// TODO: THIS METHOD SEEMS ONLY BE CAPABLE OF SENDING 20 BYTES IN A ROW!!!!
		final String message = string;
		Log.d(TAG, "Sending: " + message);
		final byte[] tx = message.getBytes();
		if (mConnected) {
			characteristicTX.setValue(tx);
			mBluetoothLeService.writeCharacteristic(characteristicTX);
		}
	}

	protected interface SerialReceiveCallbackFunction {
		void onSerialDataReceived(String data);
	}

	protected void setSerialReceiveCallbackFunction(SerialReceiveCallbackFunction callbackFunction) {
		mSerialReceiveCallbackFunction = callbackFunction;
	}

	private void replaceCurrentUiAreaFragment(Fragment fragment) {
		FragmentManager fm = getFragmentManager();

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

}
