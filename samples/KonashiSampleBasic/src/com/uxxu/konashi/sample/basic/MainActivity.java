package com.uxxu.konashi.sample.basic;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.uxxu.konashi.lib.*;

public class MainActivity extends Activity {
    private static final String TAG = "KonashiSample";
    
    private KonashiManager mKonashiManager;
    
    private Button mFindButton;
    private Button mOnButton;
    private Button mOffButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mFindButton = (Button)findViewById(R.id.find_button);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKonashiManager.find(MainActivity.this);
                //Intent intent = new Intent(MainActivity.this, SubActivity.class);
                //startActivity(intent);
            }
        });
        
        mOnButton = (Button)findViewById(R.id.on_button);
        mOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKonashiManager.digitalWrite(KonashiManager.LED2, KonashiManager.HIGH);
            }
        });
        
        mOffButton = (Button)findViewById(R.id.off_button);
        mOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKonashiManager.digitalWrite(KonashiManager.LED2, KonashiManager.LOW);
            }
        });
        
        mKonashiManager = new KonashiManager();
        mKonashiManager.initialize(getApplicationContext());
        mKonashiManager.addEventLister(mKonashiEventListener);
    }
    
    private final KonashiEventListener mKonashiEventListener = new KonashiEventListener() {
        @Override
        public void onKonashiReady() {
            Log.d(TAG, "onKonashiReady");
            
            mKonashiManager.pinMode(KonashiManager.LED2, KonashiManager.OUTPUT);
        }
    };
}
