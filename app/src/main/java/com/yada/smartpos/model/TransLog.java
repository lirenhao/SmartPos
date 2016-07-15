package com.yada.smartpos.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "transLog")
public class TransLog {

    // 凭证号(流水号\参考号)
    @Column(name = "traceNo", isId = true)
    private String traceNo;

    // 交易类型
    @Column(name = "transType")
    private String transType;

    // 刷卡类型
    @Column(name = "cardType")
    private String cardType;

    // 卡号
    @Column(name = "account")
    private String account;

    // 授权号
    @Column(name = "authCode")
    private String authCode;

    // 交易时间
    @Column(name = "transTime")
    private String transTime;

    // 交易日期
    @Column(name = "transDate")
    private String transDate;

    // 金额
    @Column(name = "amount")
    private String amount;

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
