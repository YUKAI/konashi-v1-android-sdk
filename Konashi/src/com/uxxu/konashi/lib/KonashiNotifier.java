package com.uxxu.konashi.lib;

import java.util.ArrayList;

public class KonashiNotifier {
    private ArrayList<KonashiObserver> mObservers = null;
    
    public KonashiNotifier() {
        mObservers = new ArrayList<KonashiObserver>();
    }
    
    public void addEventListener(KonashiObserver listener){
        if(!mObservers.contains(listener)){
            mObservers.add(listener);
        }
    }
    
    public void removeEventListener(KonashiObserver listener){
        if(mObservers.contains(listener)){
            mObservers.remove(listener);
        }
    }
    
    public void removeAllEventListeners(){
        mObservers.clear();
    }
    
    public void notifyKonashiEvent(final String event){
        for(final KonashiObserver observer: mObservers){
            if(observer.getActivity().isDestroyed()){
                break;
            }
            
            observer.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(event.equals(KonashiEvent.READY)){
                        observer.onKonashiReady();
                    }
                    else if(event.equals(KonashiEvent.UPDATE_PIO_INPUT)){
                        observer.onUpdatePioInput();
                    }
                    else if(event.equals(KonashiEvent.CANCEL_SELECT_KONASHI)){
                        observer.onCancelSelectKonashi();
                    }
                }
            });
        }
    }
}
