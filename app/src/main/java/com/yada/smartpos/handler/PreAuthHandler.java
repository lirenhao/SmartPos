package com.yada.smartpos.handler;

import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.*;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 预授权交易
 */
public class PreAuthHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    private EmvModule emvModule;
    private EmvControllerListener transListener;
    private EmvTransController controller;

    public PreAuthHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
        this.emvModule = new EmvModuleImpl();
    }

    /**
     * 预授权
     */
    public void preAuth() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH);
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
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().preAuth(
                            transData.getAccount(), transData.getAmount().toString(),
                            transData.getValidDate(), "901", transData.getSequenceNumber(),
                            transData.getSecondTrackData(), transData.getThirdTrackData(),
                            transData.getPin(), transData.getIcCardData());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new PreAuthTransListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new PreAuthTransListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    // TODO 非接的用那种内部交易类型
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        handleListener.resultView();
    }

    /**
     * 预授权撤销
     */
    public void preAuthRevoke() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_REVOKE);
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
                    // 输入撤销金额
                    handleListener.amountView();
                    // 联机交易
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().preAuthRevoke(
                            transData.getAccount(), transData.getAmount().toString(),
                            transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                            transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                            transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new PreAuthRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            null, new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new PreAuthRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            null, new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        // 显示结果
        handleListener.resultView();
    }

    /**
     * 预授权完成请求
     * 1.	主机将会为预授权完成交易检查原始的预授权交易，如果查不到原交易则拒绝
     * 2.	主机将检查，如果原始预授权交易是EMV交易，则发起预授权完成交易的POS也必须支持EMV交易。
     * 3.	对银联EMV相关的卡，在进行完成交易时，只能使用预授权完成通知交易
     */
    public void complete() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_COMPLETE);
        do {
            // 刷卡
            handleListener.swipeCardView();
            // 判断是IC卡还是磁条卡
            switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
                case MSCARD:
                    ((App) mainActivity.getApplication()).setFallback(false);
                    // 选择交易日期
                    handleListener.dateWheelView();
                    // 输入授权号
                    handleListener.authCodeView();
                    // 输入金额
                    handleListener.amountView();
                    // 联机交易
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().preAuthComplete(transData.getAccount(),
                            transData.getAmount().toString(), transData.getValidDate(), "901", transData.getSequenceNumber(),
                            transData.getSecondTrackData(), transData.getThirdTrackData(), transData.getPin());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new PreAuthCompleteListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            null, new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new PreAuthCompleteListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            null, new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        // 保存交易流水
        handleListener.saveTransHandle();
        // 显示结果
        handleListener.resultView();
    }

    /**
     * 预授权完成撤销
     */
    public void completeRevoke() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_COMPLETE_REVOKE);
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
                    // 联机交易
                    TransData transData = ((App) mainActivity.getApplication()).getTransData();
                    IMessage iMessage = mainActivity.getVirtualPos().createTraner().preAuthCompleteRevoke(transData.getAccount(),
                            transData.getAmount().toString(), transData.getValidDate(), "901", transData.getSequenceNumber(),
                            transData.getSecondTrackData(), transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                            transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                    ResultHandler.result(mainActivity, iMessage);
                    break;
                case ICCARD:
                    // 开启EMV流程
                    transListener = new PreAuthCompleteRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                case RFCARD:
                    transListener = new PreAuthCompleteRevokeListener(mainActivity, handleListener);
                    emvModule.initEmvModule(mainActivity);
                    controller = emvModule.getEmvTransController(transListener);
                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.TRANS_PREAUTH,
                            amount.movePointLeft(2), new BigDecimal("0"), true);
                    mainActivity.getWaitThreat().waitForRslt();
                    break;
                default:
                    break;
            }
        } while ((((App) mainActivity.getApplication()).isFallback()));
        // 删除流水
        handleListener.deleteTransHandle();
        // 展示交易信息
        handleListener.resultView();
    }

}
