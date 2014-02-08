package com.uxxu.konashi.lib;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 周りにあるBLEデバイスをリストに表示するためのAdapter
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
public class BleDeviceListAdapter extends BaseAdapter {
    /**
     * BLEデバイスのArray
     */
    private ArrayList<BluetoothDevice> mBleDevices;
    /**
     * ViewのInflater
     */
    private LayoutInflater mInflater;
    /**
     * 表示先のContext
     */
    private Context mContext;
    
    /**
     * コンストラクタ
     * @param context 表示先のContext
     */
    public BleDeviceListAdapter(Context context) {
        mContext = context;
        mBleDevices = new ArrayList<BluetoothDevice>();
    }
    
    /**
     * BLEデバイスをリストに追加する
     * @param device BLEデバイスオブジェクト
     */
    public void addDevice(BluetoothDevice device) {
        if(!mBleDevices.contains(device)) {
            KonashiUtils.log("Device name: " + device.getName());
            mBleDevices.add(device);
        }
    }
    
    /**
     * BLEデバイスのリストの中身をすべてクリアする
     */
    public void clearDevices(){
        mBleDevices.clear();
    }
    
    /**
     * リストの指定のポジションのBLEデバイスオブジェクトを取得する
     * @param position 取得したいポジション
     * @return BLEデバイスオブジェクト
     */
    public BluetoothDevice getDevice(int position) {
        return mBleDevices.get(position);
    }
    
    /**
     * BLEデバイスの個数を取得する
     */
    @Override
    public int getCount() {
        return mBleDevices.size();
    }

    /**
     * 指定のポジションのBLEデバイスオブジェクトを取得する
     * @param position 取得したいBLEデバイスのポジション
     */
    @Override
    public Object getItem(int position) {
        return mBleDevices.get(position);
    }

    /**
     * アイテムIDを取得する
     * @param position 取得したいポジション
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * BLEデバイスリストの要素Viewを返す
     * @param position 生成するViewのポジション
     * @param convertView そのポジションのView
     * @param parent 返すViewが所属するViewGroup
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        
        if(v==null){
            v = LayoutInflater.from(mContext).inflate(R.layout.device_list_element, parent, false);
        }
        
        BluetoothDevice device = (BluetoothDevice)mBleDevices.get(position);
        
        String deviceName = device.getName();
        TextView name = (TextView)v.findViewById(R.id.konashi_lib_device_name);
        if (deviceName != null && deviceName.length() > 0){
            name.setText(device.getName());
        } else {
            name.setText(R.string.konashi_lib_device_list_element_unknown_device);
        }
        
        TextView address = (TextView)v.findViewById(R.id.konashi_lib_device_address);
        address.setText(device.getAddress());
        
        return v;
    }

}
