package com.yada.smartpos.handler;

import android.os.Message;
import com.yada.smartpos.activity.MainActivity;

public class ConsumeHandler {

    private MainActivity mainActivity;
    private Message message;
    private WaitThreat flowHandler = new WaitThreat();

    public ConsumeHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public WaitThreat getFlowHandler() {
        return flowHandler;
    }

    public void pay() {
        message = mainActivity.getFragmentHandler().obtainMessage(1);
        message.obj = "amount";
        message.sendToTarget();
    }

    // 线程等待、唤醒
    public class WaitThreat {
        Object syncObj = new Object();

        void waitForRslt() throws InterruptedException {
            synchronized (syncObj) {
                syncObj.wait();
            }
        }

        void notifyThread() {
            synchronized (syncObj) {
                syncObj.notify();
            }
        }
    }
}
