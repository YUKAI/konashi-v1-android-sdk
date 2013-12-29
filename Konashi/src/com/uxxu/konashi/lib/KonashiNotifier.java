package com.uxxu.konashi.lib;

import java.util.ArrayList;

/**
 * konashiのイベントをKonashiObserverに伝えるクラス
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
public class KonashiNotifier {
    /**
     * オブザーバたち
     */
    private ArrayList<KonashiObserver> mObservers = null;
    
    /**
     * コンストラクタ
     */
    public KonashiNotifier() {
        mObservers = new ArrayList<KonashiObserver>();
    }
    
    /**
     * オブザーバを追加する
     * @param observer 追加するオブザーバ
     */
    public void addObserver(KonashiObserver observer){
        if(!mObservers.contains(observer)){
            mObservers.add(observer);
        }
    }
    
    /**
     * オブザーバを削除する
     * @param observer 削除するオブザーバ
     */
    public void removeObserver(KonashiObserver observer){
        if(mObservers.contains(observer)){
            mObservers.remove(observer);
        }
    }
    
    /**
     * オブザーバをすべて削除する
     */
    public void removeAllObservers(){
        mObservers.clear();
    }
    
    /**
     * オブザーバにイベントを通知する
     * @param event イベント名(KonashiEventだよっ）
     */
    public void notifyKonashiEvent(final KonashiEvent event){
        for(final KonashiObserver observer: mObservers){
            if(observer.getActivity().isDestroyed()){
                break;
            }
            
            observer.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(event){
                    case PERIPHERAL_NOT_FOUND:
                        observer.onNotFoundPeripheral();
                        break;
                    case CONNECTED:
                        observer.onConnected();
                        break;
                    case DISCONNECTED:
                        observer.onDisconncted();
                        break;
                    case READY:
                        observer.onReady();
                        break;
                    case UPDATE_PIO_INPUT:
                        observer.onUpdatePioInput();
                        break;
                    case UPDATE_ANALOG_VALUE:
                        observer.onUpdateAnalogValue();
                        break;
                    case UPDATE_ANALOG_VALUE_AIO0:
                        observer.onUpdateAnalogValueAio0();
                        break;
                    case UPDATE_ANALOG_VALUE_AIO1:
                        observer.onUpdateAnalogValueAio1();
                        break;
                    case UPDATE_ANALOG_VALUE_AIO2:
                        observer.onUpdateAnalogValueAio2();
                        break;
                    case UPDATE_BATTERY_LEVEL:
                        observer.onUpdateBatteryLevel();
                        break;
                    case CANCEL_SELECT_KONASHI:
                        observer.onCancelSelectKonashi();
                        break;
                    }
                }
            });
        }
    }
    
    public void notifyKonashiError(final KonashiErrorReason errorReason){
        for(final KonashiObserver observer: mObservers){
            if(observer.getActivity().isDestroyed()){
                break;
            }
            
            observer.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onError(errorReason);
                }
            });
        }
    }
}
