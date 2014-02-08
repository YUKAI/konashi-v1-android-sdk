package com.uxxu.konashi.lib;

import android.app.Activity;
import android.content.Context;

/**
 * konashi APIのインタフェース
 * 
 * @author monakaz, YUKAI Engineering
 * http://konashi.ux-xu.com
 * ========================================================================
 * Copyright 2014 Yukai Engineering Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
public interface KonashiApiInterface {
    // Observer
    public void addObserver(KonashiObserver observer);
    public void removeObserver(KonashiObserver observer);
    public void removeAllObservers();
    
    // initialization
    public void initialize(Context context);
    public void find(Activity activity);
    public void find(Activity activity, boolean isShowKonashiOnly);
    public void findWithName(Activity activity, String name);
    
    // PIO
    public void pinMode(int pin, int mode);
    public void pinModeAll(int modes);
    public void pinPullup(int pin, int pullup);
    public void pinPullupAll(int pullups);
    public int digitalRead(int pin);
    public int digitalReadAll();
    public void digitalWrite(int pin, int value);
    public void digitalWriteAll(int value);
    
    // PWM
    public void pwmMode(int pin, int mode);
    public void pwmPeriod(int pin, int period);
    public void pwmDuty(int pin, int duty);
    public void pwmLedDrive(int pin, float dutyRatio);
    public void pwmLedDrive(int pin, double dutyRatio);
    
    // AIO
    public void analogReadRequest(int pin);
    public int analogRead(int pin);
    public void analogWrite(int pin, int milliVolt);
    
    // I2C
    public void i2cMode(int mode);
    public void i2cStartCondition();
    public void i2cRestartCondition();
    public void i2cStopCondition();
    public void i2cWrite(int length, byte[] data, byte address);
    public void i2cReadRequest(int length, byte address);
    public byte[] i2cRead(int length);
    
    // UART
    public void uartMode(int mode);
    public void uartBaudrate(int baudrate);
    public void uartWrite(byte data);
    
    // Hardware
    public void reset();
    public void batteryLevelReadRequest();
    public int getBatteryLevel();
    public void signalStrengthReadRequest();
    public int getSignalStrength();
}
