package com.uxxu.konashi.lib;

import com.uxxu.konashi.lib.BleDeviceSelectionDialog.OnBleDeviceSelectListener;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class KonashiManager implements BluetoothAdapter.LeScanCallback, OnBleDeviceSelectListener {
    
    // konashi service UUID
    public static final String DEVICE_KONASHI_SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb";
   
    // konashi characteristics
    public static final String CHARACTERISTIC_KONASHI_PIO_SETTING_UUID            = "00003000-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PIO_PULLUP_UUID             = "00003001-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PIO_OUTPUT_UUID             = "00003002-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PIO_INPUT_NOTIFICATION_UUID = "00003003-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PWM_CONFIG_UUID             = "00003004-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PWM_PARAM_UUID              = "00003005-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_KONASHI_PWM_DUTY_UUID               = "00003006-0000-1000-8000-00805f9b34fb";
    
    // konashi characteristic config
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static final byte KONASHI_FAILURE = (byte)(0xff);

    // PIO
    public static final byte PIO0 = 0;
    public static final byte PIO1 = 1;
    public static final byte PIO2 = 2;
    public static final byte PIO3 = 3;
    public static final byte PIO4 = 4;
    public static final byte PIO5 = 5;
    public static final byte PIO6 = 6;
    public static final byte PIO7 = 7;
    public static final byte MODE_INPUT = 0;
    public static final byte MODE_OUTPUT = 1;
    public static final byte NO_PULLS = 0;
    public static final byte PULLUP   = 1;
    public static final byte LOW  = 0;
    public static final byte HIGH = 1;
    
    private static final long SCAN_PERIOD = 3000;
    private static final String KONAHSI_DEVICE_NAME = "konashi#";
    
    // konashi members
    private byte mPinModeSetting = 0;
    private byte mPioPullup = 0;
    private byte mPioInput = 0;
    private byte mPioOutput = 0;
    
    // BLE members
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private Handler mFindHandler;
    private Runnable mFindRunnable;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    private Boolean mIsShowKonashiOnly = true;
    
    // UI members
    private Activity mActivity;
    private BleDeviceSelectionDialog mDialog;
    
    public void initialize(Context context){        
        mFindHandler = new Handler();
        mBleDeviceListAdapter = new BleDeviceListAdapter(context);
        
        mBluetoothManager = (BluetoothManager)context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        
        mDialog = new BleDeviceSelectionDialog(mBleDeviceListAdapter, this);
        
        mFindRunnable = new Runnable() {
            @Override
            public void run() {
                if(mStatus.equals(BleStatus.SCANNING)){
                    mBluetoothAdapter.stopLeScan(KonashiManager.this);
                    setStatus(BleStatus.SCAN_END);
                    mDialog.finishFinding();
                }
            }
        };
    }
    
    public void find(Activity activity){
        find(activity, true);
    }
    
    public void find(Activity activity, Boolean isShowKonashiOnly){
        KonashiUtils.log("start");
        
        mActivity = activity;
        
        mIsShowKonashiOnly = isShowKonashiOnly;
        
        mFindHandler.postDelayed(mFindRunnable, SCAN_PERIOD);
        
        mBleDeviceListAdapter.clearDevices();
        
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
        
        mDialog.show(activity);
    }
    
    private void stopFindHandler(){
        mFindHandler.removeCallbacks(mFindRunnable);
    }
    
    private void setStatus(BleStatus status) {
        mStatus = status;
    }
    
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        KonashiUtils.log("DeviceName: " + device.getName());

        // runOnUiThread to be able to tap list element on BLE DeviceList dialog
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mIsShowKonashiOnly==false || device.getName().startsWith(KONAHSI_DEVICE_NAME)){
                    mBleDeviceListAdapter.addDevice(device);
                    mBleDeviceListAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    private enum BleStatus {
        DISCONNECTED,
        SCANNING,
        SCAN_END,
        DEVICE_FOUND,
        SERVICE_NOT_FOUND,
        SERVICE_FOUND,
        CHARACTERISTIC_NOT_FOUND,
        NOTIFICATION_REGISTERED,
        NOTIFICATION_REGISTER_FAILED,
        CLOSED;
        
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }

    @Override
    public void onSelectBleDevice(BluetoothDevice device) {    
        KonashiUtils.log("Selected device: " + device.getName());
        
        if(mStatus.equals(BleStatus.SCANNING)){
            stopFindHandler();
            mBluetoothAdapter.stopLeScan(KonashiManager.this);        
        }
        setStatus(BleStatus.DEVICE_FOUND);
        
        connect(device);
    }

    @Override
    public void onCancelSelectingBleDevice() {
        if(mStatus.equals(BleStatus.SCANNING)){
            stopFindHandler();
            mBluetoothAdapter.stopLeScan(KonashiManager.this);
            setStatus(BleStatus.DISCONNECTED);
        }
    }
    
    
    /******************************************
     * BLE functions
     ******************************************/
    
    private void connect(BluetoothDevice device){
        mBluetoothGatt = device.connectGatt(mActivity.getApplicationContext(), false, mBluetoothGattCallback);
    }
    
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // TODO Auto-generated method stub
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // TODO Auto-generated method stub
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // TODO Auto-generated method stub
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            KonashiUtils.log("onConnectionStateChange: " + status2string(status) + " -> " + status2string(newState));
            
            if(newState == BluetoothProfile.STATE_CONNECTED){
                gatt.discoverServices();
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                setStatus(BleStatus.DISCONNECTED);
                mBluetoothGatt = null;
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // TODO Auto-generated method stub
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // TODO Auto-generated method stub
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // TODO Auto-generated method stub
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            // TODO Auto-generated method stub
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // TODO Auto-generated method stub
            super.onServicesDiscovered(gatt, status);
        }
    };
    
    private String status2string(int status){
        switch(status){
            case BluetoothProfile.STATE_CONNECTED:
                return "CONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "CONNECTING";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "DISCONNECTED";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "DISCONNECTING";
            default:
                return "UNKNOWN";
        }
    }

}
