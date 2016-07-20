package com.yada.smartpos.handler;

import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.db.service.TransLogService;
import com.yada.smartpos.model.TransLog;

import java.io.IOException;
import java.util.List;

public class SettlementHandler {

    private MainActivity mainActivity;

    public SettlementHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void settlement() throws IOException, PackagingException {
        int debitNum = 0;
        int debitAmt = 0;
        int creditNum = 0;
        int creditAmt = 0;
        TransLogService service = new TransLogService(((App) mainActivity.getApplication()).getDbManager());
        List<TransLog> transLogs = service.findAll();
        for (TransLog transLog : transLogs) {
            if (Integer.parseInt(transLog.getCardType()) % 2 == 1) {
                debitNum += 1;
                debitAmt += Integer.parseInt(transLog.getAccount());
            } else {
                creditNum += 1;
                creditAmt += Integer.parseInt(transLog.getAccount());
            }
        }
        IMessage respMessage = mainActivity.getVirtualPos().createTraner().
                settlement(debitNum, debitAmt, creditNum, creditAmt);
    }
}
