package com.yada.smartpos.util;

public enum TransType {
    PAY("消费"),
    REVOKE("消费撤销"),
    REFUND("消费退货"),

    PRE_AUTH("预授权"),
    PRE_AUTH_REVOKE("预授权撤销"),
    PRE_AUTH_COMPLETE("预授权完成"),
    PRE_AUTH_COMPLETE_NOTICE("预授权完成通知"),
    PRE_AUTH_COMPLETE_REVOKE("预授权完成撤销"),

    INSTALLMENT_PAY("分期消费"),
    INSTALLMENT_REVOKE("分期撤销"),
    INSTALLMENT_REFUND("分期退货"),

    EC_CONSUMPTION("电子现金消费"),
    QUERY("余额查询");

    public String transType;

    TransType(String transType) {
        this.transType = transType;
    }
}
