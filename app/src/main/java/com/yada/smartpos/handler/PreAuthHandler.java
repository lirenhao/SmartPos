package com.yada.smartpos.handler;

import android.os.Message;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.util.Client;
import com.yada.smartpos.util.PackMessage;
import com.yada.smartpos.util.SharedPreferencesUtil;
import com.yada.smartpos.util.TransType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

/**
 * 预授权交易
 */
public class PreAuthHandler {

    private MainActivity mainActivity;
    private Message message;

    public PreAuthHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * 预授权
     */
    public void preAuth() {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH);
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
                // 磁条卡输入密码
                Message message = mainActivity.getFragmentHandler().obtainMessage(3);
                message.obj = "inputPin";
                message.sendToTarget();
                mainActivity.getInputPinWaitThreat().waitForRslt();
                // 联机交易
                onLinePreAuth(((App) mainActivity.getApplication()).getTransData());
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

        SharedPreferencesUtil.setStringParam(mainActivity,"",
                ((App) mainActivity.getApplication()).getTransResult().getTransResp());

        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    /**
     * 预授权撤销
     */
    public void preAuthRevoke() {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_REVOKE);
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

                // 联机交易
                onLinePreAuthRevoke(((App) mainActivity.getApplication()).getTransData());
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

    /**
     * 预授权完成请求
     * 1.	主机将会为预授权完成交易检查原始的预授权交易，如果查不到原交易则拒绝
     * 2.	主机将检查，如果原始预授权交易是EMV交易，则发起预授权完成交易的POS也必须支持EMV交易。
     * 3.	对银联EMV相关的卡，在进行完成交易时，只能使用预授权完成通知交易
     */
    public void complete() {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_COMPLETE);
        // 刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                // 选择交易日期
                message = mainActivity.getFragmentHandler().obtainMessage(8);
                message.obj = "dateWheel";
                message.sendToTarget();
                mainActivity.getDateWheelWaitThreat().waitForRslt();

                // 输入授权号
                message = mainActivity.getFragmentHandler().obtainMessage(5);
                message.obj = "authCode";
                message.sendToTarget();
                mainActivity.getAuthCodeWaitThreat().waitForRslt();

                // 输入金额
                message = mainActivity.getFragmentHandler().obtainMessage(1);
                message.obj = "amount";
                message.sendToTarget();
                mainActivity.getAmountWaitThreat().waitForRslt();

                // 联机交易
                onLinePreAuthComplete(((App) mainActivity.getApplication()).getTransData());
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
        if (transResult != null && "1".equals(transResult.getTransCode()) && transResult.getTransResp() != null)
            SharedPreferencesUtil.setStringParam(mainActivity,
                    ((App) mainActivity.getApplication()).getTransData().getOldProofNo(),
                    ((App) mainActivity.getApplication()).getTransResult().getTransResp());

        // 显示结果
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    /**
     * 预授权完成通知
     * 1.	主机将会为预授权完成通知交易检查原始的预授权交易，如果查不到原交易则主机将拒绝
     * 2.	预授权完成通知交易不能冲正，也不能被撤消
     * 3.	预授权完成通知交易不是操作员选择发出，而是由POS根据主机对预授权完成交易的响应报文的指令发出。如果IST用响应码Z9拒绝了预授权完成报文，表示此笔完成必须使用通知报文，此时POS需要根据原预授权完成报文的相关信息，组织预授权完成通知报文重新上送到IST，如果在第一次上送完成通知报文时，收到了IST的成功响应，就认为交易成功，如果交易被IST拒绝就认为交易失败。如果POS没有收到IST对完成通知的响应报文，就认为该通知交易成功（打印签购单）但需要在下笔交易前将此通知报文重新上送IST直到收到IST的应答报文。
     */
    public void completeNotice() {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_COMPLETE_NOTICE);
    }

    /**
     * 预授权完成撤销
     */
    public void completeRevoke() throws UnsupportedEncodingException, ISO8583Exception {
        ((App) mainActivity.getApplication()).setTransData(new TransData());
        ((App) mainActivity.getApplication()).setTransResult(new TransResult());
        ((App) mainActivity.getApplication()).getTransData().setTransType(TransType.PRE_AUTH_COMPLETE_REVOKE);
        // TODO 输入主管密码

        // 输入原凭证号
        message = mainActivity.getFragmentHandler().obtainMessage(4);
        message.obj = "proofNo";
        message.sendToTarget();
        mainActivity.getProofNoWaitThreat().waitForRslt();

        // 查询原交易信息
        String unpack = SharedPreferencesUtil.getStringParam(mainActivity,
                ((App) mainActivity.getApplication()).getTransData().getOldProofNo());
        ISO8583 iso8583 = mainActivity.getIso8583();
        // 未查到原交易抛出异常
        if (null != unpack && !"".equals(unpack)){
            iso8583.unpack(unpack);
        } else {
            throw new ISO8583Exception("未查到原交易");
        }
        // 把交易信息放到App中
        // 原交易卡号
        ((App) mainActivity.getApplication()).getTransData().setAccount(iso8583.getField(2));
        // 原交易金额
        ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(iso8583.getField(4)));
        // 原交易授权码
        ((App) mainActivity.getApplication()).getTransData().setOldAuthCode(iso8583.getField(38));
        // 原系统跟踪号
        ((App) mainActivity.getApplication()).getTransData().setOldTraceNumber(iso8583.getField(11));
        // 原交易日期
        ((App) mainActivity.getApplication()).getTransData().setOldTransDate(iso8583.getField(13));
        // 原交易时间
        ((App) mainActivity.getApplication()).getTransData().setOldTransTime(iso8583.getField(12));

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
                onLineCompleteRevoke(((App) mainActivity.getApplication()).getTransData());
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

        // 展示交易信息
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    private void onLinePreAuth(TransData transData) {
        try {
            ISO8583 iso8583 = PackMessage.preAuth(mainActivity, transData);
            String pack = iso8583.pack();
            TcpClient client = mainActivity.getClient();
            String unpack = Client.send(mainActivity, client, pack, PackMessage.reverse(iso8583));
            iso8583.initPack();
            iso8583.unpack(unpack);
            ResultHandler.result(mainActivity, iso8583);
        } catch (ISO8583Exception | IOException | NullPointerException e) {
            e.printStackTrace();
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        }
    }

    private void onLinePreAuthRevoke(TransData transData) {
        try {
            ISO8583 iso8583 = PackMessage.preAuthRevoke(mainActivity, transData);
            String pack = iso8583.pack();
            TcpClient client = mainActivity.getClient();
            String unpack = Client.send(mainActivity, client, pack, PackMessage.reverse(iso8583));
            iso8583.initPack();
            iso8583.unpack(unpack);
            ResultHandler.result(mainActivity, iso8583);
        } catch (ISO8583Exception | IOException | NullPointerException e) {
            e.printStackTrace();
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        }
    }

    private void onLinePreAuthComplete(TransData transData) {
        try {
            ISO8583 iso8583 = PackMessage.preAuthComplete(mainActivity, transData);
            String pack = iso8583.pack();
            TcpClient client = mainActivity.getClient();
            String unpack = Client.send(mainActivity, client, pack, PackMessage.reverse(iso8583));
            iso8583.initPack();
            iso8583.unpack(unpack);
            ResultHandler.result(mainActivity, iso8583);
        } catch (ISO8583Exception | IOException | NullPointerException e) {
            e.printStackTrace();
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        }
    }

    private void onLineCompleteRevoke(TransData transData) {
        try {
            ISO8583 iso8583 = PackMessage.preAuthCompleteRevoke(mainActivity, transData);
            String pack = iso8583.pack();
            TcpClient client = mainActivity.getClient();
            String unpack = Client.send(mainActivity, client, pack, PackMessage.reverse(iso8583));
            iso8583.initPack();
            iso8583.unpack(unpack);
            ResultHandler.result(mainActivity, iso8583);
        } catch (ISO8583Exception | IOException | NullPointerException e) {
            e.printStackTrace();
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
            ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        }
    }

}
