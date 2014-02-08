package com.uxxu.konashi.lib;

import android.app.Activity;
import android.content.Context;

/**
 * KonashiManagerを内包するシングルトンクラス。
 * 1つしかkonashiを使わない人や初心者はこちらを使うと楽だよっ！
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
public class Konashi {
    ///////////////////////////////////////////
    // Pin name
    ///////////////////////////////////////////
    /**
     * PIOの0ピン目
     */
    public static final int PIO0 = 0;
    /**
     * PIOの1ピン目
     */
    public static final int PIO1 = 1;
    /**
     * PIOの2ピン目
     */
    public static final int PIO2 = 2;
    /**
     * PIOの3ピン目
     */
    public static final int PIO3 = 3;
    /**
     * PIOの4ピン目
     */
    public static final int PIO4 = 4;
    /**
     * PIOの5ピン目
     */
    public static final int PIO5 = 5;
    /**
     * PIOの6ピン目
     */
    public static final int PIO6 = 6;
    /**
     * PIOの7ピン目
     */
    public static final int PIO7 = 7;
    /**
     * タクトスイッチ（ジャンパ をショートすることで、 PIO0 に接続されます）
     */
    public static final int S1 = 0;
    /**
     * konashi上の赤色LED（ジャンパ をショートすることで、 PIO1 に接続されます）
     */
    public static final int LED2 = 1;
    /**
     * konashi上の赤色LED（ジャンパ をショートすることで、 PIO2 に接続されます）
     */
    public static final int LED3 = 2;
    /**
     * konashi上の赤色LED（ジャンパ をショートすることで、 PIO3 に接続されます）
     */
    public static final int LED4 = 3;
    /**
     * konashi上の赤色LED（ジャンパ をショートすることで、 PIO4 に接続されます）
     */
    public static final int LED5 = 4;
    /**
     * AIOの0ピン目
     */
    public static final int AIO0 = 0;
    /**
     * AIOの1ピン目
     */
    public static final int AIO1 = 1;
    /**
     * AIOの2ピン目
     */
    public static final int AIO2 = 2;
    /**
     * I2CのSDAのピン(PIOの6ピン目)
     */
    public static final int I2C_SDA = 6;
    /**
     * I2CのSDAのピン(PIOの7ピン目)
     */
    public static final int I2C_SCL = 7;

    ///////////////////////////////////////////
    // PIO
    ///////////////////////////////////////////
    /**
     * ピンの出力をHIGH(3V)にする
     */
    public static final int HIGH = 1;
    /**
     * ピンの出力をLOW(0V)にする
     */
    public static final int LOW  = 0;
    /**
     * ピンの出力をHIGH(3V)にする, HIGHと同じ
     */
    public static final int TRUE = 1;
    /**
     * ピンの出力をLOW(0V)にする, LOWと同じ
     */
    public static final int FALSE  = 0;
    /**
     * ピンの入出力設定を出力に
     */
    public static final int OUTPUT = 1;
    /**
     * ピンの入出力設定を入力に
     */
    public static final int INPUT = 0;
    /**
     * ピンのプルアップ設定をON
     */
    public static final int PULLUP   = 1;
    /**
     * ピンのプルアップ設定をOFF
     */
    public static final int NO_PULLS = 0;
    
    ///////////////////////////////////////////
    // AIO
    ///////////////////////////////////////////
    /**
     * アナログ入出力の基準電圧 1300mV
     */
    public static final int ANALOG_REFERENCE = 1300;    // 1300mV
    
    ///////////////////////////////////////////
    // PWM
    ///////////////////////////////////////////
    /**
     * 指定したPIOをPWMとして使用しない(デジタルI/Oとして使用)
     */
    public static final int PWM_DISABLE = 0;
    /**
     * 指定したPIOをPWMとして使用する
     */
    public static final int PWM_ENABLE = 1;
    /**
     * 指定したPIOをLEDモードとしてPWMとして使用する
     */
    public static final int PWM_ENABLE_LED_MODE = 2;
    /**
     * LEDモード時のPWMの周期は10ms
     */
    public static final int PWM_LED_PERIOD = 10000;     // 10ms
    
    ///////////////////////////////////////////
    // UART
    ///////////////////////////////////////////
    /**
     * UART無効
     */
    public static final int UART_DISABLE = 0;
    /**
     * UART有効
     */
    public static final int UART_ENABLE = 1;
    /**
     * 通信速度: 2400bps
     */
    public static final int UART_RATE_2K4 = 0x000a;
    /**
     * 通信速度: 9600bps
     */
    public static final int UART_RATE_9K6 = 0x0028;

    ///////////////////////////////////////////
    // I2C
    ///////////////////////////////////////////
    /**
     * I2Cで一度に送受信できる最大バイト数
     */
    public static final int I2C_DATA_MAX_LENGTH = 19;
    /**
     * I2Cを無効にする
     */
    public static final int I2C_DISABLE = 0;
    /**
     * I2Cを有効にする(100kbpsモードがデフォルト)
     */
    public static final int I2C_ENABLE = 1;
    /**
     * 100kbpsモードでI2Cを有効にする
     */
    public static final int I2C_ENABLE_100K = 1;
    /**
     * 400kbpsモードでI2Cを有効にする
     */
    public static final int I2C_ENABLE_400K = 2;
    /**
     * ストップコンディション
     */
    public static final int I2C_STOP_CONDITION = 0;
    /**
     * スタートコンディション
     */
    public static final int I2C_START_CONDITION = 1;
    /**
     * リスタートコンディション
     */
    public static final int I2C_RESTART_CONDITION = 2;
    /**
     * APIの成功レスポンス
     */
    public static final int SUCCESS = 0;
    /**
     * APIの失敗レスポンス
     */
    public static final int FAILURE = -1;
    
    
    /**
     * KonashiManagerのシングルトン
     */
    private static final KonashiManager sKonashiManager = new KonashiManager();
    
    /**
     * コンストラクタ。privateにして外部からインスタンス生成できないようにする
     */
    private Konashi(){}
    
    /**
     * konashiの初期化
     * @param context konashiを使用するときのActivity
     */
    public static void initialize(Context context){
        sKonashiManager.initialize(context);
    }
    
    /**
     * デストラクタ
     */
    public static void close(){
        if(sKonashiManager!=null){
            sKonashiManager.disconnect();
            sKonashiManager.close();
        }
    }
    
    /**
     * Konashiシングルトンオブジェクトを取得
     * @return KonashiManagerのシングルトンオブジェクト
     */
    public static KonashiManager getManager(){
        return sKonashiManager;
    }

}
