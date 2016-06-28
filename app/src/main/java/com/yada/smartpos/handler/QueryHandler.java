package com.yada.smartpos.handler;

import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.QueryTransListener;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;

import java.io.IOException;
import java.math.BigDecimal;

public class QueryHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    public QueryHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
    }

    public void query() throws IOException, PackagingException {
        // 刷卡
        handleListener.swipeCardView();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 磁条卡输入密码
                handleListener.inputPinView();
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getVirtualPos().createTraner().query(
                        transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getIcCardData());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                EmvControllerListener transListener = new QueryTransListener(mainActivity, handleListener);
                EmvModule emvModule = new EmvModuleImpl();
                emvModule.initEmvModule(mainActivity);
                EmvTransController controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        ((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2),
                        new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }

        // 展示余额
        handleListener.resultView();
    }
}
