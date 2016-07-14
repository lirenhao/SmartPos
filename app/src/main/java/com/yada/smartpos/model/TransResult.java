package com.yada.smartpos.model;

import com.yada.sdk.packages.transaction.IMessage;

public class TransResult {

    private String transCode;
    private String transMsg;
    private String transResp;
    private IMessage messageResp;
    private String resultText;

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getTransMsg() {
        return transMsg;
    }

    public void setTransMsg(String transMsg) {
        this.transMsg = transMsg;
    }

    public String getTransResp() {
        return transResp;
    }

    public void setTransResp(String transResp) {
        this.transResp = transResp;
    }

    public IMessage getMessageResp() {
        return messageResp;
    }

    public void setMessageResp(IMessage messageResp) {
        this.messageResp = messageResp;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
}