package com.yada.smartpos.encryption;

import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.device.encryption.IEncryption;

import java.nio.ByteBuffer;

public class Encryption implements IEncryption {

    private String tmkZmk;

    public Encryption(String tmkZmk) {
        this.tmkZmk = tmkZmk;
    }

    /**
     * 装载主密钥
     *
     * @param zmkTmk 加密终端主密钥
     * @return 密码键盘存储主密钥
     */
    @Override
    public String getLmkTmk(String zmkTmk) {
        return AnalogEncryption.analogDecryptWorkingKey(tmkZmk, zmkTmk);
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
        return AnalogEncryption.analogDecryptWorkingKey(lmkTmk, tmkTak);
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
        return AnalogEncryption.analogDecryptWorkingKey(lmkTmk, tmkTpk);
    }

    /***
     * 获取受终端主密钥（TPK）保护的PIN
     *
     * @param accountNo 账号
     * @param pin       PIN
     * @return 受终端主密钥（TPK）保护的PIN
     */
    @Override
    public String getTpkPin(String accountNo, String pin, String lmkTpk) {
        return AnalogEncryption.analogPinEncrypt(lmkTpk, accountNo, pin);
    }

    /***
     * 获取MAC
     *
     * @param macData mac验证的数据
     * @param lmkTak  受本地主密钥（LMK）保护的终端认证密钥（TAK）
     * @return MAC
     */
    @Override
    public ByteBuffer getTakMac(ByteBuffer macData, String lmkTak) {
        return ByteBuffer.wrap(HexUtil.parseHex(AnalogEncryption.analogMacBy3Des(lmkTak, HexUtil.toHexString(macData.array()))));
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
