package com.uxxu.konashi.lib;

import android.app.Activity;
import android.content.Context;

public class Konashi {
    public static KonashiManager sKonashiManager;
    
    public static void initialize(Context context){
        sKonashiManager = new KonashiManager();
        sKonashiManager.initialize(context);
    }
    
    public static KonashiManager getManager(){
        return sKonashiManager;
    }
}
