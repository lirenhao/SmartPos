package com.yada.smartpos.event;

import android.os.Message;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.emv.level2.EmvCardholderCertType;
import com.newland.mtype.tlv.TLVPackage;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.util.Const.MessageTag;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Emv流程控制监听和QPBOC流程控制监听
 */
public class SimpleTransferListener implements EmvTransListener {

    private MainActivity mainActivity;
    private List<Integer> L_55TAGS = new ArrayList<>();

    public SimpleTransferListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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
    }

    // ExecuteRslt结果集：
    // 0x00/*成功,可能是脱机余额查询、明细成功或简易流程成功*/
    // 0x01/*交易授受*/
    // 0x02/*交易拒绝*/
    // 0x03/*联机*/
    // 0x0c/*成功获取EC余额*/
    // 0x0d/*非接触QPBOC交易接受*/
    // 0x0e/*非接触QPBOC交易拒绝*/
    // 0x0f/*非接触QPBOC交易联机*/
    // 0x10/*非接触MSD交易联机*/
    // 0x11/*成功获取QPBOC余额*/
    // 0xff/*交易失败*/
    // 0xfd/*FDDA失败*/
    // 0xfe/*FALLBACK*/
    // 0xfc/*取消*/
    // 0xfb/*交易金额大于终端限额*/
    // 0xfa/*卡片不支持电子现金*/
    @Override
    public void onQpbocFinished(EmvTransInfo transInfo) {
        // qpboc交易结束
        if (transInfo.getExecuteRslt() == 0x02) {
            mainActivity.showMessage("交易失败：【交易拒绝】！" + "\r\n", MessageTag.TIP);
        } else if (transInfo.getExecuteRslt() == 0x03) {
            mainActivity.showMessage("联机：【电子现金余额不足，请发起联机交易】！" + "\r\n", MessageTag.TIP);
            // TODO 联机交易操作
        } else if (transInfo.getExecuteRslt() == 0x00 || transInfo.getExecuteRslt() == 0x01) {
            transInfo.getOnLinePin();
            // 交易成功、交易授受
            TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
            mainActivity.showMessage(">>>>55域打包集合:" + HexUtil.toHexString(tlvPackage.pack()) + "\r\n", MessageTag.DATA);
        } else {
            mainActivity.showMessage("错误的qpboc状态返回！" + transInfo.getExecuteRslt() + "\r\n", MessageTag.DATA);
        }
    }

    @Override
    public void onEmvFinished(boolean isSuccess, EmvTransInfo transInfo) {
        ((App) mainActivity.getApplication()).getTransData().setAccount(transInfo.getCardNo());
        if (isSuccess) {
            TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
            mainActivity.showMessage(">>>>55域打包集合:" + HexUtil.toHexString(tlvPackage.pack()) + "\r\n", MessageTag.DATA);
        }
        // TODO EMV联机交易发卡行返回成功而卡片拒绝要冲正
        // ((App) mainActivity.getApplication()).getTransResult().setResCode("success");
        mainActivity.getWaitThreat().notifyThread();
    }

    @Override
    public void onError(EmvTransController controller, Exception e) {
        e.printStackTrace();
        mainActivity.showMessage("emv交易失败" + "\r\n", MessageTag.ERROR);
        mainActivity.showMessage(e.getMessage() + "\r\n", MessageTag.ERROR);

        // ((App) mainActivity.getApplication()).getTransResult().setResCode("fail");
        mainActivity.getWaitThreat().notifyThread();
    }

    @Override
    public void onFallback(EmvTransInfo transInfo) throws Exception {
        mainActivity.showMessage("ic卡交易环境不满足:交易降级..." + "\r\n", MessageTag.ERROR);
        Message message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
    }

    @Override
    public void onRequestOnline(EmvTransController controller, EmvTransInfo transInfo) throws IOException, PackagingException {
        TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
        ((App) mainActivity.getApplication()).getTransData().setAccount(transInfo.getCardNo());
        ((App) mainActivity.getApplication()).getTransData().setAmount(new BigDecimal(transInfo.getAmountAuthorisedNumeric()));
        ((App) mainActivity.getApplication()).getTransData().setValidDate(transInfo.getCardExpirationDate().substring(0, 4));
        ((App) mainActivity.getApplication()).getTransData().setSequenceNumber(transInfo.getCardSequenceNumber());
        ((App) mainActivity.getApplication()).getTransData().setSecondTrackData(HexUtil.toHexString(transInfo.getTrack_2_eqv_data()).substring(0, 37));
        ((App) mainActivity.getApplication()).getTransData().setIcCardData(HexUtil.toHexString(tlvPackage.pack()));


        /** 二磁道加密
         if (null != transInfo.getTrack_2_eqv_data()) {
         mainActivity.showMessage(">>>>二磁道明文:" + HexUtil.toHexString(transInfo.getTrack_2_eqv_data()) + "\r\n", MessageTag.DATA);
         SwiperModuleImpl swiper = new SwiperModuleImpl();
         SwipResult swipResult = swiper.k21CalculateTrackData(HexUtil.toHexString(transInfo.getTrack_2_eqv_data()), null, new WorkingKey(index), SupportMSDAlgorithm.getMSDAlgorithm(encryptAlgorithm));
         mainActivity.showMessage(">>>>二磁道密文:" + (swipResult.getSecondTrackData() == null ? null : HexUtil.toHexString(swipResult.getSecondTrackData())) + "\r\n", MessageTag.DATA);
         }
         **/
        // [步骤1]：从该处transInfo中获取ic卡卡片信息后，发送银联8583交易
        IMessage message = null;
        TransData transData = ((App) mainActivity.getApplication()).getTransData();
        switch (transData.getTransType()) {
            case PAY:
                message = mainActivity.getTraner().pay(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(),
                        transData.getSecondTrackData(), transData.getThirdTrackData(),
                        transData.getPin(), transData.getIcCardData());
                break;
            case REVOKE:
                message = mainActivity.getTraner().revoke(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                break;
            case REFUND:
                message = mainActivity.getTraner().revoke(transData.getAccount(), transData.getAmount().toString(),
                        transData.getValidDate(), "901", transData.getSequenceNumber(), transData.getSecondTrackData(),
                        transData.getThirdTrackData(), transData.getPin(), transData.getOldAuthCode(),
                        transData.getOldTraceNo(), transData.getOldTransDate(), transData.getOldTransTime());
                break;
        }

        if (null != message && "00".equals(message.getFieldString(39))) {
            tlvPackage.unpack(HexUtil.parseHex(message.getFieldString(55)));
            SecondIssuanceRequest request = new SecondIssuanceRequest();
            request.setAuthorisationResponseCode(message.getFieldString(39));// 取自银联8583规范39域值,该参数按交易实际值填充
            request.setIssuerAuthenticationData(tlvPackage.getValue(0x91));//取自银联8583规范 55域0x91值,该参数按交易实际值填充
            request.setIssuerScriptTemplate1(tlvPackage.getValue(0x71));//取自银联8583规范 55域0x71值,该参数按交易实际值填充
            request.setIssuerScriptTemplate2(tlvPackage.getValue(0x72));//取自银联8583规范 55域0x72值,该参数按交易实际值填充
            request.setAuthorisationCode(message.getFieldString(38));//取自银联8583规范 38域值,该参数按交易实际值填充

            // [步骤2].ic卡联机交易成功或者非接圈存交易，调用二次授权接口，等回调onemvfinished结束流程。
            controller.secondIssuance(request);
        } else {
            // [并列步骤2].联机交易失败或者非接交易(除圈存外)调用emv结束方法，结束流程。
            controller.doEmvFinish(false);
        }

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
    public void onRequestAmountEntry(final EmvTransController controller, EmvTransInfo context) {
        // 调用输入金额界面
        Message message = mainActivity.getFragmentHandler().obtainMessage(1);
        message.obj = "amount";
        message.sendToTarget();
        mainActivity.getAmountWaitThreat().waitForRslt();
    }

    // IM81和N900会触发，ME30、ME31不会触发
    @Override
    public void onRequestPinEntry(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        // EMV调用密码键盘
        Message message = mainActivity.getFragmentHandler().obtainMessage(3);
        message.obj = "inputPin";
        message.sendToTarget();
        mainActivity.getInputPinWaitThreat().waitForRslt();
        controller.sendPinInputResult(HexUtil.parseHex(((App) mainActivity.getApplication()).getTransData().getPin()));
    }

    /**
     * 是否拦截acctType select事件
     */
    @Override
    public boolean isAccountTypeSelectInterceptor() {
        return true;
    }

    /**
     * 是否拦截持卡人证件确认事件
     */
    @Override
    public boolean isCardHolderCertConfirmInterceptor() {
        return true;
    }

    /**
     * 是否拦截电子现金确认事件
     */
    @Override
    public boolean isEcSwitchInterceptor() {
        return true;
    }

    /**
     * 是否拦截使用外部的序列号处理器
     */
    @Override
    public boolean isTransferSequenceGenerateInterceptor() {
        return true;
    }

    /**
     * 是否拦截消息显示事件
     */
    @Override
    public boolean isLCDMsgInterceptor() {
        return true;
    }

    /**
     * 账号类型选择
     * 返回int范围：1、default2、savings3、Cheque/debit4、Credit
     *
     * @return 1-4：选择范围， －1：失败
     */
    @Override
    public int accTypeSelect() {
        return 1;
    }

    /**
     * 持卡人证件确认
     *
     * @return true:确认正确， false:确认失败
     */
    @Override
    public boolean cardHolderCertConfirm(EmvCardholderCertType certType, String certno) {
        return true;
    }

    /**
     * 电子现金/emv选择
     * 交易返回： 1：继续电子现金交易 0：不进行电子现金交易 －1:用户中止 －3:超时
     */
    @Override
    public int ecSwitch() {
        return 0;
    }

    /**
     * 流水号加1并返回
     */
    @Override
    public int incTsc() {
        return 0;
    }

    /**
     * 显示信息
     *
     * @param title       标题
     * @param msg         消息
     * @param yesNoShowed 是否出现yes/no
     * @param waitingTime 等待时间
     * @return 如果yesNoShow == true, 返回1 表示确认，返回0表示取消 如果yesNoShow == false,
     * 返回值无意义
     */
    @Override
    public int lcdMsg(String title, String msg, boolean yesNoShowed, int waitingTime) {
        return 1;
    }

}
