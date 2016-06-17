package com.yada.smartpos.model;

import com.newland.mtype.module.common.cardreader.CommonCardType;
import com.yada.smartpos.util.TransType;

import java.math.BigDecimal;

public class TransData {

    private TransType transType;// 交易类型
    private CommonCardType cardType;// 卡类型
    private String account;// 主帐号
    private BigDecimal amount;// 交易金额
    private String validDate;// 卡有效期
    private String sequenceNumber;// 卡片序列号
    private String secondTrackData;// 二磁道数据
    private String thirdTrackData;// 三磁道数据
    private String pin;// 个人识别码
    private String icCardData;// IC卡交易数据域
    private String oldProofNo;// 原凭证号
    private String oldAuthCode;// 原消费交易授权号
    private String oldTraceNo;// 原系统跟踪号
    private String oldTransDate;// 原交易日期
    private String oldTransTime;// 原交易日期
    private String referenceNumber;// 参考号
    private String installmentNumber;// 分期期数
    private String installmentPlanId;// 分期计划ID

    public TransType getTransType() {
        return transType;
    }

    public void setTransType(TransType transType) {
        this.transType = transType;
    }

    public CommonCardType getCardType() {
        return cardType;
    }

    public void setCardType(CommonCardType cardType) {
        this.cardType = cardType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getValidDate() {
        return validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSecondTrackData() {
        return secondTrackData;
    }

    public void setSecondTrackData(String secondTrackData) {
        this.secondTrackData = secondTrackData;
    }

    public String getThirdTrackData() {
        return thirdTrackData;
    }

    public void setThirdTrackData(String thirdTrackData) {
        this.thirdTrackData = thirdTrackData;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getIcCardData() {
        return icCardData;
    }

    public void setIcCardData(String icCardData) {
        this.icCardData = icCardData;
    }

    public String getOldProofNo() {
        return oldProofNo;
    }

    public void setOldProofNo(String oldProofNo) {
        this.oldProofNo = oldProofNo;
    }

    public String getOldAuthCode() {
        return oldAuthCode;
    }

    public void setOldAuthCode(String oldAuthCode) {
        this.oldAuthCode = oldAuthCode;
    }

    public String getOldTraceNo() {
        return oldTraceNo;
    }

    public void setOldTraceNo(String oldTraceNo) {
        this.oldTraceNo = oldTraceNo;
    }

    public String getOldTransDate() {
        return oldTransDate;
    }

    public void setOldTransDate(String oldTransDate) {
        this.oldTransDate = oldTransDate;
    }

    public String getOldTransTime() {
        return oldTransTime;
    }

    public void setOldTransTime(String oldTransTime) {
        this.oldTransTime = oldTransTime;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(String installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public String getInstallmentPlanId() {
        return installmentPlanId;
    }

    public void setInstallmentPlanId(String installmentPlanId) {
        this.installmentPlanId = installmentPlanId;
    }
}
