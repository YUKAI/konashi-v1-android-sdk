package com.uxxu.konashi.lib;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BleDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mBleDevices;
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<BluetoothDevice> mBleDeivceList;
    
    public BleDeviceListAdapter(Context context) {
        mContext = context;
        mBleDevices = new ArrayList<BluetoothDevice>();
    }
    
    public void addDevice(BluetoothDevice device) {
        if(!mBleDevices.contains(device)) {
            KonashiUtils.log("Device name: " + device.getName());
            mBleDevices.add(device);
        }
    }
    
    public BluetoothDevice getDevice(int position) {
        return mBleDevices.get(position);
    }
    
    @Override
    public int getCount() {
        return mBleDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mBleDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        
        if(v==null){
            v = LayoutInflater.from(mContext).inflate(R.layout.device_list_element, parent, false);
        }
        
        BluetoothDevice device = (BluetoothDevice)mBleDevices.get(position);
        
        String deviceName = device.getName();
        TextView name = (TextView)v.findViewById(R.id.device_name);
        if (deviceName != null && deviceName.length() > 0){
            name.setText(device.getName());
        } else {
            name.setText(R.string.device_list_element_unknown_device);
        }
        
        TextView address = (TextView)v.findViewById(R.id.device_address);
        address.setText(device.getAddress());
        
        return v;
    }

}
