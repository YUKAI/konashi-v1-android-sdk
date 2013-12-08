package com.uxxu.konashi.lib;

import java.util.ArrayList;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.os.Handler;
import android.os.Message;

public class KonashiManager implements BluetoothAdapter.LeScanCallback {
    
    private static final long SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME = "konashi";
    
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private Handler mFindHandler;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    
    private Activity mActivity;
    
    public void initialize(Activity activity){
        mActivity = activity;
        
        mFindHandler = new Handler();
        mBleDeviceListAdapter = new BleDeviceListAdapter(mActivity);
        
        mBluetoothManager = (BluetoothManager)mActivity.getSystemService(mActivity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }
    
    public void find(){
        KonashiUtils.log("start");
        mFindHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(KonashiManager.this);
                if(mStatus.equals(BleStatus.SCANNING)){
                    setStatus(BleStatus.SCAN_FAILED);
                }
            }
        }, SCAN_PERIOD);
        
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
    }
    
    private void setStatus(BleStatus status) {
        mStatus = status;
        mFindHandler.sendMessage(status.message());
    }
    
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        //KonashiUtils.log("DeviceName: " + device.getName());
        
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBleDeviceListAdapter.addDevice(device);
                mBleDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private enum BleStatus {
        DISCONNECTED,
        SCANNING,
        SCAN_FAILED,
        DEVICE_FOUND,
        SERVICE_NOT_FOUND,
        SERVICE_FOUND,
        CHARACTERISTIC_NOT_FOUND,
        NOTIFICATION_REGISTERED,
        NOTIFICATION_REGISTER_FAILED,
        CLOSED
        ;
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }

}
