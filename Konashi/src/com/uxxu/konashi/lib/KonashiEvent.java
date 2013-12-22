package com.uxxu.konashi.lib;

/**
 * konashiのイベントたち
 * 
 * @author monakaz, YUKAI Engineering
 * http://konashi.ux-xu.com
 * ========================================================================
 * Copyright 2013 Yukai Engineering Inc.
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
public class KonashiEvent {
    /**
     * findWithNameで指定した名前のkonashiが見つからなかった時、もしくはまわりにBLEデバイスがなかった時
     */
    public static final String PERIPHERAL_NOT_FOUND = "PERIPHERAL_NOT_FOUND";
    /**
     * BLEデバイス選択ダイアログをキャンセルした時
     */
    public static final String CANCEL_SELECT_KONASHI = "CANCEL_SELECT_KONASHI";
    /**
     * konashiに接続した時(まだこの時はkonashiが使える状態ではありません)
     */
    public static final String CONNECTED = "CONNECTED";
    /**
     * konashiとの接続を切断した時
     */
    public static final String DISCONNECTED = "DISCONNECTED";
    /**
     * konashiに接続完了した時(この時からkonashiにアクセスできるようになります)
     */
    public static final String READY = "READY";
    /**
     * PIOの入力の状態が変化した時
     */
    public static final String UPDATE_PIO_INPUT = "UPDATE_PIO_INPUT";
    /**
     * AIOのどれかのピンの電圧が取得できた時
     */
    public static final String UPDATE_ANALOG_VALUE = "UPDATE_ANALOG_VALUE";
    /**
     * AIO0の電圧が取得できた時
     */
    public static final String UPDATE_ANALOG_VALUE_AIO0 = "UPDATE_ANALOG_VALUE_AIO0";
    /**
     * AIO1の電圧が取得できた時
     */
    public static final String UPDATE_ANALOG_VALUE_AIO1 = "UPDATE_ANALOG_VALUE_AIO1";
    /**
     * AIO2の電圧が取得できた時
     */
    public static final String UPDATE_ANALOG_VALUE_AIO2 = "UPDATE_ANALOG_VALUE_AIO2";
    /**
     * I2Cからデータを受信した時
     */
    public static final String I2C_READ_COMPLETE = "I2C_READ_COMPLETE";
    /**
     * UARTのRxからデータを受信した時
     */
    public static final String UART_RX_COMPLETE = "UART_RX_COMPLETE";
    /**
     * konashiのバッテリーのレベルを取得できた時
     */
    public static final String UPDATE_BATTERY_LEVEL = "UPDATE_BATTERY_LEVEL";
    /**
     * konashiの電波強度を取得できた時
     */
    public static final String UPDATE_SIGNAL_STRENGTH = "UPDATE_SIGNAL_STRENGTH";
}
