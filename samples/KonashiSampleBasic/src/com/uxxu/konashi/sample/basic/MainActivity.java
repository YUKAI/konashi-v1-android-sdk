package com.uxxu.konashi.sample.basic;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.uxxu.konashi.lib.*;

public class MainActivity extends Activity {
    private KonashiManager mKonashiManager;
    
    private Button mFindButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mFindButton = (Button)findViewById(R.id.find_button);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKonashiManager.find();
            }
        });
        
        mKonashiManager = new KonashiManager();
        mKonashiManager.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
