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
    
    public void onKonashiReady(){}
    public void onUpdatePioInput(){}
}
