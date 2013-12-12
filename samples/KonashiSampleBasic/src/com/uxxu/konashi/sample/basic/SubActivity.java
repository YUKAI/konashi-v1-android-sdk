package com.uxxu.konashi.sample.basic;

import com.uxxu.konashi.lib.Konashi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SubActivity extends Activity {
    private Button mFindButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        
        mFindButton = (Button)findViewById(R.id.find_button);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mKonashiManager.find();
                Konashi.getManager().find(SubActivity.this);
            }
        });
        
        /*mKonashiManager = new KonashiManager();
        mKonashiManager.initialize(this);*/
        Konashi.initialize(this);
    }
}
