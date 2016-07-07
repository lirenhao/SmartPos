package com.yada.smartpos.event;

import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.handler.ResultHandler;
import com.yada.smartpos.model.TransData;

import java.math.BigDecimal;

/**
 * 退货Emv流程控制监听
 */
public class ConsumeRefundListener implements EmvControllerListener {

    private TransHandleListener handleListener;
    private MainActivity mainActivity;
    private IMessage respMessage;

    public ConsumeRefundListener(MainActivity mainActivity, TransHandleListener handleListener) {
        this.handleListener = handleListener;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onRequestSelectApplication(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        // 错误的事件返回，不可能要求应用选择！
        controller.cancelEmv();
    }

    @Override
    public void onRequestTransferConfirm(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        // 交易确认完成
        controller.transferConfirm(true);
    }

    @Override
    public void onRequestAmountEntry(EmvTransController controller, EmvTransInfo transInfo) {
        // 调用输入金额界面
        handleListener.amountView();
        BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
        controller.sendAmtInputResult(amount.movePointLeft(2));
    }

    @Override
    public void onRequestPinEntry(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        // 退货输入密码是可选项
        controller.sendPinInputResult(new byte[6]);
    }

    @Override
    public void onRequestOnline(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        handleListener.emvDataHandle(transInfo);
        // 输入原凭证号
        handleListener.proofNoView();
        // 选择交易日期
        handleListener.dateWheelView();
        // 选择交易时间
        handleListener.timeWheelView();
        // 输入授权号
        handleListener.authCodeView();
        // 输入退货金额
        handleListener.amountView();

        TransData transData = ((App) mainActivity.getApplication()).getTransData();
        respMessage = mainActivity.getVirtualPos().createTraner().refund(transData.getAccount(), transData.getAmount().toString(),
                transData.getValidDate(), "051", transData.getSequenceNumber(), transData.getSecondTrackData(),
                transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());

        handleListener.emvResultHandle(controller, transInfo, respMessage);
    }

    @Override
    public void onEmvFinished(boolean isSuccess, EmvTransInfo transInfo) throws Exception {
        ResultHandler.result(mainActivity, respMessage);
        mainActivity.getWaitThreat().notifyThread();
    }

    @Override
    public void onFallback(EmvTransInfo emvTransInfo) throws Exception {
        // TODO 降级处理
    }

    @Override
    public void onError(EmvTransController emvTransController, Exception e) {
        ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
        ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        mainActivity.getWaitThreat().notifyThread();
    }
}
