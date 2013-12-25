package com.uxxu.konashi.lib;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.uxxu.konashi.lib.BleDeviceSelectionDialog.OnBleDeviceSelectListener;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * konashiを管理するメインクラス
 * 
 * @author monakaz, YUKAI Engineering
 * http://konashi.ux-xu.com
 * ========================================================================
 * Copyright 2013 Yukai Engineering Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class KonashiManager implements BluetoothAdapter.LeScanCallback, OnBleDeviceSelectListener, KonashiApiInterface {
    
    /*************************
     * konashi constants
     *************************/
      
    private static final long SCAN_PERIOD = 3000;
    private static final String KONAHSI_DEVICE_NAME = "konashi#";
    private static final long KONASHI_SEND_PERIOD = 10;
    
    private static final int PIO_LENGTH = 8;
    private static final int PWM_LENGTH = 8;
    private static final int AIO_LENGTH = 3;
    
    
    /*****************************
     * BLE constants
     *****************************/
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_STATE_CHANGE_BT = 2;
    
    private enum BleStatus {
        DISCONNECTED,
        SCANNING,
        SCAN_END,
        DEVICE_FOUND,
        CONNECTED,
        SERVICE_NOT_FOUND,
        SERVICE_FOUND,
        CHARACTERISTICS_NOT_FOUND,
        CHARACTERISTICS_FOUND,
        READY,
        CLOSED;
        
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }
    
    
    /*****************************
     * Members
     *****************************/
    
    // FIFO buffer
    private Timer mFifoTimer;
    private ArrayList<KonashiMessage> mKonashiMessageList;
    private class KonashiMessage{
        public UUID uuid;
        public byte[] data;
        public KonashiMessage(UUID uuid, byte[] data){
            this.uuid = uuid;
            this.data = data;
        }
    }
    
    // konashi members
    // PIO
    private byte mPioModeSetting = 0;
    private byte mPioPullup = 0;
    private byte mPioInput = 0;
    private byte mPioOutput = 0;
    
    // PWM
    private byte mPwmSetting = 0;
    private int[] mPwmDuty;
    private int[] mPwmPeriod;
    
    // AIO
    private int[] mAioValue;
    
    // I2C
    private byte mI2cSetting;
    private byte[] mI2cReadData;
    private byte mI2cReadDataLength;
    private byte mI2cReadAddress;
    
    // UART
    private byte mUartSetting;
    private byte mUartBaudrate;
    private byte mUartRxData;
    
    // Hardware
    private int mBatteryLevel;
    private int mRssi;
    
    
    // BLE members
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private Handler mFindHandler;
    private Runnable mFindRunnable;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    private boolean mIsShowKonashiOnly = true;
    private boolean mIsSupportBle = false;
    private boolean mIsInitialized = false;
    private String mKonashiName;
    
    // konashi event listenr
    private KonashiNotifier mNotifier;
    
    // UI members
    private Activity mActivity;
    private BleDeviceSelectionDialog mDialog;
    
    
    /////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////// 
    
    public KonashiManager(){
        mNotifier = new KonashiNotifier();
        mKonashiMessageList = new ArrayList<KonashiMessage>();
    }
    
    /**
     * 初期化
     * @param context コンテキスト(activityよりgetApplicationContext()が良い)
     */
    public void initialize(Context context){
        mIsSupportBle = isSupportBle(context);
        if(!mIsSupportBle){
            // BLE not supported. can't initialize
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // initialize BLE
        mBleDeviceListAdapter = new BleDeviceListAdapter(context);
        
        mBluetoothManager = (BluetoothManager)context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        
        mDialog = new BleDeviceSelectionDialog(mBleDeviceListAdapter, this);
        
        mFindHandler = new Handler();
        mFindRunnable = new Runnable() {
            @Override
            public void run() {
                if(mStatus.equals(BleStatus.SCANNING)){
                    mBluetoothAdapter.stopLeScan(KonashiManager.this);
                    setStatus(BleStatus.SCAN_END);
                    
                    if(mKonashiName!=null){
                        // called findWithName. dispatch PERIPHERAL_NOT_FOUND event
                        notifyKonashiEvent(KonashiEvent.PERIPHERAL_NOT_FOUND);
                    } else {
                        mDialog.finishFinding();
                        
                        if(mBleDeviceListAdapter.getCount()==0){
                            notifyKonashiEvent(KonashiEvent.PERIPHERAL_NOT_FOUND);
                        }
                    }
                }
            }
        };
        
        initializeMembers();
        
        mIsInitialized = true;
    }
    
    /**
     * konashiを見つける(konashiのみBLEデバイスリストに表示する)
     * @param activity BLEデバイスリストを表示する先のActivity
     */
    public void find(Activity activity){
        find(activity, true, null);
    }
    
    /**
     * konashiを見つける
     * @param activity BLEデバイスリストを表示する先のActivity
     * @param isShowKonashiOnly konashiだけを表示するか、すべてのBLEデバイスを表示するか
     */
    public void find(Activity activity, boolean isShowKonashiOnly){
        find(activity, isShowKonashiOnly, null);
    }
    
    /**
     * 名前を指定してkonashiを探索。
     * @param activity BLEデバイスリストを表示する先のActivity
     * @param name konashiの緑色のチップに貼られているシールに書いている数字(例: konashi#0-1234)
     */
    public void findWithName(Activity activity, String name){
        find(activity, true, name);
    }
    
    private void find(Activity activity, boolean isShowKonashiOnly, String name){        
        // check initialized
        if(!mIsInitialized || mStatus.equals(BleStatus.READY)){
            return;
        }
        
        KonashiUtils.log("find start");
        
        mActivity = activity;
        
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        
        mIsShowKonashiOnly = isShowKonashiOnly;
        
        mFindHandler.postDelayed(mFindRunnable, SCAN_PERIOD);
        
        mBleDeviceListAdapter.clearDevices();
        
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
        
        mKonashiName = name;
        
        if(mKonashiName==null){
            mDialog.show(activity);
        }
        
        startFifoTimer();
    }
    
    /**
     * konashiとの接続を解除する
     */
    public void disconnect(){
        if(mBluetoothGatt!=null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            setStatus(BleStatus.DISCONNECTED);
        }
        
        stopFifoTimer();
        
        // reset members
        mKonashiMessageList.clear();
    }
    
    /**
     * disconnectし、変数をリセットする
     */
    public void close(){
        if(mStatus.equals(BleStatus.CLOSED)){
            return;
        }
        
        // disconnect if not-disconnected & close
        if(!mStatus.equals(BleStatus.DISCONNECTED)){
            disconnect();
        }
        
        // remove all messages
        mKonashiMessageList.clear();
        
        // remove all observers
        mNotifier.removeAllObservers();
        
        setStatus(BleStatus.CLOSED);
    }
    
    /**
     * konashiと接続済みかどうか
     * @return konashiと接続済みだったらtrue
     */
    public boolean isConnected(){
        return mStatus.equals(BleStatus.CONNECTED) ||
               mStatus.equals(BleStatus.CHARACTERISTICS_FOUND) ||
               mStatus.equals(BleStatus.SERVICE_FOUND) ||
               mStatus.equals(BleStatus.READY)
        ;
    }
    
    /**
     * konashiを使える状態になっているか
     * @return konashiを使えるならtrue
     */
    public boolean isReady(){
        return mStatus.equals(BleStatus.READY);
    }
    
    /**
     * 接続しているkonashiの名前を取得する
     * @return konashiの名前
     */
    public String getPeripheralName(){
        if(mBluetoothGatt!=null && mBluetoothGatt.getDevice()!=null){
            return mBluetoothGatt.getDevice().getName();
        } else {
            return "";
        }
    }
        
    
    /****************************
     * BLE Override methods
     ****************************/
    
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        KonashiUtils.log("DeviceName: " + device.getName());
        
        if(mKonashiName!=null){
            // called findWithName
            if(device.getName().equals(mKonashiName)){
                onSelectBleDevice(device);
            }
            
            return;
        }

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
        notifyKonashiEvent(KonashiEvent.CANCEL_SELECT_KONASHI);

        if(mStatus.equals(BleStatus.SCANNING)){
            stopFindHandler();
            mBluetoothAdapter.stopLeScan(KonashiManager.this);
            setStatus(BleStatus.DISCONNECTED);            
        }
    }
    
    
    /////////////////////////////////////////////////////////////
    // Private methods
    /////////////////////////////////////////////////////////////
    
    private void initializeMembers(){
        int i;
        
        // PIO
        mPioModeSetting = 0;
        mPioPullup = 0;
        mPioInput = 0;
        mPioOutput = 0;
            
        // PWM
        mPwmSetting = 0;
        mPwmDuty = new int[PWM_LENGTH];
        for(i=0; i<PWM_LENGTH; i++)
            mPwmDuty[i] = 0;
        mPwmPeriod = new int[PWM_LENGTH];
        for(i=0; i<PWM_LENGTH; i++)
            mPwmPeriod[i] = 0;
            
        // AIO
        mAioValue = new int[AIO_LENGTH];
        for(i=0; i<AIO_LENGTH; i++)
            mAioValue[i] = 0;
        
        // I2C
        mI2cSetting = 0;
        mI2cReadData = new byte[Konashi.I2C_DATA_MAX_LENGTH];
        for(i=0; i<Konashi.I2C_DATA_MAX_LENGTH; i++)
            mI2cReadData[i] = 0;
        mI2cReadDataLength = 0;
        mI2cReadAddress = 0;
            
        // UART
        mUartSetting = 0;
        mUartBaudrate = 0;
        mUartRxData = 0;
            
        // Hardware
        mBatteryLevel = 0;
        mRssi = 0;
    }
    
    private boolean isSupportBle(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    
    private boolean isEnableAccessKonashi(){
        return mIsSupportBle && mIsInitialized && mStatus.equals(BleStatus.READY);
    }
    
    private void stopFindHandler(){
        mFindHandler.removeCallbacks(mFindRunnable);
    }
    
    private void setStatus(BleStatus status) {
        mStatus = status;
        
        if(status==BleStatus.DISCONNECTED){
            KonashiUtils.log("konashi_status: DISCONNECTED");
        } else if(status==BleStatus.SCANNING){
            KonashiUtils.log("konashi_status: SCANNING");
        } else if(status==BleStatus.SCAN_END){
            KonashiUtils.log("konashi_status: SCAN_END");
        } else if(status==BleStatus.DEVICE_FOUND){
            KonashiUtils.log("konashi_status: DEVICE_FOUND");
        } else if(status==BleStatus.CONNECTED){
            KonashiUtils.log("konashi_status: CONNECTED");
        } else if(status==BleStatus.SERVICE_NOT_FOUND){
            KonashiUtils.log("konashi_status: SERVICE_NOT_FOUND");
        } else if(status==BleStatus.SERVICE_FOUND){
            KonashiUtils.log("konashi_status: SERVICE_FOUND");
        } else if(status==BleStatus.CHARACTERISTICS_NOT_FOUND){
            KonashiUtils.log("konashi_status: CHARACTERISTICS_NOT_FOUND");
        } else if(status==BleStatus.CHARACTERISTICS_FOUND){
            KonashiUtils.log("konashi_status: CHARACTERISTICS_FOUND");
        } else if(status==BleStatus.READY){
            KonashiUtils.log("konashi_status: READY");
            notifyKonashiEvent(KonashiEvent.READY);
        } else if(status==BleStatus.CLOSED){
            KonashiUtils.log("konashi_status: CLOSED");
        } else {
            KonashiUtils.log("konashi_status: UNKNOWN");
        }
    }
    
    
    /******************************************
     * BLE methods
     ******************************************/
    
    private void connect(BluetoothDevice device){
        mBluetoothGatt = device.connectGatt(mActivity.getApplicationContext(), false, mBluetoothGattCallback);
    }
    
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            KonashiUtils.log("onCharacteristicChanged: " + characteristic.getUuid());
            
            if(characteristic.getUuid().toString().equals(KonashiUUID.PIO_INPUT_NOTIFICATION_UUID)){
                // PIO input notification
                byte value = characteristic.getValue()[0];
                mPioInput = value;
                
                KonashiUtils.log("#####  " + mPioInput);
                
                // fire event
                notifyKonashiEvent(KonashiEvent.UPDATE_PIO_INPUT);
            }
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
            KonashiUtils.log("onConnectionStateChange: " + connectionStatus2string(status) + " -> " + connectionStatus2string(newState));
            
            if(newState == BluetoothProfile.STATE_CONNECTED){
                setStatus(BleStatus.CONNECTED);
                
                notifyKonashiEvent(KonashiEvent.CONNECTED);
                
                gatt.discoverServices();
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                setStatus(BleStatus.DISCONNECTED);
                
                notifyKonashiEvent(KonashiEvent.DISCONNECTED);
                
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
            KonashiUtils.log("onServicesDiscovered start");
            
            if(status == BluetoothGatt.GATT_SUCCESS){
                BluetoothGattService service = gatt.getService(KonashiUUID.SERVICE_UUID);

                // Check konashi service
                if (service == null) {
                    setStatus(BleStatus.SERVICE_NOT_FOUND);
                    return;
                }
                
                setStatus(BleStatus.SERVICE_FOUND);
                    
                // Check the characteristics
                if(!isAvailableCharacteristic(KonashiUUID.PIO_SETTING_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PIO_PULLUP_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PIO_OUTPUT_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PIO_INPUT_NOTIFICATION_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PWM_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PWM_PARAM_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.PWM_DUTY_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.ANALOG_DRIVE_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.ANALOG_READ0_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.ANALOG_READ1_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.ANALOG_READ2_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.I2C_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.I2C_START_STOP_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.I2C_WRITE_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.I2C_READ_PARAM_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.I2C_READ_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.UART_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.UART_BAUDRATE_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.UART_TX_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.UART_RX_NOTIFICATION_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.HARDWARE_RESET_UUID) ||
                   !isAvailableCharacteristic(KonashiUUID.HARDWARE_LOW_BAT_NOTIFICATION_UUID)
                ){
                    setStatus(BleStatus.CHARACTERISTICS_NOT_FOUND);
                    return;
                }
        
                // available all konashi characteristics
                setStatus(BleStatus.CHARACTERISTICS_FOUND);
                
                // enable notification
                if(!enableNotification(KonashiUUID.PIO_INPUT_NOTIFICATION_UUID)
                ){
                    setStatus(BleStatus.CHARACTERISTICS_NOT_FOUND);
                    return;
                }
                
                setStatus(BleStatus.READY);
            }
        }
    };
    
    private BluetoothGattCharacteristic getCharacteristic(UUID uuid){
        if(mBluetoothGatt!=null){
            BluetoothGattService service = mBluetoothGatt.getService(KonashiUUID.SERVICE_UUID);
            return service.getCharacteristic(uuid);
        } else {
            return null;
        }
    }
    
    private boolean isAvailableCharacteristic(UUID uuid){
        KonashiUtils.log("check characteristic: " + uuid.toString());

        return getCharacteristic(uuid) != null;
    }
    
    private boolean enableNotification(UUID uuid){
        KonashiUtils.log("try enable notification: " + uuid.toString());
        
        BluetoothGattCharacteristic characteristic = getCharacteristic(uuid);
        if(mBluetoothGatt!=null && characteristic!=null){
            boolean registered = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KonashiUUID.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            return registered;
        } else {
            return false;
        }
    }
    
    private String connectionStatus2string(int status){
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
    
    
    /*********************************
     * FIFO send buffer
     *********************************/
    
    private void addMessage(UUID uuid, byte[] value){
        mKonashiMessageList.add(new KonashiMessage(uuid, value));
    }
    
    private KonashiMessage getFirstMessage(){
        if(mKonashiMessageList.size()>0){
            KonashiMessage message = mKonashiMessageList.get(0);
            mKonashiMessageList.remove(0);
            return message;
        } else {
            return null;
        }
    }
    
    private void startFifoTimer(){
        stopFifoTimer();
        
        mFifoTimer = new Timer(true);
        final Handler handler = new Handler();
        mFifoTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        KonashiMessage message = getFirstMessage();
                        if(message!=null){
                            writeValue(message.uuid, message.data);
                        }
                    }
                });
            }
        }, KONASHI_SEND_PERIOD, KONASHI_SEND_PERIOD);
    }
    
    private void stopFifoTimer(){
        if(mFifoTimer!=null){
            mFifoTimer.cancel();
            mFifoTimer = null;
        }
    }
    
    
    /*********************************
     * Write/Read on BLE
     *********************************/
    
    private void writeValue(UUID uuid, byte[] value){
        if (mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(KonashiUUID.SERVICE_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
            if(characteristic!=null){
                characteristic.setValue(value);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }
    
    
    /******************************
     * Konashi observer methods
     ******************************/

    /**
     * konashiのイベントのオブザーバを追加する
     * @param observer 追加するオブザーバ
     */
    public void addObserver(KonashiObserver observer){
        mNotifier.addObserver(observer);
    }
    
    /**
     * 指定したオブザーバを削除する
     * @param observer 削除するオブザーバ
     */
    public void removeObserver(KonashiObserver observer){
        mNotifier.removeObserver(observer);
    }
    
    /**
     * すべてのオブザーバを削除する
     */
    public void removeAllObservers(){
        mNotifier.removeAllObservers();
    }
    
    /**
     * オブザーバにイベントを通知する
     * @param event 通知するイベント名
     */
    private void notifyKonashiEvent(KonashiEvent event){
        mNotifier.notifyKonashiEvent(event);
    }
    
    private void notifyKonashiError(KonashiErrorReason errorReason){
        mNotifier.notifyKonashiError(errorReason);
    }
    
    /******************************
     * Konashi methods
     ******************************/
    
    ///////////////////////////
    // PIO
    ///////////////////////////
    
    /**
     * PIOのピンを入力として使うか、出力として使うかの設定を行う
     * @param pin 設定するPIOのピン名。
     * @param mode ピンに設定するモード。INPUT か OUTPUT が設定できます。
     */
    @Override
    public void pinMode(int pin, int mode){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (mode == Konashi.OUTPUT || mode == Konashi.INPUT)){
            if(mode == Konashi.OUTPUT){
                mPioModeSetting |= (byte)(0x01 << pin);
            }else{
                mPioModeSetting &= (byte)(~(0x01 << pin) & 0xFF);
            }
            
            byte[] val = new byte[1];
            val[0] = mPioModeSetting;
            
            addMessage(KonashiUUID.PIO_SETTING_UUID, val);
        }
    }
    
    /**
     * PIOのピンを入力として使うか、出力として使うかの設定を行う
     * @param modes PIO0 〜 PIO7 の計8ピンの設定
     */
    @Override
    public void pinModeAll(int modes){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(modes >= 0x00 && modes <= 0xFF){
            mPioModeSetting = (byte)modes;
            
            byte[] val = new byte[1];
            val[0] = mPioModeSetting;
            
            addMessage(KonashiUUID.PIO_SETTING_UUID, val);
        }
    }
    
    /**
     * PIOのピンをプルアップするかの設定を行う
     * @param pin 設定するPIOのピン名
     * @param pullup ピンをプルアップするかの設定。PULLUP か NO_PULLS が設定できます。
     */
    @Override
    public void pinPullup(int pin, int pullup){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (pullup == Konashi.PULLUP || pullup == Konashi.NO_PULLS)){
            if(pullup == Konashi.PULLUP){
                mPioPullup |= (byte)(0x01 << pin);
            }else{
                mPioPullup &= (byte)(~(0x01 << pin) & 0xFF);
            }
            
            byte[] val = new byte[1];
            val[0] = mPioPullup;
            
            addMessage(KonashiUUID.PIO_PULLUP_UUID, val);
        }
    }
    
    /**
     * PIOのピンをプルアップするかの設定を行う
     * @param pullups PIO0 〜 PIO7 の計8ピンのプルアップの設定
     */
    @Override
    public void pinPullupAll(int pullups){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pullups >= 0x00 && pullups <= 0xFF){
            mPioPullup = (byte)pullups;
            
            byte[] val = new byte[1];
            val[0] = mPioPullup;
            
            addMessage(KonashiUUID.PIO_PULLUP_UUID, val);
        }
    }
    
    /**
     * PIOの特定のピンの入力状態を取得する
     * @param pin PIOのピン名
     * @return HIGH(1) もしくは LOW(0)
     */
    @Override
    public int digitalRead(int pin){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return -1;
        }
        
        return (mPioInput >> pin) & 0x01;
    }
    
    /**
     * PIOのすべてのピンの状態を取得する
     * @return PIOの状態(PIO0〜PIO7の入力状態が8bit(1byte)で表現)
     */
    @Override
    public int digitalReadAll(){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return -1;
        }
        
        return mPioInput;
    }
    
    /**
     * PIOの特定のピンの出力状態を設定する
     * @param pin 設定するPIOのピン名
     * @param value 設定するPIOの出力状態。HIGH もしくは LOW が指定可能
     */
    @Override
    public void digitalWrite(int pin, int value){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (value == Konashi.HIGH || value == Konashi.LOW)){
            KonashiUtils.log("digitalWrite pin: " + pin + ", value: " + value);
            
            if(value == Konashi.HIGH){
                mPioOutput |= 0x01 << pin;
            } else {
                mPioOutput &= ~(0x01 << pin) & 0xFF;
            }
            
            byte[] val = new byte[1];
            val[0] = mPioOutput;
            
            addMessage(KonashiUUID.PIO_OUTPUT_UUID, val);
        }
    }
    
    /**
     * PIOの特定のピンの出力状態を設定する
     * @param value PIOの出力状態。PIO0〜PIO7の出力状態が8bit(1byte)で表現
     */
    @Override
    public void digitalWriteAll(int value){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(value >= 0x00 && value <= 0xFF){            
            mPioOutput = (byte)value;
            
            byte[] val = new byte[1];
            val[0] = mPioOutput;
            
            addMessage(KonashiUUID.PIO_OUTPUT_UUID, val);
        }
    }
    
    
    ///////////////////////////
    // PWM
    ///////////////////////////
    
    /**
     * PIO の指定のピンを PWM として使用する/しないかを設定する
     * @param pin PWMモードの設定をするPIOのピン番号。Konashi.PIO0 〜 Konashi.PIO7。
     * @param mode 設定するPWMのモード。Konashi.PWM_DISABLE, Konashi.PWM_ENABLE, Konashi.PWM_ENABLE_LED_MODE のいずれかをセットする。
     */
    @Override
    public void pwmMode(int pin, int mode){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (mode == Konashi.PWM_DISABLE || mode == Konashi.PWM_ENABLE || mode == Konashi.PWM_ENABLE_LED_MODE)){
            if(mode == Konashi.PWM_ENABLE || mode == Konashi.PWM_ENABLE_LED_MODE){
                mPwmSetting |= 0x01 << pin;
            } else {
                mPwmSetting &= ~(0x01 << pin) & 0xFF;
            }
            
            if (mode == Konashi.PWM_ENABLE_LED_MODE){
                pwmPeriod(pin, Konashi.PWM_LED_PERIOD);
                pwmLedDrive(pin, 0.0F);
            }
            
            byte[] val = new byte[1];
            val[0] = mPwmSetting;
            
            addMessage(KonashiUUID.PWM_CONFIG_UUID, val);
        }
    }
    
    /**
     * 指定のピンのPWM周期を設定する
     * @param pin PWMモードの設定をするPIOのピン番号。Konashi.PIO0 〜 Konashi.PIO7。
     * @param period 周期。単位はマイクロ秒(us)で32bitで指定してください。最大2^(32)us = 71.5分。
     */
    @Override
    public void pwmPeriod(int pin, int period){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && mPwmDuty[pin] <= period){
            mPwmPeriod[pin] = period;
            
            byte[] val = new byte[5];
            val[0] = (byte)pin;
            val[1] = (byte)((mPwmPeriod[pin] >> 24) & 0xFF);
            val[2] = (byte)((mPwmPeriod[pin] >> 16) & 0xFF);
            val[3] = (byte)((mPwmPeriod[pin] >> 8) & 0xFF);
            val[4] = (byte)((mPwmPeriod[pin] >> 0) & 0xFF);
            
            addMessage(KonashiUUID.PWM_PARAM_UUID, val);
        }
    }
    
    /**
     * 指定のピンのPWMのデューティ(ONになっている時間)を設定する。
     * @param pin PWMモードの設定をするPIOのピン番号。Konashi.PIO0 〜 Konashi.PIO7。
     * @param duty デューティ。単位はマイクロ秒(us)で32bitで指定してください。最大2^(32)us = 71.5分。
     */
    @Override
    public void pwmDuty(int pin, int duty){
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && duty <= mPwmPeriod[pin]){
            mPwmDuty[pin] = duty;
            
            byte[] val = new byte[5];
            val[0] = (byte)pin;
            val[1] = (byte)((mPwmDuty[pin] >> 24) & 0xFF);
            val[2] = (byte)((mPwmDuty[pin] >> 16) & 0xFF);
            val[3] = (byte)((mPwmDuty[pin] >> 8) & 0xFF);
            val[4] = (byte)((mPwmDuty[pin] >> 0) & 0xFF);
            
            addMessage(KonashiUUID.PWM_DUTY_UUID, val);
        }
    }
    
    /**
     * 指定のピンのLEDの明るさを0%〜100%で指定する
     * @param pin PWMモードの設定をするPIOのピン番号。Konashi.PIO0 〜 Konashi.PIO7。
     * @param dutyRatio LEDの明るさ。0.0F〜100.0F をしてしてください。
     */
    @Override
    public void pwmLedDrive(int pin, float dutyRatio){
        int duty;

        if(dutyRatio >= 0.0 && dutyRatio <= 100.0){
            duty = (int)(Konashi.PWM_LED_PERIOD * dutyRatio / 100);        
            pwmDuty(pin, duty);
        }
    }
    
    /**
     * pwmLedDrive(int pin, float dutyRatio) の doubleでdutyRatioを指定する版。
     * @param pin PWMモードの設定をするPIOのピン番号。Konashi.PIO0 〜 Konashi.PIO7。
     * @param dutyRatio LEDの明るさ。0.0〜100.0 をしてしてください。
     */
    @Override
    public void pwmLedDrive(int pin, double dutyRatio){        
        pwmLedDrive(pin, (float)dutyRatio);
    }
    
    
    ///////////////////////////
    // AIO
    ///////////////////////////

    @Override
    public void analogReadRequest(int pin) {
        if(!isEnableAccessKonashi()){
            notifyKonashiError(KonashiErrorReason.NOT_READY);
            return;
        }
        
        
    }
    
}
