package com.yada.smartpos.handler;

import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.ConsumePayListener;
import com.yada.smartpos.event.ConsumeRefundListener;
import com.yada.smartpos.event.ConsumeRevokeListener;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.math.BigDecimal;

public class ConsumeHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    private EmvModule emvModule;
    private EmvControllerListener transListener;
    private EmvTransController controller;

    public ConsumeHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
        this.emvModule = new EmvModuleImpl();
    }

    public void pay() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PAY);

        // 输入金额
        handleListener.amountView();
        do {
            // 启动刷卡
            handleListener.swipeCardView();
            BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
            // 判断是IC卡还是磁条卡
            switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
                case MSCARD:
                    ((App) mainActivity.getApplication()).setFallback(false);
                    // 磁条卡输入密码
                    handleListener.inputPinView();
                    // 联机交易
                    handleListener.loadingView();
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().pay(
                            transData.getAccount(), transData.getAmount().toString(),
                            transData.getValidDate(), "901", transData.getSequenceNumber(),
                            transData.getSecondTrackData(), transData.getThirdTrackData(),
                            transData.getPin(), transData.getIcCardData());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new ConsumePayListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new ConsumePayListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.RF_GOOD_SERVICE,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        // 保存交易流水
        handleListener.saveTransHandle();
        // 展示交易结果
        handleListener.resultView();
    }

    public void revoke() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.REVOKE);

        // 输入主管密码
        handleListener.authPasswordView();
        // 输入原凭证号
        handleListener.proofNoView();
        // 查询原交易信息
        if (!handleListener.selectTransHandle()) {
            throw new PackagingException("未查到原交易");
        }
        // 展示原交易信息
        handleListener.showFormView();
        do {
            // 刷卡
            handleListener.swipeCardView();
            BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
            // 判断是IC卡还是磁条卡
            switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
                case MSCARD:
                    ((App) mainActivity.getApplication()).setFallback(false);
                    // 磁条卡输入密码
                    handleListener.inputPinView();
                    // 联机交易
                    handleListener.loadingView();
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().revoke(transData.getAccount(), transData.getAmount().toString(),
                            transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                            transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                            transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new ConsumeRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new ConsumeRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.RF_GOOD_SERVICE,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        // 删除流水
        handleListener.deleteTransHandle();
        // 展示撤销信息
        handleListener.resultView();
    }

    public void refund() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.REFUND);
        // 输入主管密码
        handleListener.authPasswordView();
        do {
            // 刷卡
            handleListener.swipeCardView();
            // 判断是IC卡还是磁条卡
            switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
                case MSCARD:
                    ((App) mainActivity.getApplication()).setFallback(false);
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
                    // 联机交易
                    handleListener.loadingView();
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().refund(
                            transData.getAccount(), transData.getAmount().toString(),
                            transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                            transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                            transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new ConsumeRefundListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.RETURNS, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                            null, null, true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new ConsumeRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.RETURNS, InnerProcessingCode.RF_REFUND,
                            null, null, true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        handleListener.resultView();
    }
}