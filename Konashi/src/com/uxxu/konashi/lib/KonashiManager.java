package com.uxxu.konashi.lib;

import java.util.ArrayList;
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
import android.os.Handler;
import android.os.Message;

public class KonashiManager implements BluetoothAdapter.LeScanCallback, OnBleDeviceSelectListener {
    
    // konashi service UUID
    private static final String KONASHI_BASE_UUID = "-0000-1000-8000-00805f9b34fb";
    private static final String KONASHI_SERVICE_UUID = "0000ff00" + KONASHI_BASE_UUID;
   
    // konashi characteristics
    private static final String KONASHI_PIO_SETTING_UUID                     = "00003000" + KONASHI_BASE_UUID;
    private static final String KONASHI_PIO_PULLUP_UUID                      = "00003001" + KONASHI_BASE_UUID;
    private static final String KONASHI_PIO_OUTPUT_UUID                      = "00003002" + KONASHI_BASE_UUID;
    private static final String KONASHI_PIO_INPUT_NOTIFICATION_UUID          = "00003003" + KONASHI_BASE_UUID;
    
    private static final String KONASHI_PWM_CONFIG_UUID                      = "00003004" + KONASHI_BASE_UUID;
    private static final String KONASHI_PWM_PARAM_UUID                       = "00003005" + KONASHI_BASE_UUID;
    private static final String KONASHI_PWM_DUTY_UUID                        = "00003006" + KONASHI_BASE_UUID;

    private static final String KONASHI_ANALOG_DRIVE_UUID                    = "00003007" + KONASHI_BASE_UUID;
    private static final String KONASHI_ANALOG_READ0_UUID                    = "00003008" + KONASHI_BASE_UUID;
    private static final String KONASHI_ANALOG_READ1_UUID                    = "00003009" + KONASHI_BASE_UUID;
    private static final String KONASHI_ANALOG_READ2_UUID                    = "0000300a" + KONASHI_BASE_UUID;

    private static final String KONASHI_I2C_CONFIG_UUID                      = "0000300b" + KONASHI_BASE_UUID;
    private static final String KONASHI_I2C_START_STOP_UUID                  = "0000300c" + KONASHI_BASE_UUID;
    private static final String KONASHI_I2C_WRITE_UUID                       = "0000300d" + KONASHI_BASE_UUID;
    private static final String KONASHI_I2C_READ_PARAM_UUID                  = "0000300e" + KONASHI_BASE_UUID;
    private static final String KONASHI_I2C_READ_UUID                        = "0000300f" + KONASHI_BASE_UUID;

    private static final String KONASHI_UART_CONFIG_UUID                     = "00003010" + KONASHI_BASE_UUID;
    private static final String KONASHI_UART_BAUDRATE_UUID                   = "00003011" + KONASHI_BASE_UUID;
    private static final String KONASHI_UART_TX_UUID                         = "00003012" + KONASHI_BASE_UUID;
    private static final String KONASHI_UART_RX_NOTIFICATION_UUID            = "00003013" + KONASHI_BASE_UUID;

    private static final String KONASHI_HARDWARE_RESET_UUID                  = "00003014" + KONASHI_BASE_UUID;
    private static final String KONASHI_HARDWARE_LOW_BAT_NOTIFICATION_UUID   = "00003015" + KONASHI_BASE_UUID;
    
    // konashi characteristic configuration
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902" + KONASHI_BASE_UUID;
    private static final byte KONASHI_FAILURE = (byte)(0xff);

    // PIO
    public static final byte PIO0 = 0;
    public static final byte PIO1 = 1;
    public static final byte PIO2 = 2;
    public static final byte PIO3 = 3;
    public static final byte PIO4 = 4;
    public static final byte PIO5 = 5;
    public static final byte PIO6 = 6;
    public static final byte PIO7 = 7;
    public static final byte S1 = 0;
    public static final byte LED2 = 1;
    public static final byte LED3 = 2;
    public static final byte LED4 = 3;
    public static final byte LED5 = 4;
    public static final byte AIO0 = 0;
    public static final byte AIO1 = 1;
    public static final byte AIO2 = 2;
    public static final byte I2C_SDA = 6;
    public static final byte I2C_SCL = 7;
    
    public static final byte INPUT = 0;
    public static final byte OUTPUT = 1;
    public static final byte NO_PULLS = 0;
    public static final byte PULLUP   = 1;
    public static final byte LOW  = 0;
    public static final byte HIGH = 1;
    
    private static final long SCAN_PERIOD = 3000;
    private static final String KONAHSI_DEVICE_NAME = "konashi#";
    
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
    
    // konashi event listenr
    private KonashiNotifier mNotifier;
    
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
        
        mNotifier = new KonashiNotifier();
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
            KonashiUtils.log("onConnectionStateChange: " + connectionStatus2string(status) + " -> " + connectionStatus2string(newState));
            
            if(newState == BluetoothProfile.STATE_CONNECTED){
                setStatus(BleStatus.CONNECTED);
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
            KonashiUtils.log("onServicesDiscovered start");
            
            if(status == BluetoothGatt.GATT_SUCCESS){
                BluetoothGattService service = gatt.getService(UUID.fromString(KONASHI_SERVICE_UUID));

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
                
                setStatus(BleStatus.READY);
            }
        }
    };
    
    private Boolean isAvailableCharacteristic(String uuidString){
        if (mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(KONASHI_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuidString));
            return characteristic != null;
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
    
    private void writeValue(String uuidString, byte[] value){
        if (mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(KONASHI_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuidString));
            if(characteristic!=null){
                characteristic.setValue(value);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }
    
    
    /******************************
     * Konashi methods
     ******************************/
    
    public void pinMode(byte pin, byte mode){
        if(pin >= PIO0 && pin <= PIO7 && (mode == OUTPUT || mode == INPUT)){
            if(mode == OUTPUT){
                mPinModeSetting |= (byte)(0x01 << pin);
            }else{
                mPinModeSetting &= (byte)(~(0x01 << pin) & 0xFF);
            }
            
            byte[] val = new byte[1];
            val[0] = mPinModeSetting;
            
            writeValue(KONASHI_PIO_SETTING_UUID, val);
        }
    }
    
    public void digitalWrite(byte pin, byte value){
        if(pin >= PIO0 && pin <= PIO7 && (value == HIGH || value == LOW)){
            KonashiUtils.log("digitalWrite pin: " + pin + ", value: " + value);
            
            if(value == HIGH){
                mPioOutput |= 0x01 << pin;
            } else {
                mPioOutput &= ~(0x01 << pin) & 0xFF;
            }
            
            byte[] val = new byte[1];
            val[0] = mPioOutput;
            
            writeValue(KONASHI_PIO_OUTPUT_UUID, val);
        }
    }
    
    
    /******************************
     * Konashi event listener
     ******************************/

    public void addEventLister(KonashiEventListener listener){
        mNotifier.addEventListener(listener);
    }
    
    public void removeEventListener(KonashiEventListener listener){
        mNotifier.removeEventListener(listener);
    }
    
    public void removeAllEventListeners(){
        mNotifier.removeAllEventListeners();
    }
    
    public void notifyKonashiEvent(String event){
        mNotifier.notifyKonashiEvent(event);
    }
}
