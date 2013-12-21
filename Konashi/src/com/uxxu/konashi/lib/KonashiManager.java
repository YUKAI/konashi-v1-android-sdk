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

public class KonashiManager implements BluetoothAdapter.LeScanCallback, OnBleDeviceSelectListener {
    
    /*************************
     * konashi constants
     *************************/
    
    // konashi service UUID
    private static final String KONASHI_BASE_UUID_STRING = "-0000-1000-8000-00805F9B34FB";
    private static final UUID KONASHI_SERVICE_UUID = UUID.fromString("0000FF00" + KONASHI_BASE_UUID_STRING);
   
    // konashi characteristics
    private static final UUID KONASHI_PIO_SETTING_UUID                     = UUID.fromString("00003000" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_PIO_PULLUP_UUID                      = UUID.fromString("00003001" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_PIO_OUTPUT_UUID                      = UUID.fromString("00003002" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_PIO_INPUT_NOTIFICATION_UUID          = UUID.fromString("00003003" + KONASHI_BASE_UUID_STRING);
    
    private static final UUID KONASHI_PWM_CONFIG_UUID                      = UUID.fromString("00003004" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_PWM_PARAM_UUID                       = UUID.fromString("00003005" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_PWM_DUTY_UUID                        = UUID.fromString("00003006" + KONASHI_BASE_UUID_STRING);

    private static final UUID KONASHI_ANALOG_DRIVE_UUID                    = UUID.fromString("00003007" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_ANALOG_READ0_UUID                    = UUID.fromString("00003008" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_ANALOG_READ1_UUID                    = UUID.fromString("00003009" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_ANALOG_READ2_UUID                    = UUID.fromString("0000300A" + KONASHI_BASE_UUID_STRING);

    private static final UUID KONASHI_I2C_CONFIG_UUID                      = UUID.fromString("0000300B" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_I2C_START_STOP_UUID                  = UUID.fromString("0000300C" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_I2C_WRITE_UUID                       = UUID.fromString("0000300D" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_I2C_READ_PARAM_UUID                  = UUID.fromString("0000300E" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_I2C_READ_UUID                        = UUID.fromString("0000300F" + KONASHI_BASE_UUID_STRING);

    private static final UUID KONASHI_UART_CONFIG_UUID                     = UUID.fromString("00003010" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_UART_BAUDRATE_UUID                   = UUID.fromString("00003011" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_UART_TX_UUID                         = UUID.fromString("00003012" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_UART_RX_NOTIFICATION_UUID            = UUID.fromString("00003013" + KONASHI_BASE_UUID_STRING);

    private static final UUID KONASHI_HARDWARE_RESET_UUID                  = UUID.fromString("00003014" + KONASHI_BASE_UUID_STRING);
    private static final UUID KONASHI_HARDWARE_LOW_BAT_NOTIFICATION_UUID   = UUID.fromString("00003015" + KONASHI_BASE_UUID_STRING);
    
    // konashi characteristic configuration
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902" + KONASHI_BASE_UUID_STRING;
    private static final byte KONASHI_FAILURE = (byte)(0xff);
    
    private static final long SCAN_PERIOD = 3000;
    private static final String KONAHSI_DEVICE_NAME = "konashi#";
    private static final long KONASHI_SEND_PERIOD = 10;
    
    
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
                    }
                }
            }
        };
        
        mIsInitialized = true;
    }
    
    public void find(Activity activity){
        find(activity, true, null);
    }
    
    public void find(Activity activity, boolean isShowKonashiOnly){
        find(activity, isShowKonashiOnly, null);
    }
    
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
        mNotifier.removeAllEventListeners();
        
        setStatus(BleStatus.CLOSED);
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
            
            if(characteristic.getUuid().toString().equals(KONASHI_PIO_INPUT_NOTIFICATION_UUID)){
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
                BluetoothGattService service = gatt.getService(KONASHI_SERVICE_UUID);

                // Check konashi service
                if (service == null) {
                    setStatus(BleStatus.SERVICE_NOT_FOUND);
                    return;
                }
                
                setStatus(BleStatus.SERVICE_FOUND);
                    
                // Check the characteristics
                if(!isAvailableCharacteristic(KONASHI_PIO_SETTING_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PIO_PULLUP_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PIO_OUTPUT_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PIO_INPUT_NOTIFICATION_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PWM_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PWM_PARAM_UUID) ||
                   !isAvailableCharacteristic(KONASHI_PWM_DUTY_UUID) ||
                   !isAvailableCharacteristic(KONASHI_ANALOG_DRIVE_UUID) ||
                   !isAvailableCharacteristic(KONASHI_ANALOG_READ0_UUID) ||
                   !isAvailableCharacteristic(KONASHI_ANALOG_READ1_UUID) ||
                   !isAvailableCharacteristic(KONASHI_ANALOG_READ2_UUID) ||
                   !isAvailableCharacteristic(KONASHI_I2C_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KONASHI_I2C_START_STOP_UUID) ||
                   !isAvailableCharacteristic(KONASHI_I2C_WRITE_UUID) ||
                   !isAvailableCharacteristic(KONASHI_I2C_READ_PARAM_UUID) ||
                   !isAvailableCharacteristic(KONASHI_I2C_READ_UUID) ||
                   !isAvailableCharacteristic(KONASHI_UART_CONFIG_UUID) ||
                   !isAvailableCharacteristic(KONASHI_UART_BAUDRATE_UUID) ||
                   !isAvailableCharacteristic(KONASHI_UART_TX_UUID) ||
                   !isAvailableCharacteristic(KONASHI_UART_RX_NOTIFICATION_UUID) ||
                   !isAvailableCharacteristic(KONASHI_HARDWARE_RESET_UUID) ||
                   !isAvailableCharacteristic(KONASHI_HARDWARE_LOW_BAT_NOTIFICATION_UUID)
                ){
                    setStatus(BleStatus.CHARACTERISTICS_NOT_FOUND);
                    return;
                }
        
                // available all konashi characteristics
                setStatus(BleStatus.CHARACTERISTICS_FOUND);
                
                // enable notification
                if(!enableNotification(KONASHI_PIO_INPUT_NOTIFICATION_UUID)
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
            BluetoothGattService service = mBluetoothGatt.getService(KONASHI_SERVICE_UUID);
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
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
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
            BluetoothGattService service = mBluetoothGatt.getService(KONASHI_SERVICE_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
            if(characteristic!=null){
                characteristic.setValue(value);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }
    
    
    /******************************
     * Konashi methods
     ******************************/
    
    ///////////////////////////
    // PIO
    ///////////////////////////
    
    public void pinMode(int pin, int mode){
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (mode == Konashi.OUTPUT || mode == Konashi.INPUT)){
            if(mode == Konashi.OUTPUT){
                mPinModeSetting |= (byte)(0x01 << pin);
            }else{
                mPinModeSetting &= (byte)(~(0x01 << pin) & 0xFF);
            }
            
            byte[] val = new byte[1];
            val[0] = mPinModeSetting;
            
            addMessage(KONASHI_PIO_SETTING_UUID, val);
        }
    }
    
    public void digitalWrite(int pin, int value){
        if(pin >= Konashi.PIO0 && pin <= Konashi.PIO7 && (value == Konashi.HIGH || value == Konashi.LOW)){
            KonashiUtils.log("digitalWrite pin: " + pin + ", value: " + value);
            
            if(value == Konashi.HIGH){
                mPioOutput |= 0x01 << pin;
            } else {
                mPioOutput &= ~(0x01 << pin) & 0xFF;
            }
            
            byte[] val = new byte[1];
            val[0] = mPioOutput;
            
            addMessage(KONASHI_PIO_OUTPUT_UUID, val);
        }
    }
    
    public int digitalRead(int pin){
        return (mPioInput >> pin) & 0x01;
    }
    
    
    /******************************
     * Konashi observer
     ******************************/

    public void addObserver(KonashiObserver observer){
        mNotifier.addEventListener(observer);
    }
    
    public void removeObserver(KonashiObserver observer){
        mNotifier.removeEventListener(observer);
    }
    
    public void removeAllObservers(){
        mNotifier.removeAllEventListeners();
    }
    
    public void notifyKonashiEvent(String event){
        mNotifier.notifyKonashiEvent(event);
    }
}
