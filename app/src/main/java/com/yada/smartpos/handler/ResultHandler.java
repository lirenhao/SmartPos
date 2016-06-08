package com.yada.smartpos.handler;

import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;

public class ResultHandler {

    public static void result(MainActivity mainActivity, ISO8583 iso8583) throws ISO8583Exception {
        if ("00".equals(iso8583.getField(39))) {
            // 凭证号就是流水号
            ((App) mainActivity.getApplication()).getTransData().setOldProofNo(iso8583.getField(11));
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("1");
            ((App) mainActivity.getApplication()).getTransResult().setTransResp(iso8583.pack());
        } else {
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(iso8583.getField(39));
        }
    }

}
