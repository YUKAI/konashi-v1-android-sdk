package com.uxxu.konashi.lib;

import com.uxxu.konashi.lib.BuildConfig;

import android.util.Log;

public class KonashiUtils {
    private static final Boolean DEBUG = true;

    private static final String TAG = "KonashiLib";
    private static final int LOG_STACK_LEVEL = 3;
    
    public static void log(){
        log("");
    }
    
    public static void log(String text){
        if(DEBUG){
            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            Log.d(TAG, "[" + ste[LOG_STACK_LEVEL].getFileName() + ":" + ste[LOG_STACK_LEVEL].getMethodName() + "(" + ste[LOG_STACK_LEVEL].getLineNumber() + ")] " + text);
        }
    }
}
