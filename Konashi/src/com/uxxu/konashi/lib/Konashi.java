package com.uxxu.konashi.lib;

import android.app.Activity;
import android.content.Context;

public class Konashi {
    // Pin name
    public static final int PIO0 = 0;
    public static final int PIO1 = 1;
    public static final int PIO2 = 2;
    public static final int PIO3 = 3;
    public static final int PIO4 = 4;
    public static final int PIO5 = 5;
    public static final int PIO6 = 6;
    public static final int PIO7 = 7;
    public static final int S1 = 0;
    public static final int LED2 = 1;
    public static final int LED3 = 2;
    public static final int LED4 = 3;
    public static final int LED5 = 4;
    public static final int AIO0 = 0;
    public static final int AIO1 = 1;
    public static final int AIO2 = 2;
    public static final int I2C_SDA = 6;
    public static final int I2C_SCL = 7;

    // PIO
    public static final int HIGH = 1;
    public static final int LOW  = 0;
    public static final int TRUE = 1;
    public static final int FALSE  = 0;
    public static final int OUTPUT = 1;
    public static final int INPUT = 0;
    public static final int PULLUP   = 1;
    public static final int NO_PULLS = 0;
    
    // AIO
    public static final int ANALOG_REFERENCE = 1300;
    
    // PWM
    public static final int PWM_DISABLE = 0;
    public static final int PWM_ENABLE = 1;
    public static final int PWM_ENABLE_LED_MODE = 2;
    public static final int PWM_LED_PERIOD = 10000;
    
    // UART
    public static final int UART_DISABLE = 0;
    public static final int UART_ENABLE = 1;
    public static final int UART_RATE_2K4 = 0x000a;
    public static final int UART_RATE_9K6 = 0x0028;

    // I2C
    public static final int I2C_DATA_MAX_LENGTH = 19;
    public static final int I2C_DISABLE = 0;
    public static final int I2C_ENABLE = 1;
    public static final int I2C_ENABLE_100K = 1;
    public static final int I2C_ENABLE_400K = 2;
    public static final int I2C_STOP_CONDITION = 0;
    public static final int I2C_START_CONDITION = 1;
    public static final int I2C_RESTART_CONDITION = 2;
    
    
    private static KonashiManager sKonashiManager;
    
    public static void initialize(Context context){
        sKonashiManager = new KonashiManager();
        sKonashiManager.initialize(context);
    }
    
    public static KonashiManager getManager(){
        return sKonashiManager;
    }
}
