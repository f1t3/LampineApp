package com.lampineapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lampineapp.helper.GeneralHelpers;
import com.lampineapp.lsms.androidbtle.HM10TransparentBTLEBroadcastReceiver;

import java.util.ArrayList;

public class
ActivityConnectToLamp extends AppCompatActivity {

    private final static String TAG = ActivityConnectToLamp.class.getSimpleName();

    final boolean LIST_ALL_BTLE_DEVICES = true;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView mListView;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private Activity mActivity = this;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOC = 1;

    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 100000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_lamp);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_connect_to_lamp));
        }

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // List view
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView = findViewById(R.id.list_view_lampine_devices);
        mListView.setAdapter(mLeDeviceListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if (device == null)
                    return;
                final Intent intent = new Intent(mActivity, ActivityLampConnected.class);
                HM10TransparentBTLEBroadcastReceiver.setDevice(device);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);

        // Check if location permission is still active, start scan if so, stop scan if not.
        if (checkAndGetLocationPermission() == true) {
            scanLeDevice(true);
        } else {
            scanLeDevice(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private boolean checkAndGetLocationPermission() {
        // Check if location permission has been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted.
            final DialogWithTwoButtons getLocPermissionDialog = new DialogWithTwoButtons(this,
                    getString(R.string.permit_location), "OK", "",
                    true) {
                @Override
                void onPositiveBtnClick() {
                    // Request permission.
                    this.cancel();
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_ENABLE_LOC);
                }

                @Override
                void onNegativeBtnClick() {
                    return;
                }
            };
        }

        // User should have given permission by now. Otherwise assume he denied.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            setTitle("Enable Location to detect device!");
            return false;
        } else {
            return true;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ActivityConnectToLamp.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                if (isValidLampineDevice(device)) {
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Reuse converted view if existing
            View v = convertView == null ? mInflator.inflate(R.layout.activity_connect_to_lamp_listitem, parent, false) : convertView;
            TextView devAddrTextView   = (TextView)  v.findViewById(R.id.device_address);
            TextView devNameTextView   = (TextView)  v.findViewById(R.id.device_name);
            ImageView devIconImageView = (ImageView) v.findViewById(R.id.device_icon);

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            devNameTextView.setText((deviceName != null && deviceName.length() > 0) ? deviceName : getResources().getString(R.string.unknown_device));
            devAddrTextView.setText(device.getAddress());
            devIconImageView.setImageDrawable(isValidLampineDevice(device) ? getDrawable(R.drawable.ic_headlight_svgrepo_com_24dp) : getDrawable(R.drawable.ic_help_outline_24dp));
            return v;
        }

        @Override
        public boolean isEnabled(int position)
        {
            return true;
        }
    }

    // Device scan callback, called once LE device is found. Check if Lampine device and
    // add to list if so.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isValidLampineDevice(device) || LIST_ALL_BTLE_DEVICES) {
                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    // Tests weather BTLE device is valid Lampine device
    private boolean isValidLampineDevice(BluetoothDevice device) {
        if (device == null)
            return false;
        final String name = device.getName();
        if (name == null)
            return false;
        if ( name.equals("LampineK10RGB_EVL1") || name.equals("Lampine-K9RGB-r1") || name.equals("JDY-10M") ) {
            return true;
        }
        return false;
    }

}
