package com.yada.smartpos.handler;

import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.db.service.TransLogService;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransLog;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.util.List;

public class SettlementHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    public SettlementHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
    }

    public void bill() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.BILL);
        handleListener.loadingView();
        int debitNum = 0;
        int debitAmt = 0;
        int creditNum = 0;
        int creditAmt = 0;
        TransLogService service = new TransLogService(((App) mainActivity.getApplication()).getDbManager());
        List<TransLog> transLogs = service.findAll();
        for (TransLog transLog : transLogs) {
            if (Integer.parseInt(transLog.getCardType()) % 2 == 1) {
                debitNum += 1;
                debitAmt += Integer.parseInt(transLog.getAmount());
            } else {
                creditNum += 1;
                creditAmt += Integer.parseInt(transLog.getAmount());
            }
        }
        IMessage respMessage = mainActivity.getVirtualPos().createTraner().
                settlement(debitNum, debitAmt, creditNum, creditAmt);
        if(null != respMessage && "00".equals(respMessage.getFieldString(39))){
            service.deleteByBatchNo(respMessage.getFieldString(61).substring(0, 6));
            mainActivity.getVirtualPos().resetSingIn();
        }
        ResultHandler.result(mainActivity, respMessage);
        handleListener.resultView();
    }
}
