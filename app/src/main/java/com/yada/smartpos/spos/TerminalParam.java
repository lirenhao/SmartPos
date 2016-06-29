package com.yada.smartpos.spos;

import com.newland.mtype.module.common.emv.AIDConfig;
import com.newland.mtype.module.common.emv.CAPublicKey;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.device.pos.posp.params.Block01;
import com.yada.sdk.device.pos.posp.params.Block03;
import com.yada.sdk.device.pos.posp.params.Block04_1;
import com.yada.sdk.device.pos.posp.params.Block04_2;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.util.Const;

import java.util.HashMap;
import java.util.Map;

public class TerminalParam {

    private MainActivity mainActivity;

    public TerminalParam(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setBlock01(Block01 block01) {
        ((App) mainActivity.getApplication()).setParamBlock01(block01);
    }

    public void setAid(Map<String, Block03> block03Map) {
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);

        if (null == block03Map) block03Map = new HashMap<>();

        for (String aid : block03Map.keySet()) {
            AIDConfig aidConfig = new AIDConfig();
            aidConfig.setAid(block03Map.get(aid).aid);// AID（0x9F06）
            aidConfig.setAppSelectIndicator((int) block03Map.get(aid).asi);// 应用选择指示符(0x9DF01)
            aidConfig.setAppVersionNumberTerminal(block03Map.get(aid).appVersion); // 应用版本号(0xDF01)
            aidConfig.setTacDefault(block03Map.get(aid).tacDefault);// TAC－缺省(0xDF11)
            aidConfig.setTacOnLine(block03Map.get(aid).tacOnLine);// TAC－联机（0xDF12）
            aidConfig.setTacDenial(block03Map.get(aid).tacReject);// TAC－拒绝（0xDF13）
            aidConfig.setTerminalFloorLimit(intToHex(block03Map.get(aid).terminalFloorLimit, 8));// 终端最低限额（0x9F1B）
            aidConfig.setThresholdValueForBiasedRandomSelection(block03Map.get(aid).biasRandomlySelectedThreshold);// 偏置随机选择的阈值（0xDF15）
            aidConfig.setMaxTargetPercentageForBiasedRandomSelection((int) block03Map.get(aid).biasRandomlySelectedMaxPercentage);// 偏置随机选择的最大目标百分数（0xDF16）
            aidConfig.setTargetPercentageForRandomSelection((int) block03Map.get(aid).randomlySelectedPercentage);// 随机选择的目标百分数（0xDF17）
            aidConfig.setDefaultDDOL(block03Map.get(aid).ddol);// 缺省DDOL （0xDF14）
            aidConfig.setOnLinePinCapability((int) block03Map.get(aid).appFlag);//  终端联机PIN 支持（0xDF18）
            aidConfig.setEcTransLimit(intToHex((int) block03Map.get(aid).ecTerminalLimit, 12));// 终端电子现金交易限额（0x9F7B）
            // DF19强制更新EMV相关参数标志,保留不用
            aidConfig.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 非接触读写器脱机最低限额（0xDF19）
            aidConfig.setNciccTransLimit(intToHex((int) block03Map.get(aid).contactLessLimit, 12));// 非接触读写器交易限额（0xDF20）
            aidConfig.setNciccCVMLimit(intToHex((int) block03Map.get(aid).contactLessCVMLimit, 12));// 读写器持卡人验证方法所需限制（0xDF21）

            boolean addAIDResult = emvModule.addAID(aidConfig);
            if (!addAIDResult)
                mainActivity.showMessage("装载AID错误！", Const.MessageTag.ERROR);
        }
    }

    public void setCAPK(Map<String, Block04_1> block04_1Map, Map<String, Block04_2> block04_2Map) {
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);

        if (null == block04_1Map) block04_1Map = new HashMap<>();
        if (null == block04_2Map) block04_2Map = new HashMap<>();

         /*
         index - 认证中心公钥索引(0x9f22)
         hashAlgorithmIndicator - 认证中心公钥哈什算法标识(0xDF06) TODO 国密没有
         publicKeyAlgorithmIndicator - 认证中心公钥算法标识(0xDF07)
         modulus - 认证中心公钥模(0xDF02)
         exponent - 认证中心公钥指数(0xDF04) TODO 国密没有
         sha1CheckSum - 认证中心公钥校验值(0xDF03) TODO 国密没有
         expirationDate - 认证中心公钥有效期(格式yyyyMMdd)(0xDF05)
         */
        for (String rid : block04_1Map.keySet()) {
            if (!HexUtil.toHexString(block04_1Map.get(rid).publicKeyHash).matches("0+")) {
                CAPublicKey caKey = new CAPublicKey(Integer.parseInt(HexUtil.toHexString(new byte[]{block04_1Map.get(rid).publicKeyIndex}), 16),
                        Integer.parseInt(HexUtil.toHexString(new byte[]{block04_1Map.get(rid).hashAlgorithmIndicator}), 16),
                        Integer.parseInt(HexUtil.toHexString(new byte[]{block04_1Map.get(rid).publicKeyAlgorithmIndicator}), 16),
                        block04_1Map.get(rid).publicKeyModulus, block04_1Map.get(rid).publicKeyExponent,
                        block04_1Map.get(rid).publicKeyHash, block04_1Map.get(rid).expiryDate);
                boolean addCAPK = emvModule.addCAPublicKey(block04_1Map.get(rid).rid, caKey);
                if (!addCAPK)
                    mainActivity.showMessage("装载CAPK错误！", Const.MessageTag.ERROR);
            }
        }

        for (String rid : block04_2Map.keySet()) {
            if (!HexUtil.toHexString(block04_2Map.get(rid).publicKeyHash).matches("0+")) {
                CAPublicKey caKey = new CAPublicKey(Integer.parseInt(HexUtil.toHexString(new byte[]{block04_2Map.get(rid).publicKeyIndex}), 16),
                        1, Integer.parseInt(HexUtil.toHexString(new byte[]{block04_2Map.get(rid).hashAlgorithmIndicator}), 16),
                        block04_2Map.get(rid).publicKeyModulus, HexUtil.parseHex("03"),
                        block04_2Map.get(rid).publicKeyHash, block04_2Map.get(rid).expiryDate);
                boolean addCAPK = emvModule.addCAPublicKey(block04_2Map.get(rid).rid, caKey);
                if (!addCAPK)
                    mainActivity.showMessage("装载CAPK错误！", Const.MessageTag.ERROR);
            }
        }
    }

    private byte[] intToHex(int index, int length) {
        String hexString = "" + index;
        int len = length - hexString.length();
        for (int i = 0; i < len; i++) {
            hexString = "0" + hexString;
        }
        return HexUtil.parseHex(hexString);
    }
}
