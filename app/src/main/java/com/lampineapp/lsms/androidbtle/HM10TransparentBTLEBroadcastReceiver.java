package com.lampineapp.lsms.androidbtle;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lampineapp.lsms.LSMStack;
import com.lampineapp.lsms.layer1.LLayer1HardwareInterface;

import java.nio.charset.StandardCharsets;

public class HM10TransparentBTLEBroadcastReceiver extends BroadcastReceiver implements LLayer1HardwareInterface {
    private final static String TAG = HM10TransparentBTLEBroadcastReceiver.class.getSimpleName();

    private final static int MAX_SYMBOLS_PER_TRANSMISSION = 20;

    private ResponseListener mRespListener;

    private enum ConnectionState {DISCONNECTED, CONNECTED}
    private ConnectionState mConnectionState;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private String mDeviceAddress;
    private String mDeviceName;

    public HM10TransparentBTLEBroadcastReceiver() {
        mDeviceName = "";
        mDeviceAddress = "";
    }

    public HM10TransparentBTLEBroadcastReceiver(String deviceName, String deviceAddress) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {

            case BluetoothLeService.ACTION_DATA_AVAILABLE:
                final byte[] data = (intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

                if (mRespListener == null) {
                    Log.e(TAG, "No ResponseListener set");
                } else {
                    mRespListener.onResponse(data);
                    Log.d(TAG, "Received: \"" + new String(data, StandardCharsets.US_ASCII) + "\"");
                }
                break;

            case BluetoothLeService.ACTION_GATT_CONNECTED:
                mConnectionState = ConnectionState.CONNECTED;
                break;

            case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                mConnectionState = ConnectionState.DISCONNECTED;
                break;
            case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                setupSerial();
                break;
        }
    }

    @Override
    public void transmit(byte[] data) throws IllegalArgumentException {
        Log.d(TAG, "Sending: " + new String(data, StandardCharsets.US_ASCII));
        if (data.length > MAX_SYMBOLS_PER_TRANSMISSION) {
            throw new IllegalArgumentException();
        }
        characteristicTX.setValue(data);
        mBluetoothLeService.writeCharacteristic(characteristicTX);
    }

    @Override
    public void setOnResponseListener(ResponseListener listener) {
        mRespListener = listener;
    }

    @Override
    public int getMaxTxSize() {
        return MAX_SYMBOLS_PER_TRANSMISSION;
    }

    @Override
    public boolean isConnected() {
        if (mBluetoothLeService != null) {
            return mBluetoothLeService.isConnected(mDeviceAddress);
        }
        return false;
    }

    private void setupSerial() {
        String uuid;
        String unknownServiceString = "Unknown service";

        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            uuid = gattService.getUuid().toString();
            // If the service exists for HM 10 Serial, say so.
            if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
                break;
            }
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            mBluetoothLeService.connect(mDeviceAddress);
            //  mConnectionState = ConnectionState.CONNECTED;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void bindToDevice(Context context, String name, String address)
    {
        mDeviceName = name;
        mDeviceAddress = address;
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, context.BIND_AUTO_CREATE);
        context.registerReceiver(this, makeGattUpdateIntentFilter());

    }

    public void connect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }


}
