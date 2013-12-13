package com.uxxu.konashi.lib;

import java.util.ArrayList;

public class KonashiNotifier {
    private ArrayList<KonashiEventListener> mListeners = null;
    
    public KonashiNotifier() {
        mListeners = new ArrayList<KonashiEventListener>();
    }
    
    public void addEventListener(KonashiEventListener listener){
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }
    
    public void removeEventListener(KonashiEventListener listener){
        if(mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }
    
    public void removeAllEventListeners(){
        mListeners.clear();
    }
    
    public void notifyKonashiEvent(String event){
        for(KonashiEventListener listener: mListeners){
            if(event.equals(KonashiEvent.READY)){
                listener.onKonashiReady();
            }
        }
    }
}
