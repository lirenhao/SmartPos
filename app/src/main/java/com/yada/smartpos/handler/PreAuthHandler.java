package com.yada.smartpos.handler;

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
 * 预授权交易
 */
public class PreAuthHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    public PreAuthHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
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
        // 启动刷卡
        handleListener.swipeCardView();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
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
                EmvControllerListener transListener = new PreAuthTransListener(mainActivity, handleListener);
                EmvModule emvModule = new EmvModuleImpl();
                emvModule.initEmvModule(mainActivity);
                EmvTransController controller = emvModule.getEmvTransController(transListener);
                BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
                // TODO 开启EMV预授权流程的参数设置需确认
                controller.startEmv(amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
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
                EmvControllerListener transListener = new PreAuthRevokeListener(mainActivity, handleListener);
                EmvModule emvModule = new EmvModuleImpl();
                emvModule.initEmvModule(mainActivity);
                EmvTransController controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(new BigDecimal("0"), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
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
        // 刷卡
        handleListener.swipeCardView();
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
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
                EmvControllerListener transListener = new PreAuthCompleteListener(mainActivity, handleListener);
                EmvModule emvModule = new EmvModuleImpl();
                emvModule.initEmvModule(mainActivity);
                EmvTransController controller = emvModule.getEmvTransController(transListener);
                controller.startEmv(new BigDecimal("0"), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
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
        String unpack = SharedPreferencesUtil.getStringParam(mainActivity,
                ((App) mainActivity.getApplication()).getTransData().getOldProofNo());
        // 未查到原交易抛出异常
        IMessage oldMessage;
        if (null != unpack && !"".equals(unpack)) {
            oldMessage = mainActivity.getPacker().unpack(ByteBuffer.wrap(BytesUtils.hexStringToBytes(unpack)));
        } else {
            throw new PackagingException("未查到原交易");
        }
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
        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
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
                EmvControllerListener transListener = new PreAuthCompleteRevokeListener(mainActivity, handleListener);
                EmvModule emvModule = new EmvModuleImpl();
                emvModule.initEmvModule(mainActivity);
                EmvTransController controller = emvModule.getEmvTransController(transListener);
                BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
                controller.startEmv(amount.movePointLeft(2), new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }

        // 展示交易信息
        handleListener.resultView();
    }

}
