package com.lampineapp.lsms.androidbtle;

import android.bluetooth.BluetoothDevice;
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

import com.lampineapp.lsms.layer1.LMSLayer1HardwareInterface;

public class HM10TransparentBTLEBroadcastReceiver extends BroadcastReceiver implements LMSLayer1HardwareInterface {
    private final static String TAG = HM10TransparentBTLEBroadcastReceiver.class.getSimpleName();

    private final static int MAX_SYMBOLS_PER_TRANSMISSION = 20;

    private ReceiveListener mRespListener;

    private enum ConnectionState {DISCONNECTED, CONNECTED}
    private ConnectionState mConnectionState;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private static BluetoothDevice mDevice;

    public HM10TransparentBTLEBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {

            case BluetoothLeService.ACTION_DATA_AVAILABLE:
                final byte[] data = (intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

                if (mRespListener == null) {
                    Log.e(TAG, "No ResponseListener set");
                } else {
                    mRespListener.onReceive(data);
                }
                break;

            case BluetoothLeService.ACTION_GATT_CONNECTED:
                mConnectionState = ConnectionState.CONNECTED;
                boolean succ = setConnectionPriorityToHigh();
                Log.d(TAG, "Connection priority high: " + succ);
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
    public void transmit(byte[] data) {
        try {
            characteristicTX.setValue(data);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        } catch (Exception e) {};
    }

    @Override
    public void setOnResponseListener(ReceiveListener listener) {
        mRespListener = listener;
    }

    @Override
    public int getMaxTxSize() {
        return MAX_SYMBOLS_PER_TRANSMISSION;
    }

    @Override
    public boolean isConnected() {
        if (mBluetoothLeService != null) {
            return mBluetoothLeService.isConnected(mDevice);
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
            mBluetoothLeService.connect(mDevice);
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

    public void bindToDevice(Context context)
    {
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, context.BIND_AUTO_CREATE);
    }

    public void unbindFromDevice(Context context)
    {
        context.unbindService(getServiceConnection());
    }

    public void registerReceiver(Context context) {
        context.registerReceiver(this, makeGattUpdateIntentFilter());
    }

    public void unregisterReceiver(Context context)
    {
        context.unregisterReceiver(this);
    }

    public void connect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDevice);
        }
    }

    public void disconnect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public boolean setConnectionPriorityToHigh() {
        if (mBluetoothLeService == null) {
            return false;
        }
        return mBluetoothLeService.setConnectionPriorityToHigh();
    }

    public static void setDevice(BluetoothDevice device)
    {
        mDevice = device;
    }

    public static BluetoothDevice getDevice()
    {
        return mDevice;
    }


}
