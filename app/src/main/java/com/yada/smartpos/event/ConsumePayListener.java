package com.yada.smartpos.event;

import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.level2.EmvCardholderCertType;
import com.newland.mtype.module.common.emv.level2.EmvLevel2ControllerExtListener;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.handler.ResultHandler;
import com.yada.smartpos.model.TransData;

import java.math.BigDecimal;

/**
 * Emv流程控制监听
 */
public class ConsumePayListener implements EmvLevel2ControllerExtListener {

    private TransHandleListener handleListener;
    private MainActivity mainActivity;
    private IMessage respMessage;

    public ConsumePayListener(MainActivity mainActivity, TransHandleListener handleListener) {
        this.handleListener = handleListener;
        this.mainActivity = mainActivity;
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
    public void onRequestAmountEntry(EmvTransController controller, EmvTransInfo transInfo) {
        // 调用输入金额界面
        handleListener.amountView();
        BigDecimal amount = ((App) mainActivity.getApplication()).getTransData().getAmount();
        controller.sendAmtInputResult(amount.movePointLeft(2));
    }

    @Override
    public void onRequestPinEntry(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        ((App) mainActivity.getApplication()).getTransData().setAccount(transInfo.getCardNo());
        // EMV调用密码键盘
        handleListener.inputPinView();
        controller.sendPinInputResult(HexUtil.parseHex(((App) mainActivity.getApplication()).getTransData().getPin()));
    }

    @Override
    public void onRequestOnline(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        handleListener.emvDataHandle(transInfo);

        handleListener.loadingView();
        TransData transData = ((App) mainActivity.getApplication()).getTransData();
        respMessage = mainActivity.getVirtualPos().createTraner().pay(transData.getAccount(), transData.getAmount().toString(),
                transData.getValidDate(), "051", transData.getSequenceNumber(),
                transData.getSecondTrackData(), transData.getThirdTrackData(),
                transData.getPin(), transData.getIcCardData());

        handleListener.emvResultHandle(controller, transInfo, respMessage);
    }

    @Override
    public void onEmvFinished(boolean isSuccess, EmvTransInfo transInfo) throws Exception {
        ResultHandler.result(mainActivity, respMessage);
        if (isSuccess) {
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("1");
        } else {
            ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
        }
        mainActivity.getWaitThreat().notifyThread();
    }

    @Override
    public void onFallback(EmvTransInfo emvTransInfo) throws Exception {
        handleListener.emvFallbackHandle();
    }

    @Override
    public void onError(EmvTransController emvTransController, Exception e) {
        ((App) mainActivity.getApplication()).getTransResult().setTransCode("0");
        ((App) mainActivity.getApplication()).getTransResult().setTransMsg(e.getMessage());
        mainActivity.getWaitThreat().notifyThread();
    }

    /**
     * 是否拦截acctType select事件
     */
    @Override
    public boolean isAccountTypeSelectInterceptor() {
        return false;
    }

    /**
     * 是否拦截持卡人证件确认事件
     */
    @Override
    public boolean isCardHolderCertConfirmInterceptor() {
        return false;
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
        return false;
    }

    /**
     * 是否拦截消息显示事件
     */
    @Override
    public boolean isLCDMsgInterceptor() {
        return false;
    }

    /**
     * 账号类型选择
     *
     * @return －1:失败 1:default 2:savings 3:Cheque/debit 4:Credit
     */
    @Override
    public int accTypeSelect() {
        return 0;
    }

    /**
     * 持卡人证件确认
     *
     * @param certType 持卡人证件类型
     * @param certNo   证件号码
     * @return true:确认正确，false:确认失败
     */
    @Override
    public boolean cardHolderCertConfirm(EmvCardholderCertType certType, String certNo) {
        return false;
    }

    /**
     * 电子现金/emv选择
     *
     * @return 1：继续电子现金交易 0：不进行电子现金交易 －1:用户中止 －3:超时
     */
    @Override
    public int ecSwitch() {
        return 0;
    }

    /**
     * 流水号加1并返回
     *
     * @return 下一个流水号
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
     * @param yesNoShowed 是否出现
     * @param waitingTime 等待时间
     * @return yesNoShowed=true,返回1表示确认,返回0表示取消yesNoShowed=false,返回值无意义
     */
    @Override
    public int lcdMsg(String title, String msg, boolean yesNoShowed, int waitingTime) {
        return 0;
    }
}
