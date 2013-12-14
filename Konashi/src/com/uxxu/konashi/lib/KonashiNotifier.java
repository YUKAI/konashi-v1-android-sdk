package com.uxxu.konashi.lib;

import java.util.ArrayList;

public class KonashiNotifier {
    private ArrayList<KonashiObserver> mListeners = null;
    
    public KonashiNotifier() {
        mListeners = new ArrayList<KonashiObserver>();
    }
    
    public void addEventListener(KonashiObserver listener){
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }
    
    public void removeEventListener(KonashiObserver listener){
        if(mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }
    
    public void removeAllEventListeners(){
        mListeners.clear();
    }
    
    public void notifyKonashiEvent(String event){
        for(KonashiObserver listener: mListeners){
            if(event.equals(KonashiEvent.READY)){
                listener.onKonashiReady();
            }
        }
    }
}
