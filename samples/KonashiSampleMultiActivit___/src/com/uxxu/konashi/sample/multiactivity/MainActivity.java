package com.uxxu.konashi.sample.multiactivity;

import com.uxxu.konashi.lib.Konashi;

import android.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.);
        
        Konashi.initialize(this);
        
        //Button findButton = (Button)findViewById(R.id.find);
    }
}
