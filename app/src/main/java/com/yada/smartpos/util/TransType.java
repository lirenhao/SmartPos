package com.yada.smartpos.util;

public enum TransType {
    PAY,
    REVOKE,
    REFUND,

    PRE_AUTH,
    PRE_AUTH_REVOKE,
    PRE_AUTH_COMPLETE,
    PRE_AUTH_COMPLETE_NOTICE,
    PRE_AUTH_COMPLETE_REVOKE,

    INSTALLMENT_PAY,
    INSTALLMENT_REVOKE,
    INSTALLMENT_REFUND,
}