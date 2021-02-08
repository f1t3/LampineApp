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

    final int CHECKSUM_BYTES = 4;
    final int TRAIL_BYTES = 2;
    final int DATA_BYTES = 14;
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
                mRxDataBuffer += data;
                if (mRxDataBuffer.endsWith("\r\n")) {
                    if (checkStringAgainstChecksum(mRxDataBuffer)) {
                        sendSerialString("ACK");
                        if (mSerialReceiveCallbackFunction != null)
                            mSerialReceiveCallbackFunction.onSerialDataReceived(mRxDataBuffer);
                    } else {
                        sendSerialString("NACK");
                    }
                    mReceiverState = ReceiverState.READY_TO_RECEIVE;
                    mRxDataBuffer = "";
                    break;
                }
        }
    }

    private boolean checkStringAgainstChecksum(String str) {
        // Sanity check
        if (str.length() < 7)
            return false;
        String rxStr = str.substring(0, str.length() - CHECKSUM_BYTES - TRAIL_BYTES);
        String sumStr = str.substring(str.length() - CHECKSUM_BYTES - TRAIL_BYTES, str.length() - TRAIL_BYTES);
        if (getChecksumFromStringAsString(rxStr).equals(sumStr)) {
            return true;
        } else {
            return false;
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
        final int PACK_SIZE = 14;
        // Transmit in data packages of 14
        while (str.length() > PACK_SIZE) {
            // Transmit first 14 chars per iteration
            if (mConnectionState == ConnectionState.CONNECTED) {
                String txStr = str.substring(0, PACK_SIZE-1);
                txStr += getChecksumFromStringAsString(txStr) + "\r\n";
                characteristicTX.setValue(txStr.getBytes());
                mBluetoothLeService.writeCharacteristic(characteristicTX);
            } else {
                return;
            }
            str = str.substring(PACK_SIZE);
            // Make sure receiver has read message
            // TODO: WAIT FOR ACK!
            sleep_ms(40);
        }
        // Transmit rest of chars
        if (mConnectionState == ConnectionState.CONNECTED) {
            str += getChecksumFromStringAsString(str) + "\r\n";
            characteristicTX.setValue(str.getBytes());
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        }	else {
            return;
        }
    }

    private String getChecksumFromStringAsString(String str) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            sum += (int)str.charAt(i);
        }
        if (sum > 9999) sum = 0;
        return String.format("%04d", sum);
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