package com.yada.smartpos.util;

import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.model.TransData;

public class PackMessage {

    public static String reverse(ISO8583 iso8583) throws ISO8583Exception {
        iso8583.setField(62, iso8583.getField(0) + iso8583.getField(11) + "0000000000");
        iso8583.setField(0, "0400");
        iso8583.setField(11, "000008");// 系统跟踪号应该与被冲正的交易不同
        return iso8583.pack();
    }

    public static ISO8583 pay(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 revoke(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "200008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        iso8583.setField(22, "901");// POS输入方式
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 原消费交易授权号
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        iso8583.setField(62, "0200" + transData.getOldTraceNumber()
                + transData.getOldTransDate() + transData.getOldTransTime());// 自定义域 信息类型码+系统跟踪号+交易日期和时间
        return iso8583;
    }

    public static ISO8583 refund(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0220");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "270008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        iso8583.setField(22, "901");// POS输入方式
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 原消费交易授权号
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        iso8583.setField(62, "0200000008");// 自定义域 信息类型码+系统跟踪号+交易日期和时间
        return iso8583;
    }

    public static ISO8583 query(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "310008");// 处理码
        if (transData.getAmount() != null)
            iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 preAuth(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0100");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "030008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 preAuthRevoke(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0100");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "200008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        iso8583.setField(22, "901");// POS输入方式
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 原消费交易授权号
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        iso8583.setField(62, "0100" + transData.getOldTraceNumber()
                + transData.getOldTransDate() + transData.getOldTransTime());// 自定义域 信息类型码+系统跟踪号+交易日期和时间
        return iso8583;
    }

    public static ISO8583 preAuthComplete(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 授权码
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 preAuthCompleteNotice(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0220");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        iso8583.setField(12, transData.getOldTransDate());// 本地交易时间
        iso8583.setField(13, transData.getOldTransDate());// 本地交易日期
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(37, transData.getReferenceNumber());// 参考号
        iso8583.setField(38, transData.getOldAuthCode());// 授权码
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 preAuthCompleteRevoke(MainActivity mainActivity, TransData transData) {
        return revoke(mainActivity, transData);
    }

    public static ISO8583 installmentPay(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        switch (transData.getCardType()) {
            case MSCARD:
                iso8583.setField(22, "901");// POS输入方式
                break;
            default:
                iso8583.setField(22, "051");// POS输入方式
                break;
        }
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        // 使用90子域，值为”905”；还需要使用91子域以上送期数与PLAN ID
        iso8583.setField(48, "90039059106" + transData.getInstallmentPlanId() + transData.getInstallmentNumber());
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        return iso8583;
    }

    public static ISO8583 installmentRevoke(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "200008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        iso8583.setField(22, "901");// POS输入方式
        if (transData.getSequenceNumber() != null)
            iso8583.setField(23, transData.getSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 原消费交易授权号
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        // 使用90子域，值为”905”；还需要使用91子域以上送期数与PLAN ID
        iso8583.setField(48, "90039059106" + transData.getInstallmentPlanId() + transData.getInstallmentNumber());
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        iso8583.setField(62, "0200" + transData.getOldTraceNumber()
                + transData.getOldTransDate() + transData.getOldTransTime());// 自定义域 信息类型码+系统跟踪号+交易日期和时间
        return iso8583;
    }

    public static ISO8583 installmentRefund(MainActivity mainActivity, TransData transData) {
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0220");
        iso8583.setField(2, transData.getAccount());// 主帐号
        iso8583.setField(3, "270008");// 处理码
        iso8583.setField(4, transData.getAmount().toString());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        if (transData.getValidDate() != null)
            iso8583.setField(14, transData.getValidDate());// 卡有效期
        iso8583.setField(22, "901");// POS输入方式
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (transData.getSecondTrackData() != null)
            iso8583.setField(35, transData.getSecondTrackData());// 二磁道数据
        if (transData.getThirdTrackData() != null)
            iso8583.setField(36, transData.getThirdTrackData());// 三磁道数据
        iso8583.setField(38, transData.getOldAuthCode());// 原消费交易授权号
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        // 使用90子域，值为”905”；还需要使用91子域以上送期数与PLAN ID
        iso8583.setField(48, "90039059106" + transData.getInstallmentPlanId() + transData.getInstallmentNumber());
        iso8583.setField(49, "156");// 货币代码
        if (transData.getPin() != null)
            iso8583.setField(52, transData.getPin());// 个人识别码
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
        iso8583.setField(62, "0200000008");// 自定义域 信息类型码+系统跟踪号+交易日期和时间
        return iso8583;
    }
}