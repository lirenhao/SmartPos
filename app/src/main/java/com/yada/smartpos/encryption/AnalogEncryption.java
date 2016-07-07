package com.yada.smartpos.encryption;


import com.payneteasy.tlv.HexUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * 模拟加密机
 *
 * @author ZhangYaMin
 */
public class AnalogEncryption {

    //初始向量
    private static String ivInfo = "0000000000000000";

    /**
     * 加密函数(ECB)
     *
     * @param data 加密数据
     * @param key  密钥
     * @return 返回加密后的数据
     */
    private static byte[] ecbEncrypt(byte[] data, byte[] key) {

        try {

            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();

            // 从原始密钥数据创建DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);

            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);

            // DES的ECB模式
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

            // 用密钥初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);

            // 执行加密操作
            byte encryptedData[] = cipher.doFinal(data);

            return encryptedData;
        } catch (Exception e) {
            throw new RuntimeException("ECB-DES算法，加密数据出错!");
        }

    }

    /**
     * 解密函数(ECB)
     *
     * @param data 解密数据
     * @param key  密钥
     * @return 返回解密后的数据
     */
    private static byte[] ecbDecrypt(byte[] data, byte[] key) {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();

            // byte rawKeyData[] = /* 用某种方法获取原始密匙数据 */;

            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);

            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成 一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);

            // DES的ECB模式
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

            // 用密钥初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);

            // 正式执行解密操作
            byte decryptedData[] = cipher.doFinal(data);

            return decryptedData;
        } catch (Exception e) {
            throw new RuntimeException("ECB-DES算法，解密出错。");
        }

    }

    /**
     * 加密函数(CBC)
     *
     * @param data 加密数据
     * @param key  密钥
     * @return 返回加密后的数据
     */
    private static byte[] cbcEncrypt(byte[] data, byte[] key, byte[] iv) {

        try {
            // 从原始密钥数据创建DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);

            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);

            // DES的CBC模式,采用NoPadding模式，data长度必须是8的倍数
            Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

            // 用密钥初始化Cipher对象
            IvParameterSpec param = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, param);

            // 执行加密操作
            byte encryptedData[] = cipher.doFinal(data);

            return encryptedData;
        } catch (Exception e) {
            throw new RuntimeException("CBC-DES算法，加密数据出错!");
        }
    }

    /**
     * 解密函数(CBC)
     *
     * @param data 解密数据
     * @param key  密钥
     * @return 返回解密后的数据
     */
    @SuppressWarnings("unused")
    private static byte[] cbcDecrypt(byte[] data, byte[] key, byte[] iv) {
        try {
            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);

            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);

            // DES的CBC模式,采用NoPadding模式，data长度必须是8的倍数
            Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

            // 用密钥初始化Cipher对象
            IvParameterSpec param = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, param);

            // 正式执行解密操作
            byte decryptedData[] = cipher.doFinal(data);

            return decryptedData;
        } catch (Exception e) {
            throw new RuntimeException("CBC-DES算法，解密出错。");
        }

    }

    /**
     * 将工作密钥解为明文
     *
     * @param plaintextZmk         解workKey的Zmk(明文)
     * @param ciphertextWorkingKey workKey密文
     * @return 返回workKey明文
     **/
    public static String analogDecryptWorkingKey(String plaintextZmk, String ciphertextWorkingKey) {
        byte[] inPlaintextZmk = HexUtil.parseHex(plaintextZmk);
        byte[] inCiphertextWorkingKey = HexUtil.parseHex(ciphertextWorkingKey);

        byte[] leftPlaintextZmk = Arrays.copyOfRange(inPlaintextZmk, 0, 8);
        byte[] rightPlaintextZmk = Arrays.copyOfRange(inPlaintextZmk, 8, 16);

        byte[] leftCiphertextWorkingKey = Arrays.copyOfRange(inCiphertextWorkingKey, 0, 8);
        byte[] rightCiphertextWorkingKey = Arrays.copyOfRange(inCiphertextWorkingKey, 8, 16);

        // 一、WorkingKey左部分解密 (反3DES)
        /* 1、ECB-DES解密  left key */
        byte[] ecbDecryptReturn = ecbDecrypt(leftCiphertextWorkingKey, leftPlaintextZmk);
		/* 2、 ECB-DES加密  right key */
        byte[] ecbEncryptReturn = ecbEncrypt(ecbDecryptReturn, rightPlaintextZmk);
		/* 3、 ECB-DES解密  left key */
        byte[] leftPlaintextWorkingKey = ecbDecrypt(ecbEncryptReturn, leftPlaintextZmk);

        // 一、WorkingKey右部分解密 (反3DES)
		/* 1、ECB-DES解密  left key */
        ecbDecryptReturn = ecbDecrypt(rightCiphertextWorkingKey, leftPlaintextZmk);
		/* 2、 ECB-DES加密  right key */
        ecbEncryptReturn = ecbEncrypt(ecbDecryptReturn, rightPlaintextZmk);
		/* 3、 ECB-DES解密  left key */
        byte[] rightPlaintextWorkingKey = ecbDecrypt(ecbEncryptReturn, leftPlaintextZmk);

        byte[] plaintextWorkingKey = ArrayUtils.addAll(leftPlaintextWorkingKey, rightPlaintextWorkingKey);

        return HexUtil.toHexString(plaintextWorkingKey);
    }

    /**
     * 计算密钥校验值
     *
     * @param plaintextKey 明文KEY
     * @return 返回校验值
     **/
    public static String getCheckValueOfKey(String plaintextKey) {
        byte[] desData = HexUtil.parseHex("0000000000000000");
        byte[] leftPlaintextKey = HexUtil.parseHex(plaintextKey.substring(0, 16));
        byte[] rightPlaintextKey = HexUtil.parseHex(plaintextKey.substring(16, 32));
		/* 1、ECB-DES加密  left key */
        byte[] ecbEncryptReturn = ecbEncrypt(desData, leftPlaintextKey);

		/* 2、 ECB-DES解密  right key */
        byte[] ecbDecryptReturn = ecbDecrypt(ecbEncryptReturn, rightPlaintextKey);

		/* 3、 ECB-DES加密  left key */
        ecbEncryptReturn = ecbEncrypt(ecbDecryptReturn, leftPlaintextKey);

        return HexUtil.toHexString(Arrays.copyOfRange(ecbEncryptReturn, 0, 4));
    }

    /**
     * 3DES算法计算MAC
     *
     * @param macKey  计算mac的Key(明文)
     * @param macData 计算mac的数据域
     * @return 返回mac数据
     **/
    public static String analogMacBy3Des(String macKey, String macData) {
        byte[] inMacKey = HexUtil.parseHex(macKey);
        byte[] inMacData = HexUtil.parseHex(macData);

        if (inMacData.length % 8 != 0) {
            int iFillLen = 8 - inMacData.length % 8;
            byte[] bFillData = new byte[iFillLen];
            for (int i = 0; i < iFillLen; i++) {
                bFillData[i] = 0x00;
            }
            inMacData = ArrayUtils.addAll(inMacData, bFillData);
        }
        //初始变量
        byte[] ivData = HexUtil.parseHex(ivInfo);
        //left key
        byte[] lKey = Arrays.copyOfRange(inMacKey, 0, 8);
        //right key
        byte[] rKey = Arrays.copyOfRange(inMacKey, 8, 16);

		/* 1、CBC-DES加密  left key */
        byte[] cbcEncryptReturn = cbcEncrypt(inMacData, lKey, ivData);
//		System.out.println(Utils.encodeHexString(cbcEncryptReturn));
        byte[] ecbDecryptData = Arrays.copyOfRange(cbcEncryptReturn, cbcEncryptReturn.length - 8, cbcEncryptReturn.length);
//		System.out.println(Utils.encodeHexString(ecbDecryptData));

		/* 2、 ECB-DES解密  right key */
        byte[] ecbDecryptReturn = ecbDecrypt(ecbDecryptData, rKey);
//		System.out.println(Utils.encodeHexString(ecbDecryptReturn));

		/* 3、 ECB-DES加密  left key */
        byte[] ecbEncryptReturn = ecbEncrypt(ecbDecryptReturn, lKey);

        return HexUtil.toHexString(ecbEncryptReturn);
    }

    public static String macBy3Des(String macKey, String macData) throws Exception {
        byte[] inMacKey = HexUtil.parseHex(macKey);
        byte[] inMacData = HexUtil.parseHex(macData);

        if (inMacData.length % 8 != 0) {
            int iFillLen = 8 - inMacData.length % 8;
            byte[] bFillData = new byte[iFillLen];
            for (int i = 0; i < iFillLen; i++) {
                bFillData[i] = 0x00;
            }
            inMacData = ArrayUtils.addAll(inMacData, bFillData);
        }
        //left key
        byte[] lKey = HexUtil.parseHex(macKey.substring(0, 8));
        //right key
        byte[] rKey = Arrays.copyOfRange(inMacKey, 8, 16);

        SecretKey key1 = new SecretKeySpec(HexUtil.parseHex(macKey.substring(0, 16)), "DES");
        SecretKey key2 = new SecretKeySpec(HexUtil.parseHex(macKey.substring(0, 16)), "DES");
        SecretKey key3 = new SecretKeySpec(HexUtil.parseHex(macKey+macKey.substring(0, 16)), "DESede");
        Cipher cipher1 = Cipher.getInstance("DES/CBC/NoPadding");
        Cipher cipher2 = Cipher.getInstance("DES/ECB/NoPadding");
        Cipher cipher3 = Cipher.getInstance("DESede/ECB/NoPadding");

        IvParameterSpec param = new IvParameterSpec(HexUtil.parseHex(ivInfo));
        cipher1.init(Cipher.ENCRYPT_MODE, key1, param);
        byte[] cbcEncryptReturn = cipher1.doFinal(inMacData);
        cipher2.init(Cipher.DECRYPT_MODE, key2);
        byte[] ecbDecryptReturn = cipher2.doFinal(Arrays.copyOfRange(cbcEncryptReturn, cbcEncryptReturn.length - 8, cbcEncryptReturn.length));
        cipher3.init(Cipher.ENCRYPT_MODE, key3);
        byte[] desEncryptReturn = cipher3.doFinal(ecbDecryptReturn);
        return HexUtil.toHexString(desEncryptReturn);
    }

    /**
     * PIN加密
     *
     * @param pinKey  计算pin的Key
     * @param cardNo  卡号/主账号
     * @param pinData 明文PIN
     * @return 返回pin密文
     **/
    public static String analogPinEncrypt(String pinKey, String cardNo, String pinData) {
        byte[] inPinKey = HexUtil.parseHex(pinKey);
        //left key
        byte[] lKey = Arrays.copyOfRange(inPinKey, 0, 8);
        //right key
        byte[] rKey = Arrays.copyOfRange(inPinKey, 8, 16);

		/* xorPin 处理 */
        byte[] xorPin = HexUtil.parseHex(String.format("%02d%-14s", pinData.length(), pinData).replace(' ', 'F'));
		/* xorPan 处理*/
        byte[] xorPan = HexUtil.parseHex(String.format("0000%12s", cardNo.substring(cardNo.length() - 13, cardNo.length() - 1)));
		/* xorData 处理*/
        byte[] xorData = new byte[8];
        for (int i = 0; i < 8; i++) {
            xorData[i] = (byte) (xorPin[i] ^ xorPan[i]);
        }

		/* 1、ECB-DES加密  left key */
        byte[] ecbEncryptReturn = ecbEncrypt(xorData, lKey);
//		System.out.println(Utils.encodeHexString(xorData) + " " + Utils.encodeHexString(lKey));

		/* 2、 ECB-DES解密  right key */
        byte[] ecbDecryptReturn = ecbDecrypt(ecbEncryptReturn, rKey);
//		System.out.println(Utils.encodeHexString(ecbEncryptReturn) + " " + Utils.encodeHexString(rKey));

		/* 3、 ECB-DES加密  left key */
        ecbEncryptReturn = ecbEncrypt(ecbDecryptReturn, lKey);
//		System.out.println(Utils.encodeHexString(ecbDecryptReturn) + " " + Utils.encodeHexString(lKey));

        return HexUtil.toHexString(ecbEncryptReturn);
    }

}

