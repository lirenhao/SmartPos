package com.yada.smartpos.handler;

import android.os.Message;
import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.EmvTransListener;
import com.yada.smartpos.event.SimpleTransferListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;

import java.io.IOException;
import java.math.BigDecimal;

public class QueryHandler {

    private MainActivity mainActivity;
    private Message message;

    public QueryHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void query() throws IOException, PackagingException {
        // 刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        EmvTransListener transListener = new SimpleTransferListener(mainActivity);
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);
        EmvTransController controller = emvModule.getEmvTransController(transListener);
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 磁条卡输入密码
                message = mainActivity.getFragmentHandler().obtainMessage(3);
                message.obj = "inputPin";
                message.sendToTarget();
                mainActivity.getInputPinWaitThreat().waitForRslt();
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getTraner().query(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getIcCardData());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        ((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2),
                        new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.EC_CONSUMPTION,
                        ((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2),
                        new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }

        // 展示余额
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "success";
        message.sendToTarget();
    }
}
