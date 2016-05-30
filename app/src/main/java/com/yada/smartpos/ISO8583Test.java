package com.yada.smartpos;

import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.TLVUtils;

public class ISO8583Test {

    public static void main(String[] args) {
        String str = "5257461195912988=24101010000087900000";
        System.out.println(ISOUtils.hexString(ISOUtils.str2bcd(str, true)));

        String filed48 = "9851176FDFBA82270E437A9ECA8CB1FD2E40C022C89EF977E2F92B19951141929402D9C2B4E7ACE4CB2FAC3C9F5F01E3319A935BDCC5AD";
        for(String s: TLVUtils.getTLVList(filed48)){
            System.out.println(s);
        }
    }
}
