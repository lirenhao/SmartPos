package com.yada.smartpos.activity;

import android.app.Application;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.tlv.TLVPackage;

import java.math.BigDecimal;

public class App extends Application {

    private boolean isDukpt = false;

    private SwipResult swipResult = null; // 刷卡结果

    private EmvTransInfo emvTransInfo = null; // EMV结果

    private BigDecimal amt; // 金额

    private String cardNo; // 卡号

    private byte[] pin; // 密码

    private TLVPackage tlvPackage;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO 初始化设备、装载主密钥
    }

    public boolean isDukpt() {
        return isDukpt;
    }

    public void setDukpt(boolean dukpt) {
        isDukpt = dukpt;
    }

    public SwipResult getSwipResult() {
        return swipResult;
    }

    public void setSwipResult(SwipResult swipResult) {
        this.swipResult = swipResult;
    }

    public EmvTransInfo getEmvTransInfo() {
        return emvTransInfo;
    }

    public void setEmvTransInfo(EmvTransInfo emvTransInfo) {
        this.emvTransInfo = emvTransInfo;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public byte[] getPin() {
        return pin;
    }

    public void setPin(byte[] pin) {
        this.pin = pin;
    }

    public TLVPackage getTlvPackage() {
        return tlvPackage;
    }

    public void setTlvPackage(TLVPackage tlvPackage) {
        this.tlvPackage = tlvPackage;
    }
}
