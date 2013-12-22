package com.uxxu.konashi.lib;

import android.app.Activity;

/**
 * konashiのイベントをキャッチするためのオブザーバクラス
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
public abstract class KonashiObserver {
    /**
     * runOnUiThreadするために必要なのです。。。
     */
    private Activity mActivity;
    
    /**
     * コンストラクタ
     * @param activity Activity
     */
    public KonashiObserver(Activity activity){
        mActivity = activity;
    }
    
    /**
     * Activityを取得する
     * @return Activity
     */
    public Activity getActivity(){
        return mActivity;
    }
    
    /**
     * findWithNameで指定した名前のkonashiが見つからなかった時、もしくはまわりにBLEデバイスがなかった時に呼ばれる
     */
    public void onNotFoundPeripheral(){}   
    /**
     * konashiに接続した時(まだこの時はkonashiが使える状態ではありません)に呼ばれる
     */
    public void onConnected(){}
    /**
     * konashiとの接続を切断した時に呼ばれる
     */
    public void onDisconncted(){}
    /**
     * konashiに接続完了した時(この時からkonashiにアクセスできるようになります)に呼ばれる
     */
    public void onReady(){}
    /**
     * PIOの入力の状態が変化した時に呼ばれる
     */
    public void onUpdatePioInput(){}
    /**
     * BLEデバイス選択ダイアログをキャンセルした時に呼ばれる
     */
    public void onCancelSelectKonashi(){}
}
