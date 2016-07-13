package com.yada.smartpos.handler;

import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.pos.sdk.util.BytesUtils;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.*;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.util.SharedPreferencesUtil;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

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
                        transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(),
                        transData.getSecondTrackData(), transData.getThirdTrackData(),
                        transData.getPin(), transData.getIcCardData(),
                        transData.getInstallmentPlanId(), Integer.parseInt(transData.getInstallmentNumber()));
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

        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        if (transResult != null && "1".equals(transResult.getTransCode()) && transResult.getMessageResp() != null)
            SharedPreferencesUtil.setStringParam(mainActivity,
                    ((App) mainActivity.getApplication()).getTransResult().getMessageResp().getFieldString(11),
                    ((App) mainActivity.getApplication()).getTransResult().getTransResp());

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
        String unpack = SharedPreferencesUtil.getStringParam(mainActivity,
                ((App) mainActivity.getApplication()).getTransData().getOldProofNo());
        // 未查到原交易抛出异常
        IMessage oldMessage;
        if (null != unpack && !"".equals(unpack)) {
            oldMessage = mainActivity.getPacker().unpack(ByteBuffer.wrap(BytesUtils.hexStringToBytes(unpack)));
        } else {
            throw new PackagingException("未查到原交易");
        }
        // 把交易信息放到App中
        // 原交易卡号
        ((App) mainActivity.getApplication()).getTransData().setAccount(oldMessage.getFieldString(2));
        // 原交易金额
        ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(oldMessage.getFieldString(4)));
        // 原交易授权码
        ((App) mainActivity.getApplication()).getTransData().setOldAuthCode(oldMessage.getFieldString(38));
        // 原系统跟踪号
        ((App) mainActivity.getApplication()).getTransData().setOldTraceNo(oldMessage.getFieldString(11));
        // 原交易日期
        ((App) mainActivity.getApplication()).getTransData().setOldTransDate(oldMessage.getFieldString(13));
        // 原交易时间
        ((App) mainActivity.getApplication()).getTransData().setOldTransTime(oldMessage.getFieldString(12));

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
                controller.startEmv(ProcessingCode.RETURNS , InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
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
