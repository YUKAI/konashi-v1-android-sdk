package com.uxxu.konashi.lib;

import android.app.Activity;

public abstract class KonashiObserver {
    private Activity mActivity;
    
    public KonashiObserver(Activity activity){
        mActivity = activity;
    }
    
    public Activity getActivity(){
        return mActivity;
    }
    
    public void onNotFoundPeripheral(){}    
    public void onConnected(){}
    public void onDisconncted(){}
    public void onReady(){}
    public void onUpdatePioInput(){}
    public void onCancelSelectKonashi(){}
}
