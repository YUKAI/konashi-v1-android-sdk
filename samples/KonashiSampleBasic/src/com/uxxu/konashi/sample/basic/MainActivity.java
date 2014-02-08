package com.uxxu.konashi.sample.basic;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uxxu.konashi.lib.*;

public class MainActivity extends KonashiActivity {
    private static final String TAG = "KonashiSample";
    
    private LinearLayout mContainer;
    private Button mFindButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mContainer = (LinearLayout)findViewById(R.id.container);
        mContainer.setVisibility(View.GONE);
        
        mFindButton = (Button)findViewById(R.id.find_button);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getKonashiManager().isReady()){
                    // connect konashi
                    getKonashiManager().find(MainActivity.this);
                    //mKonashiManager.findWithName(MainActivity.this, "konashi#4-0452");                    
                } else {
                    // disconnect konashi
                    getKonashiManager().disconnect();
                    
                    mFindButton.setText(getText(R.string.find_button));
                    mContainer.setVisibility(View.GONE);
                }
            }
        });
        
        setButtonAction(R.id.led2_button, Konashi.LED2);
        setButtonAction(R.id.led3_button, Konashi.LED3);
        setButtonAction(R.id.led4_button, Konashi.LED4);
        setButtonAction(R.id.led5_button, Konashi.LED5);
        
        // Initialize konashi manager
        getKonashiManager().addObserver(mKonashiObserver);
    }
    
    private void setButtonAction(int resId, final int pin){
        Button button = (Button)findViewById(resId);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {                
                switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    getKonashiManager().digitalWrite(pin, Konashi.HIGH);
                    break;
                    
                case MotionEvent.ACTION_UP:
                    getKonashiManager().digitalWrite(pin, Konashi.LOW);
                    break;
                }
                return false;
            }
        });
    }

    private final KonashiObserver mKonashiObserver = new KonashiObserver(MainActivity.this) {
        @Override
        public void onReady(){
            Log.d(TAG, "onKonashiReady");
            
            mFindButton.setText(getText(R.string.disconnect_button));
            mContainer.setVisibility(View.VISIBLE);

            getKonashiManager().pinMode(Konashi.LED2, Konashi.OUTPUT);
            getKonashiManager().pinMode(Konashi.LED3, Konashi.OUTPUT);
            getKonashiManager().pinMode(Konashi.LED4, Konashi.OUTPUT);
            getKonashiManager().pinMode(Konashi.LED5, Konashi.OUTPUT);
        }
    };
}
