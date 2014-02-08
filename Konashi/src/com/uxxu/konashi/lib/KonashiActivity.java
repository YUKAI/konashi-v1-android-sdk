package com.uxxu.konashi.lib;

import android.app.Activity;
import android.os.Bundle;

public class KonashiActivity extends Activity {
    private KonashiManager mKonashiManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize konashi manager
        mKonashiManager = new KonashiManager();
        mKonashiManager.initialize(getApplicationContext());
    }
    
    @Override
    protected void onDestroy() {
        if(mKonashiManager!=null){
            mKonashiManager.disconnect();
            mKonashiManager.close();
            mKonashiManager = null;
        }
        
        super.onDestroy();
    }
    
    public KonashiManager getKonashiManager(){
        return mKonashiManager;
    }
}
