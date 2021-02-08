package com.lampineapp;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.btle.BluetoothLeService;
import com.btle.SampleGattAttributes;
import com.lampineapp.ActivityLampConnected;
import com.lampineapp.R;

public class LampineTransmitter extends android.content.BroadcastReceiver {

    private final static String TAG = ActivityLampConnected.class.getSimpleName();
    enum ConnectionState {DISCONNECTED, CONNECTED} ConnectionState mConnectionState;
    enum ReceiverState { READY_TO_RECEIVE, WAITING_FOR_ACK} ReceiverState mReceiverState;
    private String mRxDataBuffer = "";
    private String mRxAckBuffer = "";
    SerialReceiveCallbackFunction mSerialReceiveCallbackFunction;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private String mDeviceName;

    public LampineTransmitter(String deviceName, String deviceAddress) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
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
            // TODO: REMOVE
            mConnectionState = ConnectionState.CONNECTED;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            mConnectionState = ConnectionState.CONNECTED;

        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                .equals(action)) {
            mConnectionState = ConnectionState.DISCONNECTED;
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                .equals(action)) {
            setupSerial();
            mReceiverState = ReceiverState.READY_TO_RECEIVE;
        } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            // Call receiver
            handleSerialDataRx(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
        }
    }

    private void handleSerialDataRx(String data) {

        Log.d(TAG,"Received data: \"" + data + "\"");
        switch (mReceiverState) {
            case READY_TO_RECEIVE:
                if (data.contains("\r\n")) {
                    // Send back ACK + data + \r\n
                    if (data.length() > 2) {
                        mRxDataBuffer += data.substring(1, data.lastIndexOf('\r') - 1);
                    }
                    sendSerialString("ACK" + mRxDataBuffer + "\r\n");
                    mReceiverState = ReceiverState.WAITING_FOR_ACK;
                } else {
                    mRxDataBuffer += data;
                }
                break;
            case WAITING_FOR_ACK:
                mRxAckBuffer += data;
                if (mRxAckBuffer.contains("ACK\r\n")) {
                    if (mSerialReceiveCallbackFunction != null) mSerialReceiveCallbackFunction.onSerialDataReceived(mRxDataBuffer);
                    mReceiverState = ReceiverState.READY_TO_RECEIVE;
                    mRxAckBuffer = "";
                    mRxDataBuffer = "";
                    break;
                }
                if (mRxAckBuffer.length() >= 5) {
                    // No ACK received. Dump old data, assume received data is new resend data
                    mRxDataBuffer = "";
                    mRxDataBuffer = mRxAckBuffer;
                    mRxAckBuffer = "";
                    mReceiverState = ReceiverState.READY_TO_RECEIVE;
                    break;
                }
        }
    }

    // setupSerial
    //
    // set serial characteristics
    //
    private void setupSerial() {

        // blechat - set serial characteristics
        String uuid;
        String unknownServiceString = "Unknown service";

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

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void sendSerialString(String string) {
        Log.d(TAG, "Sending: " + string);
        String str = string;
        final int PACK_SIZE = 20;
        // Transmit in packages of 20, since characteristic cannot exceed 20 bytes
        while (str.length() > PACK_SIZE) {
            // Transmit first 20 chars per iteration
            if (mConnectionState == ConnectionState.CONNECTED) {
                characteristicTX.setValue(str.substring(0, PACK_SIZE-1).getBytes());
                mBluetoothLeService.writeCharacteristic(characteristicTX);
            } else {
                return;
            }
            str = str.substring(PACK_SIZE);
            sleep_ms(40);
        }
        // Transmit rest of chars
        if (mConnectionState == ConnectionState.CONNECTED) {
            characteristicTX.setValue(str.getBytes());
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        }	else {
            return;
        }
    }

    public interface SerialReceiveCallbackFunction {
        void onSerialDataReceived(String data);
    }

    public void setSerialReceiveCallbackFunction(SerialReceiveCallbackFunction callbackFunction) {
        mSerialReceiveCallbackFunction = callbackFunction;
    }

    private void sleep_ms(int time_ms) {
        try {
            Thread.sleep(time_ms);
        } catch (Exception e) {
            // TODO: catch
        }
    }

    public void connect()
    {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    public boolean isConnected() {

        if (mBluetoothLeService != null) {
            return mBluetoothLeService.isConnected(mDeviceAddress);
        } else {
            return false;
        }
    }
};