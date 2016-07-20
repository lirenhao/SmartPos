package com.yada.smartpos.handler;

import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;

public class ResultHandler {

    public static void result(MainActivity mainActivity, IMessage iMessage) {

        if (null != iMessage && "00".equals(iMessage.getFieldString(39))) {
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("1");
        } else {
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
        }

        // 凭证号就是流水号
        if (null != iMessage) {
            String transResp = iMessage.toString();
            ((App) mainActivity.getApplication()).getTransResult().setTransResp(transResp);
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(iMessage.getFieldString(39));
        } else {
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg("");
        }
        ((App) mainActivity.getApplication()).getTransResult().setMessageResp(iMessage);
    }

}