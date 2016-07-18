package com.yada.smartpos.event;

import android.os.Message;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.tlv.TLVPackage;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.device.pos.posp.params.Block01;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.db.service.TransLogService;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransLog;
import com.yada.smartpos.model.TransResult;
import org.xutils.ex.DbException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransHandleListener {

    private List<Integer> L_55TAGS = new ArrayList<>();
    private MainActivity mainActivity;

    public TransHandleListener(MainActivity mainActivity) {
        L_55TAGS.add(0x9f26);
        L_55TAGS.add(0x9F27);
        L_55TAGS.add(0x9F10);
        L_55TAGS.add(0x9F37);
        L_55TAGS.add(0x9F36);
        L_55TAGS.add(0x95);
        L_55TAGS.add(0x9A);
        L_55TAGS.add(0x9C);
        L_55TAGS.add(0x9F02);
        L_55TAGS.add(0x5F2A);
        L_55TAGS.add(0x82);
        L_55TAGS.add(0x9F1A);
        L_55TAGS.add(0x9F03);
        L_55TAGS.add(0x9F33);
        L_55TAGS.add(0x9F74);
        L_55TAGS.add(0x9F34);
        L_55TAGS.add(0x9F35);
        L_55TAGS.add(0x9F1E);
        L_55TAGS.add(0x84);
        L_55TAGS.add(0x9F09);
        L_55TAGS.add(0x9F41);
        L_55TAGS.add(0x91);
        L_55TAGS.add(0x71);
        L_55TAGS.add(0x72);
        L_55TAGS.add(0xDF31);
        L_55TAGS.add(0x9F63);
        L_55TAGS.add(0x8A);
        L_55TAGS.add(0xDF32);
        L_55TAGS.add(0xDF33);
        L_55TAGS.add(0xDF34);
        L_55TAGS.add(0xDF75);
        this.mainActivity = mainActivity;
    }

    public void amountView() {
        // 调用输入金额界面
        Message message = mainActivity.getFragmentHandler().obtainMessage(1);
        message.obj = "amount";
        message.sendToTarget();
        mainActivity.getAmountWaitThreat().waitForRslt();
    }

    public void swipeCardView() {
        // 启动刷卡
        Message message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();
    }

    public void inputPinView() {
        // 调用密码键盘
        Message message = mainActivity.getFragmentHandler().obtainMessage(3);
        message.obj = "inputPin";
        message.sendToTarget();
        mainActivity.getInputPinWaitThreat().waitForRslt();
    }

    public void authPasswordView() {
        // TODO 输入主管密码
    }

    public void proofNoView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(4);
        message.obj = "proofNo";
        message.sendToTarget();
        mainActivity.getProofNoWaitThreat().waitForRslt();
    }

    public void authCodeView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(5);
        message.obj = "authCode";
        message.sendToTarget();
        mainActivity.getAuthCodeWaitThreat().waitForRslt();
    }

    public void showFormView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(6);
        message.obj = "showForm";
        message.sendToTarget();
        mainActivity.getShowFormWaitThreat().waitForRslt();
    }

    public void installmentView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(7);
        message.obj = "installment";
        message.sendToTarget();
        mainActivity.getInstallmentWaitThreat().waitForRslt();
    }

    public void dateWheelView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(8);
        message.obj = "dateWheel";
        message.sendToTarget();
        mainActivity.getDateWheelWaitThreat().waitForRslt();
    }

    public void timeWheelView() {
        Message message = mainActivity.getFragmentHandler().obtainMessage(9);
        message.obj = "timeWheel";
        message.sendToTarget();
        mainActivity.getTimeWheelWaitThreat().waitForRslt();
    }

    public void resultView() {
        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        StringBuilder result = new StringBuilder();
        if (null == transResult) {
            result.append("交易失败\n交易结果为空");
        } else {
            if ("1".equals(transResult.getTransCode())) {
                Block01 paramBlock01 = ((App) mainActivity.getApplication()).getParamBlock01();
                IMessage messageResp = transResult.getMessageResp();
                switch (((App) mainActivity.getApplication()).getTransData().getTransType()) {
                    case QUERY:
                        String field54 = messageResp.getFieldString(54);
                        BigDecimal ledgerBalance = new BigDecimal(0);
                        BigDecimal availableBalance = new BigDecimal(0);
                        String tag, value;
                        int index = 0;
                        while (index < field54.length()) {
                            tag = field54.substring(index + 2, index + 4);
                            value = field54.substring(index + 2 + 2 + 4, index + 2 + 2 + 4 + 12);
                            index = index + 20;
                            if (tag.equals("01")) {
                                ledgerBalance = new BigDecimal(value);
                            }
                            if (tag.equals("02")) {
                                availableBalance = new BigDecimal(value);
                            }
                        }
                        result.append("余额查询\n\n").
                                append("账户余额：").append(ledgerBalance.movePointLeft(2).toString()).append("\n").
                                append("可用余额：").append(availableBalance.movePointLeft(2).toString());
                        break;
                    default:
                        result.append("\n\n").append("           ").
                                append("签购单").append("\n\n").
                                append("商户名称：").append(paramBlock01.cnMerName).append("\n").
                                append("商户编号：").append(messageResp.getFieldString(42)).append("\n").
                                append("终端编号：").append(messageResp.getFieldString(41)).append("\n").
                                append("操作员号：001\n").
                                append("交易类型：").append(((App) mainActivity.getApplication()).getTransData().getTransType().transType).append("\n").
                                append("卡号：").append(messageResp.getFieldString(2)).append("\n").
                                append("凭证号：").append(messageResp.getFieldString(11)).append("\n").
                                append("授权码：").append(messageResp.getFieldString(38)).append("\n").
                                append("参考号：").append(messageResp.getFieldString(37)).append("\n").
                                append("日期时间：").append(messageResp.getFieldString(13)).append(messageResp.getFieldString(12)).append("\n").
                                append("金额：").append(new BigDecimal(messageResp.getFieldString(4)).movePointLeft(2).toString()).append("\n").
                                append("--------------------------------\n").append("\n\n\n\n\n");
                }
            } else {
                result.append("交易失败\n").append(transResult.getTransMsg());
            }
        }
        ((App) mainActivity.getApplication()).getTransResult().setResultText(result.toString());
        Message message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    public void emvDataHandle(EmvTransInfo transInfo) {
        TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
        ((App) mainActivity.getApplication()).getTransData().setAccount(transInfo.getCardNo());
        ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(transInfo.getAmountAuthorisedNumeric()));
        if (null != transInfo.getCardExpirationDate() && !"".equals(transInfo.getCardExpirationDate())) {
            ((App) mainActivity.getApplication()).getTransData().setValidDate(transInfo.getCardExpirationDate().substring(0, 4));
        }
        ((App) mainActivity.getApplication()).getTransData().setSequenceNumber(transInfo.getCardSequenceNumber());
        ((App) mainActivity.getApplication()).getTransData().setSecondTrackData(HexUtil.toHexString(transInfo.getTrack_2_eqv_data()).substring(0, 37));
        ((App) mainActivity.getApplication()).getTransData().setIcCardData(HexUtil.toHexString(tlvPackage.pack()));
    }

    public void emvResultHandle(EmvTransController controller, EmvTransInfo transInfo, IMessage respMessage) {
        if (null != respMessage) {
            switch (transInfo.getOpenCardType()) {
                case COMMON_ICCARDREADER:
                    SecondIssuanceRequest request = new SecondIssuanceRequest();
                    // 取自银联8583规范39域值,该参数按交易实际值填充
                    request.setAuthorisationResponseCode(respMessage.getFieldString(39));
                    //取自银联8583规范 38域值,该参数按交易实际值填充
                    request.setAuthorisationCode(respMessage.getFieldString(38));

                    if (null != respMessage.getFieldString(55)) {
                        TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
                        tlvPackage.unpack(HexUtil.parseHex(respMessage.getFieldString(55)));
                        //取自银联8583规范 55域0x91值,该参数按交易实际值填充
                        request.setIssuerAuthenticationData(tlvPackage.getValue(0x91));
                        //取自银联8583规范 55域0x71值,该参数按交易实际值填充
                        request.setIssuerScriptTemplate1(tlvPackage.getValue(0x71));
                        //取自银联8583规范 55域0x72值,该参数按交易实际值填充
                        request.setIssuerScriptTemplate2(tlvPackage.getValue(0x72));
                    }
                    // [步骤2].ic卡联机交易成功或者非接圈存交易，调用二次授权接口，等回调onemvfinished结束流程。
                    controller.secondIssuance(request);
                    break;
                case COMMON_RFCARDREADER:
                    if ("00".equals(respMessage.getFieldString(39))) {
                        controller.doEmvFinish(true);
                    } else {
                        controller.doEmvFinish(false);
                    }
                    break;
            }
        } else {
            // [并列步骤2].联机交易失败或者非接交易(除圈存外)调用emv结束方法，结束流程。
            controller.doEmvFinish(false);
        }
    }

    public void emvFallbackHandle(EmvTransInfo transInfo) {
        // TODO 降级处理
    }

    public void saveTransHandle() throws DbException {
        TransData transData = ((App) mainActivity.getApplication()).getTransData();
        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        if (transResult != null && "1".equals(transResult.getTransCode()) && transResult.getMessageResp() != null) {
            IMessage message = transResult.getMessageResp();

            TransLog transLog = new TransLog();
            transLog.setTraceNo(message.getFieldString(11));
            transLog.setTransType(transData.getTransType().toString());
            transLog.setCardType(transData.getCardType().toString());
            transLog.setAccount(message.getFieldString(2));
            transLog.setAuthCode(message.getFieldString(38));
            transLog.setTransTime(message.getFieldString(12));
            transLog.setTransDate(message.getFieldString(13));
            transLog.setAmount(message.getFieldString(4));
            transLog.setBatchNo(message.getFieldString(61).substring(0, 6));

            TransLogService service = new TransLogService(((App) mainActivity.getApplication()).getDbManager());
            service.save(transLog);
        }
    }

    public void deleteTransHandle() throws DbException {
        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        if (transResult != null && "1".equals(transResult.getTransCode()) && transResult.getMessageResp() != null) {
            TransLogService service = new TransLogService(((App) mainActivity.getApplication()).getDbManager());
            service.deleteById(((App) mainActivity.getApplication()).getTransData().getOldProofNo());
        }
    }

    public boolean selectTransHandle() throws DbException {
        TransLogService service = new TransLogService(((App) mainActivity.getApplication()).getDbManager());
        TransLog transLog = service.findById(((App) mainActivity.getApplication()).getTransData().getOldProofNo());
        if (null != transLog) {
            // 原交易卡号
            ((App) mainActivity.getApplication()).getTransData().setAccount(transLog.getAccount());
            // 原交易金额
            ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(transLog.getAmount()));
            // 原交易授权码
            ((App) mainActivity.getApplication()).getTransData().setOldAuthCode(transLog.getAuthCode());
            // 原系统跟踪号
            ((App) mainActivity.getApplication()).getTransData().setOldTraceNo(transLog.getTraceNo());
            // 原交易日期
            ((App) mainActivity.getApplication()).getTransData().setOldTransDate(transLog.getTransDate());
            // 原交易时间
            ((App) mainActivity.getApplication()).getTransData().setOldTransTime(transLog.getTransTime());
            return true;
        } else {
            return false;
        }
    }
}
