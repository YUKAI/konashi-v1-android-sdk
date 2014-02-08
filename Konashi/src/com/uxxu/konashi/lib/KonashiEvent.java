package com.uxxu.konashi.lib;

/**
 * konashiのイベントたち
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
public enum KonashiEvent {
    /**
     * findWithNameで指定した名前のkonashiが見つからなかった時、もしくはまわりにBLEデバイスがなかった時
     */
    PERIPHERAL_NOT_FOUND,
    /**
     * BLEデバイス選択ダイアログをキャンセルした時
     */
    CANCEL_SELECT_KONASHI,
    /**
     * konashiに接続した時(まだこの時はkonashiが使える状態ではありません)
     */
    CONNECTED,
    /**
     * konashiとの接続を切断した時
     */
    DISCONNECTED,
    /**
     * konashiに接続完了した時(この時からkonashiにアクセスできるようになります)
     */
    READY,
    /**
     * PIOの入力の状態が変化した時
     */
    UPDATE_PIO_INPUT,
    /**
     * AIOのどれかのピンの電圧が取得できた時
     */
    UPDATE_ANALOG_VALUE,
    /**
     * AIO0の電圧が取得できた時
     */
    UPDATE_ANALOG_VALUE_AIO0,
    /**
     * AIO1の電圧が取得できた時
     */
    UPDATE_ANALOG_VALUE_AIO1,
    /**
     * AIO2の電圧が取得できた時
     */
    UPDATE_ANALOG_VALUE_AIO2,
    /**
     * I2Cからデータを受信した時
     */
    I2C_READ_COMPLETE,
    /**
     * UARTのRxからデータを受信した時
     */
    UART_RX_COMPLETE,
    /**
     * konashiのバッテリーのレベルを取得できた時
     */
    UPDATE_BATTERY_LEVEL,
    /**
     * konashiの電波強度を取得できた時
     */
    UPDATE_SIGNAL_STRENGTH
}
