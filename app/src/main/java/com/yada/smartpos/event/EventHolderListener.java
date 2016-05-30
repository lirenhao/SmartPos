package com.yada.smartpos.event;


import android.os.Handler;
import com.newland.mtype.event.DeviceEvent;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.PinInputEvent;

public class EventHolderListener <T extends DeviceEvent> implements DeviceEventListener<T> {

    private T event;

    private final Object syncObj = new Object();

    private boolean isClosed = false;

    @Override
    public void onEvent(T event, Handler handler) {
        this.event = event;
        if (event instanceof PinInputEvent && ((PinInputEvent) event).isProcessing()) {
            return;
        }
        synchronized (syncObj) {
            isClosed = true;
            syncObj.notify();
        }
    }

    @Override
    public Handler getUIHandler() {
        return null;
    }

    public void startWait() throws InterruptedException {
        synchronized (syncObj) {
            if (!isClosed)
                syncObj.wait();
        }
    }
}
