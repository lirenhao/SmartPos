package com.yada.smartpos.handler;

import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.InstallmentPayListener;
import com.yada.smartpos.event.InstallmentRefundListener;
import com.yada.smartpos.event.InstallmentRevokeListener;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 分期交易
 */
public class InstallmentHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    private EmvModule emvModule;
    private EmvControllerListener transListener;
    private EmvTransController controller;

    public InstallmentHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
        this.emvModule = new EmvModuleImpl();
    }

    public void pay() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_PAY);

        // 输入金额
        handleListener.amountView();
        // 启动刷卡
        handleListener.swipeCardView();

        BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 输入分期期数
                handleListener.installmentView();
                // 磁条卡输入密码
                handleListener.inputPinView();
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getVirtualPos().createTraner().stagesPay(
                        transData.getAccount(), transData.getAmount().toString(), transData.getValidDate(), "051",
                        transData.getSequenceNumber(), transData.getSecondTrackData(), transData.getThirdTrackData(),
                        transData.getPin(), transData.getIcCardData(), transData.getInstallmentPlanId(),
                        Integer.parseInt(transData.getInstallmentNumber()), transData.getInstallmentType());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                // 开启EMV流程
                transListener = new InstallmentPayListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                transListener = new InstallmentPayListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
        // 保存交易流水
        handleListener.saveTransHandle();
        // 显示结果
        handleListener.resultView();
    }

    public void revoke() throws PackagingException, IOException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_REVOKE);
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
        // 刷卡
        handleListener.swipeCardView();

        BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getVirtualPos().createTraner().stagesRevoke(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime(),
                        transData.getInstallmentPlanId(), transData.getInstallmentNumber());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                transListener = new InstallmentRevokeListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                transListener = new InstallmentRevokeListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.RF_GOOD_SERVICE,
                        amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
        // 删除流水
        handleListener.deleteTransHandle();
        // 展示撤销信息
        handleListener.resultView();
    }

    public void refund() throws PackagingException, IOException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_REFUND);
        // 输入主管密码
        handleListener.authPasswordView();
        // 刷卡
        handleListener.swipeCardView();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
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
                // 输入分期期数
                handleListener.installmentView();
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getVirtualPos().createTraner().stagesRefund(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime(),
                        transData.getInstallmentPlanId(), transData.getInstallmentNumber());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                transListener = new InstallmentRefundListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.RETURNS, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        null, null, true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                transListener = new InstallmentRefundListener(mainActivity, handleListener);
                emvModule.initEmvModule(mainActivity);
                controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(ProcessingCode.RETURNS, InnerProcessingCode.RF_REFUND,
                        null, null, true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
        // 显示结果
        handleListener.resultView();
    }
}
