package com.uxxu.konashi.sample.multi_activity;

import com.uxxu.konashi.lib.Konashi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ControllerActivity extends Activity {
    private static final String TAG = "konashi";

    private SeekBar mSeekbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        
        mSeekbar = (SeekBar)findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgress: " + progress);
                
                Konashi.getManager().pwmLedDrive(Konashi.LED2, progress);
            }
        });
    }
}
