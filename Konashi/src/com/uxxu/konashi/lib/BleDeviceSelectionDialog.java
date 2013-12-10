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

public class BleDeviceSelectionDialog implements OnItemClickListener {
    private Activity mContext;
    private OnBleDeviceSelectListener mListener;
    private AlertDialog mDialog;
    private LinearLayout mFindingContainer = null;
    private LinearLayout mNotFoundContainer = null;
    private BleDeviceListAdapter mAdapter;
    
    public BleDeviceSelectionDialog(Activity context, BleDeviceListAdapter adapter, OnBleDeviceSelectListener listener){
        mContext = context;
        mListener = listener;
        mAdapter = adapter;
    }
    
    public void show(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_device_list, null);

        ListView listView = (ListView)view.findViewById(R.id.list);
        listView.setScrollingCacheEnabled(false);
        listView.setOnItemClickListener(this);
        listView.setAdapter(mAdapter);
        
        mFindingContainer = (LinearLayout)view.findViewById(R.id.finding);
        mNotFoundContainer = (LinearLayout)view.findViewById(R.id.not_found);
        
        Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.dialog_device_list_title));
        builder.setView(view);
        builder.setPositiveButton(mContext.getString(R.string.dialog_device_list_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mListener!=null){
                    mListener.onCancelSelectingBleDevice();
                }
            }
        });
        mDialog = builder.show();
        
        startFinding();
    }
    
    public void startFinding(){
        if(mFindingContainer!=null){
            mFindingContainer.setVisibility(View.VISIBLE);
        }
        if(mNotFoundContainer!=null){
            mNotFoundContainer.setVisibility(View.GONE);
        }
    }
    
    public void finishFinding(){
        if(mFindingContainer!=null){
            mFindingContainer.setVisibility(View.GONE);
        }
        if(mAdapter.getCount()==0 && mNotFoundContainer!=null){
            mNotFoundContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        if(mDialog!=null){
            // Hide dialog
            mDialog.dismiss();
            mDialog = null;
        }
        
        if(mListener!=null){
            mListener.onSelectBleDevice(mAdapter.getDevice(position));
        }
    }
    
    
    public interface OnBleDeviceSelectListener
    {
        public void onSelectBleDevice(BluetoothDevice device);
        public void onCancelSelectingBleDevice();
    }
}
