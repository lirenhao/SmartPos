package com.yada.smartpos.spos;

import com.yada.sdk.packages.transaction.IMessage;

public class CheckSignIn {

    private VirtualPos vp;

    public CheckSignIn(VirtualPos vp) {
        this.vp = vp;
    }

    void checkMessage(IMessage responseMessage) {
        String processCode = responseMessage.getFieldString(3);
        char c = processCode.charAt(5);
        String respCode = responseMessage.getFieldString(39);

        switch (c) {
            case '1':
                vp.resetParamDownload();
                break;
            case '4':
            case '5':
            case '6':
                vp.resetSingIn();
                break;
        }

        switch (respCode) {
            case "Z1":
            case "88":
            case "89":
                vp.resetSingIn();
                break;
        }
    }
}
