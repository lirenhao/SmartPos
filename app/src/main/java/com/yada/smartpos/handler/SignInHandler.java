package com.yada.smartpos.handler;

import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.BytesUtils;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.module.impl.PinInputModuleImpl;
import com.yada.smartpos.util.Client;
import com.yada.smartpos.util.Const;
import com.yada.smartpos.util.SharedPreferencesUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

public class SignInHandler {

    private MainActivity mainActivity;

    public SignInHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void signIn() {
        // TODO 签到前先冲正
        Set<String> set = SharedPreferencesUtil.getReverseParam(mainActivity);
        for(String reverse: set){
            Client.sendReverse(mainActivity, mainActivity.getClient(), reverse);
        }
        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0800");
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        iso8583.setField(24, "009");// NII
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(61, "000008001");// 自定义域 交易批次号+网管消息类型
        TcpClient client = mainActivity.getClient();
        try {
            ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes("60001200001306" + iso8583.pack()));
            System.out.println("签到8583包：60001200001306" + iso8583.pack());
            client.open();
            ByteBuffer respBuffer = client.send(reqBuffer);
            String unpack = BytesUtils.bytesToHex(respBuffer.array());
            System.out.println(unpack.substring(14));
            iso8583.initPack();
            iso8583.unpack(unpack.substring(14));

            loadWorkingKey(iso8583.getField(48));
        } catch (IOException | ISO8583Exception e) {
            mainActivity.showMessage("签到异常异常：" + e.getMessage(), Const.MessageTag.ERROR);
        } finally {
            client.close();
        }
    }

    public void paramDownload() {
        // TODO 参数下载
    }

    private void loadWorkingKey(String field48) {
        String tpk = field48.substring(5, 37);
        PinInputModule pinInput = new PinInputModuleImpl();
        byte[] kcv = pinInput.loadWorkingKey(WorkingKeyType.PININPUT, 2,
                Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX, ISOUtils.hex2byte(tpk), null);
        System.out.println(field48.substring(39, 55));
        System.out.println(BytesUtils.bytesToHex(kcv));
    }
}
