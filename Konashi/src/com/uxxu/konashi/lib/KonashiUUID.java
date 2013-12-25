package com.uxxu.konashi.lib;

import java.util.UUID;

/**
 * konashiで使用するGATTのUUID
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
public class KonashiUUID {
    // konashi service UUID
    public static final String BASE_UUID_STRING = "-0000-1000-8000-00805F9B34FB";
    public static final UUID SERVICE_UUID = UUID.fromString("0000FF00" + BASE_UUID_STRING);
   
    // konashi characteristics
    public static final UUID PIO_SETTING_UUID                     = UUID.fromString("00003000" + BASE_UUID_STRING);
    public static final UUID PIO_PULLUP_UUID                      = UUID.fromString("00003001" + BASE_UUID_STRING);
    public static final UUID PIO_OUTPUT_UUID                      = UUID.fromString("00003002" + BASE_UUID_STRING);
    public static final UUID PIO_INPUT_NOTIFICATION_UUID          = UUID.fromString("00003003" + BASE_UUID_STRING);
    
    public static final UUID PWM_CONFIG_UUID                      = UUID.fromString("00003004" + BASE_UUID_STRING);
    public static final UUID PWM_PARAM_UUID                       = UUID.fromString("00003005" + BASE_UUID_STRING);
    public static final UUID PWM_DUTY_UUID                        = UUID.fromString("00003006" + BASE_UUID_STRING);

    public static final UUID ANALOG_DRIVE_UUID                    = UUID.fromString("00003007" + BASE_UUID_STRING);
    public static final UUID ANALOG_READ0_UUID                    = UUID.fromString("00003008" + BASE_UUID_STRING);
    public static final UUID ANALOG_READ1_UUID                    = UUID.fromString("00003009" + BASE_UUID_STRING);
    public static final UUID ANALOG_READ2_UUID                    = UUID.fromString("0000300A" + BASE_UUID_STRING);

    public static final UUID I2C_CONFIG_UUID                      = UUID.fromString("0000300B" + BASE_UUID_STRING);
    public static final UUID I2C_START_STOP_UUID                  = UUID.fromString("0000300C" + BASE_UUID_STRING);
    public static final UUID I2C_WRITE_UUID                       = UUID.fromString("0000300D" + BASE_UUID_STRING);
    public static final UUID I2C_READ_PARAM_UUID                  = UUID.fromString("0000300E" + BASE_UUID_STRING);
    public static final UUID I2C_READ_UUID                        = UUID.fromString("0000300F" + BASE_UUID_STRING);

    public static final UUID UART_CONFIG_UUID                     = UUID.fromString("00003010" + BASE_UUID_STRING);
    public static final UUID UART_BAUDRATE_UUID                   = UUID.fromString("00003011" + BASE_UUID_STRING);
    public static final UUID UART_TX_UUID                         = UUID.fromString("00003012" + BASE_UUID_STRING);
    public static final UUID UART_RX_NOTIFICATION_UUID            = UUID.fromString("00003013" + BASE_UUID_STRING);

    public static final UUID HARDWARE_RESET_UUID                  = UUID.fromString("00003014" + BASE_UUID_STRING);
    public static final UUID HARDWARE_LOW_BAT_NOTIFICATION_UUID   = UUID.fromString("00003015" + BASE_UUID_STRING);
    
    // konashi characteristic configuration
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG         = UUID.fromString("00002902" + BASE_UUID_STRING);
}
