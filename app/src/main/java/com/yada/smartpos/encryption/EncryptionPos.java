package com.yada.smartpos.encryption;

import com.newland.mtype.module.common.pin.*;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.device.encryption.IEncryption;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.module.impl.PinInputModuleImpl;
import com.yada.smartpos.util.Const;

import java.nio.ByteBuffer;

public class EncryptionPos implements IEncryption {

    private PinInputModule pinInputModule;

    public EncryptionPos() {
        pinInputModule = new PinInputModuleImpl();
    }

    /**
     * 装载主密钥
     *
     * @param zmkTmk 加密终端主密钥
     * @return 密码键盘存储主密钥
     */
    @Override
    public String getLmkTmk(String zmkTmk) {
        /*
        kekUsingType：主密钥的灌装方式。常用的灌装方式如：ENCRYPT_TMK - USE ENCRYPT TMK，
        MAIN_KEY - 使用主密钥加密主密钥，PRIVATE_KEY-主密钥采用发行方私钥加密，TR31_BLOCK - USE TR31 block。
        mainIndex：待设置主密钥索引，取值范围1-255 。
        data：待装载的主密钥数据
        checkValue:主密钥校验值
        kekIndex:传输密钥索引（该字段只在kekUsingType 为MAIN_KEY 时设置，其他情况下置为-1
        即可）
         */
        pinInputModule.loadMainKey(KekUsingType.MAIN_KEY, Const.MKIndexConst.DEFAULT_MK_INDEX,
                HexUtil.parseHex(zmkTmk), null, 2);
        return null;
    }

    /**
     * 装载MAC工作密钥
     *
     * @param lmkTmk 终端主密钥的索引
     * @param tmkTak 终端MAC密钥
     * @return 密码键盘存储主密钥MAC密钥
     */
    @Override
    public String getLmkTak(String lmkTmk, String tmkTak) {
        /*
        type：工作密钥类型。常用密钥类型：WorkingKeyType.MAC-MAC密钥。
        mainKeyIndex：已装载的主密钥索引。
        workingKeyIndex：待设置的工作密钥索引，取值范围1-255。
        data：待装载的主工作密钥数据。
        checkValue:校验值
         */
        pinInputModule.loadWorkingKey(WorkingKeyType.MAC, Const.MKIndexConst.DEFAULT_MK_INDEX,
                Const.MacWKIndexConst.DEFAULT_MAC_WK_INDEX, HexUtil.parseHex(tmkTak), null);
        return null;
    }

    /**
     * 装载PIN工作密钥
     *
     * @param lmkTmk 终端主密钥的索引
     * @param tmkTpk 终端PIN密钥
     * @return 密码键盘存储主密钥PIN密钥
     */
    @Override
    public String getLmkTpk(String lmkTmk, String tmkTpk) {
        /*
        type：工作密钥类型。常用密钥类型：WorkingKeyType.PININPUT-PIN密钥。
        mainKeyIndex：已装载的主密钥索引。
        workingKeyIndex：待设置的工作密钥索引，取值范围1-255。
        data：待装载的主工作密钥数据。
        checkValue:校验值
         */
        pinInputModule.loadWorkingKey(WorkingKeyType.PININPUT, Const.MKIndexConst.DEFAULT_MK_INDEX,
                Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX, HexUtil.parseHex(tmkTpk), null);
        return null;
    }

    /**
     * PIN已在密码键盘中加密
     *
     * @return 直接返回PIN
     */
    @Override
    public String getTpkPin(String accountNo, String pin, String lmkTpk) {
        return pin;
    }

    @Override
    public ByteBuffer getTakMac(ByteBuffer macData, String lmkTak) {
        MacResult macResult = pinInputModule.calcMac(MacAlgorithm.MAC_X99, KeyManageType.MKSK,
                new WorkingKey(Const.MacWKIndexConst.DEFAULT_MAC_WK_INDEX), macData.array());
        return ByteBuffer.wrap(macResult.getMac());
    }

    @Override
    public String getLmkPinFromZpkPin(String hsmZpkLmk, String classifiedPin, String cardNo) {
        return null;
    }

    @Override
    public String DecryptPin(String classifiedPin, String cardNo) {
        return null;
    }

    @Override
    public String GetLmkZpk(String hsmKeyLmk) {
        return null;
    }

    @Override
    public String GetLmkZak(String hsmKeyLmk) {
        return null;
    }

    @Override
    public String GetLmkZek(String hsmKeyLmk) {
        return null;
    }

    @Override
    public String getZakMac(String lmkZak, String macData) {
        return null;
    }
}
