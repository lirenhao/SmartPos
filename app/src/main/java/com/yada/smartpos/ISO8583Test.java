package com.yada.smartpos;

import com.newland.mtype.util.ISOUtils;
import com.payneteasy.tlv.HexUtil;

import java.io.UnsupportedEncodingException;

public class ISO8583Test {

    public static void main(String[] args) throws UnsupportedEncodingException {
        String str = "5257461195912988=24101010000087900000";
        System.out.println(HexUtil.toHexString(ISOUtils.str2bcd(str, false)));

        String s = "测试交易";
        byte[] bytes = s.getBytes("GBK");
        System.out.println(HexUtil.toHexString(bytes));

        String zero = "0000000000000";
        System.out.println(zero.matches("0+"));
    }
}
