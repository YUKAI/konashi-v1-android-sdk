package com.uxxu.konashi.lib;

import java.util.ArrayList;

import com.uxxu.konashi.lib.BleDeviceSelectionDialog.OnBleDeviceSelectListener;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.os.Handler;
import android.os.Message;

public class KonashiManager implements BluetoothAdapter.LeScanCallback, OnBleDeviceSelectListener {
    
    private static final long SCAN_PERIOD = 3000;
    private static final String KONAHSI_DEVICE_NAME = "konashi#";
    
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private Handler mFindHandler;
    private Runnable mFindRunnable;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    private Boolean mIsShowKonashiOnly = true;
    
    private Activity mActivity;
    private BleDeviceSelectionDialog mDialog;
    
    public void initialize(Activity activity){
        mActivity = activity;
        
        mFindHandler = new Handler();
        mBleDeviceListAdapter = new BleDeviceListAdapter(mActivity);
        
        mBluetoothManager = (BluetoothManager)mActivity.getSystemService(mActivity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        
        mDialog = new BleDeviceSelectionDialog(mActivity, mBleDeviceListAdapter, this);
        
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
    
    public void find(){
        find(true);
    }
    
    public void find(Boolean isShowKonashiOnly){
        KonashiUtils.log("start");
        
        mIsShowKonashiOnly = isShowKonashiOnly;
        
        mFindHandler.postDelayed(mFindRunnable, SCAN_PERIOD);
        
        mBleDeviceListAdapter.clearDevices();
        
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
        
        mDialog.show();
    }
    
    private void stopFindHandler(){
        mFindHandler.removeCallbacks(mFindRunnable);
    }
    
    private void setStatus(BleStatus status) {
        mStatus = status;
    }
    
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        //KonashiUtils.log("DeviceName: " + device.getName());
        
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
        CLOSED
        ;
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
        
    }

    @Override
    public void onCancelSelectingBleDevice() {
        if(mStatus.equals(BleStatus.SCANNING)){
            stopFindHandler();
            mBluetoothAdapter.stopLeScan(KonashiManager.this);
            setStatus(BleStatus.DISCONNECTED);
        }
    }

}
