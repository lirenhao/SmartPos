package com.yada.smartpos.event;

import android.os.Message;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.tlv.TLVPackage;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;

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
        Message message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "result";
        message.sendToTarget();
    }

    public void emvDataHandle(EmvTransInfo transInfo) {
        TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
        ((App) mainActivity.getApplication()).getTransData().setAccount(transInfo.getCardNo());
        ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(transInfo.getAmountAuthorisedNumeric()));
        if(null != transInfo.getCardExpirationDate() && !"".equals(transInfo.getCardExpirationDate())){
            ((App) mainActivity.getApplication()).getTransData().setValidDate(transInfo.getCardExpirationDate().substring(0, 4));
        }
        ((App) mainActivity.getApplication()).getTransData().setSequenceNumber(transInfo.getCardSequenceNumber());
        ((App) mainActivity.getApplication()).getTransData().setSecondTrackData(HexUtil.toHexString(transInfo.getTrack_2_eqv_data()).substring(0, 37));
        ((App) mainActivity.getApplication()).getTransData().setIcCardData(HexUtil.toHexString(tlvPackage.pack()));
    }

    public void emvResultHandle(EmvTransController controller, EmvTransInfo transInfo, IMessage respMessage) {
        if (null != respMessage && "00".equals(respMessage.getFieldString(39))) {
            SecondIssuanceRequest request = new SecondIssuanceRequest();
            request.setAuthorisationResponseCode(respMessage.getFieldString(39));// 取自银联8583规范39域值,该参数按交易实际值填充
            request.setAuthorisationCode(respMessage.getFieldString(38));//取自银联8583规范 38域值,该参数按交易实际值填充

            if (null != respMessage.getFieldString(55)) {
                TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
                tlvPackage.unpack(HexUtil.parseHex(respMessage.getFieldString(55)));
                request.setIssuerAuthenticationData(tlvPackage.getValue(0x91));//取自银联8583规范 55域0x91值,该参数按交易实际值填充
                request.setIssuerScriptTemplate1(tlvPackage.getValue(0x71));//取自银联8583规范 55域0x71值,该参数按交易实际值填充
                request.setIssuerScriptTemplate2(tlvPackage.getValue(0x72));//取自银联8583规范 55域0x72值,该参数按交易实际值填充
            }
            // [步骤2].ic卡联机交易成功或者非接圈存交易，调用二次授权接口，等回调onemvfinished结束流程。
            controller.secondIssuance(request);
        } else {
            // [并列步骤2].联机交易失败或者非接交易(除圈存外)调用emv结束方法，结束流程。
            controller.doEmvFinish(false);
        }
    }
}
