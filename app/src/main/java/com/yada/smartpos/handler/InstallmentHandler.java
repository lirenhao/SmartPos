package com.yada.smartpos.handler;

import android.os.Message;
import com.newland.pos.sdk.util.BytesUtils;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
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
    private Message message;

    public InstallmentHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void sale() throws IOException, PackagingException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_PAY);

        // 输入金额
        message = mainActivity.getFragmentHandler().obtainMessage(1);
        message.obj = "amount";
        message.sendToTarget();
        mainActivity.getAmountWaitThreat().waitForRslt();

        // 启动刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 输入分期期数
                message = mainActivity.getFragmentHandler().obtainMessage(7);
                message.obj = "installment";
                message.sendToTarget();
                mainActivity.getInstallmentWaitThreat().waitForRslt();

                // 磁条卡输入密码
                message = mainActivity.getFragmentHandler().obtainMessage(3);
                message.obj = "inputPin";
                message.sendToTarget();
                mainActivity.getInputPinWaitThreat().waitForRslt();
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getTraner().stagesPay(transData.getAccount(),
                        transData.getValidDate(), transData.getAmount().toString(), transData.getPin(),
                        transData.getInstallmentPlanId(), Integer.parseInt(transData.getInstallmentNumber()));
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
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
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    public void revoke() throws PackagingException, IOException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_REVOKE);
        // TODO 输入主管密码

        // 输入原凭证号
        message = mainActivity.getFragmentHandler().obtainMessage(4);
        message.obj = "proofNo";
        message.sendToTarget();
        mainActivity.getProofNoWaitThreat().waitForRslt();

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
        message = mainActivity.getFragmentHandler().obtainMessage(6);
        message.obj = "showForm";
        message.sendToTarget();
        mainActivity.getShowFormWaitThreat().waitForRslt();

        // 刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 联机交易
                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getTraner().stagesRevoke(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime(),
                        transData.getInstallmentPlanId(), transData.getInstallmentNumber());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }

        // 展示撤销信息
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    public void refund() throws PackagingException, IOException {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.INSTALLMENT_REFUND);
        // TODO 输入主管密码

        // 刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 输入原凭证号
                message = mainActivity.getFragmentHandler().obtainMessage(4);
                message.obj = "proofNo";
                message.sendToTarget();
                mainActivity.getProofNoWaitThreat().waitForRslt();

                // 选择交易日期
                message = mainActivity.getFragmentHandler().obtainMessage(8);
                message.obj = "dateWheel";
                message.sendToTarget();
                mainActivity.getDateWheelWaitThreat().waitForRslt();

                // 选择交易时间
                message = mainActivity.getFragmentHandler().obtainMessage(9);
                message.obj = "timeWheel";
                message.sendToTarget();
                mainActivity.getTimeWheelWaitThreat().waitForRslt();

                // 输入授权号
                message = mainActivity.getFragmentHandler().obtainMessage(5);
                message.obj = "authCode";
                message.sendToTarget();
                mainActivity.getAuthCodeWaitThreat().waitForRslt();

                // 输入退货金额
                message = mainActivity.getFragmentHandler().obtainMessage(1);
                message.obj = "amount";
                message.sendToTarget();
                mainActivity.getAmountWaitThreat().waitForRslt();

                // 输入分期期数
                message = mainActivity.getFragmentHandler().obtainMessage(7);
                message.obj = "installment";
                message.sendToTarget();
                mainActivity.getInstallmentWaitThreat().waitForRslt();

                TransData transData = ((App) mainActivity.getApplication()).getTransData();
                IMessage iMessage = mainActivity.getTraner().stagesRefund(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime(),
                        transData.getInstallmentPlanId(), transData.getInstallmentNumber());
                ResultHandler.result(mainActivity, iMessage);
                break;
            case ICCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }
        // 显示结果
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }
}
