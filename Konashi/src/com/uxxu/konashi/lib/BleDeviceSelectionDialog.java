package com.uxxu.konashi.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 周りにあるBLEデバイスを表示するダイアログ
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
public class BleDeviceSelectionDialog implements OnItemClickListener {
    /**
     * ダイアログでキャンセルを押されたか、BLEデバイスが選択されたかをキャッチするためのオブジェクト。
     */
    private OnBleDeviceSelectListener mListener;
    /**
     * ダイアログ本体
     */
    private AlertDialog mDialog;
    /**
     * 「konashi探索中」というテキストを格納するContainer
     */
    private LinearLayout mFindingContainer = null;
    /**
     * 「konashiが見つかりませんでした」というテキストを格納するContainer
     */
    private LinearLayout mNotFoundContainer = null;
    /**
     * 表示するBLEデバイスリストのAdapter
     */
    private BleDeviceListAdapter mAdapter;
    
    /**
     * コンストラクタ
     * 
     * @param adapter 表示するBLEデバイスリストのAdapter
     * @param listener ダイアログでキャンセルを押されたか、BLEデバイスが選択されたかをキャッチするためのオブジェクト
     */
    public BleDeviceSelectionDialog(BleDeviceListAdapter adapter, OnBleDeviceSelectListener listener){
        mListener = listener;
        mAdapter = adapter;
    }
    
    /**
     * BLEデバイスリストのダイアログを表示する
     * @param activity ダイアログを表示する先のActivity
     */
    public void show(Activity activity){
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_device_list, null);

        ListView listView = (ListView)view.findViewById(R.id.konashi_lib_list);
        listView.setScrollingCacheEnabled(false);
        listView.setOnItemClickListener(this);
        listView.setAdapter(mAdapter);
        
        mFindingContainer = (LinearLayout)view.findViewById(R.id.konashi_lib_finding);
        mNotFoundContainer = (LinearLayout)view.findViewById(R.id.konashi_lib_not_found);
        
        Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.konashi_lib_dialog_device_list_title));
        builder.setView(view);
        builder.setPositiveButton(activity.getString(R.string.konashi_lib_dialog_device_list_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mListener!=null){
                    mListener.onCancelSelectingBleDevice();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {  
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mListener!=null){
                    mListener.onCancelSelectingBleDevice();
                }
            }
        });
        mDialog = builder.show();
        
        startFinding();
    }
    
    /**
     * 「konashi探索中」というテキストを表示し、「konashiが見つかりませんでした」というテキストを非表示にする
     */
    public void startFinding(){
        if(mFindingContainer!=null){
            mFindingContainer.setVisibility(View.VISIBLE);
        }
        if(mNotFoundContainer!=null){
            mNotFoundContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * 「konashi探索中」というテキストを非表示し、「konashiが見つかりませんでした」というテキストを表示にする
     */
    public void finishFinding(){
        if(mFindingContainer!=null){
            mFindingContainer.setVisibility(View.GONE);
        }
        if(mAdapter.getCount()==0 && mNotFoundContainer!=null){
            mNotFoundContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * リスト中のBLEデバイスがクリックされたら
     * 
     * @param adapterView AdapterView(使ってない)
     * @param view View（使ってない)
     * @param position 選択されたBLEデバイスのポジション
     * @param id そのBLEデバイスのID
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        KonashiUtils.log("onItemClick");
        
        if(mDialog!=null){
            // Hide dialog
            mDialog.dismiss();
            mDialog = null;
        }
        
        if(mListener!=null){
            mListener.onSelectBleDevice(mAdapter.getDevice(position));
        }
    }
    
    
    /**
     * ダイアログ内のBLEデバイスが選択されたか、キャンセルを押されたかのイベントを伝えるインタフェース
     */
    public interface OnBleDeviceSelectListener
    {
        /**
         * BLEデバイス選択ダイアログに表示されているBLEデバイスが選択された時
         * @param device 選択されたBLEデバイスオブジェクト
         */
        public void onSelectBleDevice(BluetoothDevice device);
        /**
         * BLEデバイス選択ダイアログでキャンセルが押された時
         */
        public void onCancelSelectingBleDevice();
    }
}
