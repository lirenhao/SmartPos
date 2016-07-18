package com.yada.smartpos;

import com.payneteasy.tlv.HexUtil;
import com.yada.smartpos.encryption.AnalogEncryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionTest {

    public static void main(String[] args) throws Exception {
        String zmk = "780C44692D6C27B45CBD77A3639F1B95";
        String tmk = "620D2892F9189CA30BDA03DADB6B1B88";

        String key = "780C44692D6C27B45CBD77A3639F1B95780C44692D6C27B4";
        // 算法名称
        SecretKey secretKey = new SecretKeySpec(HexUtil.parseHex(key), "DESede");
        // 算法名称/加密模式/填充方式
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        System.out.println(HexUtil.toHexString(cipher.doFinal(HexUtil.parseHex(tmk))));

        System.out.println(AnalogEncryption.analogDecryptWorkingKey(zmk, tmk));

        System.out.println(AnalogEncryption.analogMacBy3Des("04AC2892F72CBA3BCC8451A484DAC37E", "6253371162874883000008000000000001000497015611000897"));

        System.out.println(AnalogEncryption.getCheckValueOfKey("BF3D5845D337575D5D0297EC012A2FB0"));
    }
}
