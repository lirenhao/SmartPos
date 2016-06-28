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

        for (String aid : block03Map.keySet()) {
            AIDConfig aidConfig = new AIDConfig();
            aidConfig.setAid(block03Map.get(aid).aid);// 0x9f06
            aidConfig.setAppSelectIndicator((int) block03Map.get(aid).asi);// 0xDF01
            aidConfig.setAppVersionNumberTerminal(block03Map.get(aid).appVersion);// 0x9f09
            aidConfig.setTacDefault(block03Map.get(aid).tacDefault);// 0xDF11
            aidConfig.setTacOnLine(block03Map.get(aid).tacOnLine);// 0xDF12
            aidConfig.setTacDenial(block03Map.get(aid).tacReject);// 0xDF13
            aidConfig.setTerminalFloorLimit(intToHex(block03Map.get(aid).terminalFloorLimit, 8));// 0x9f1b
            aidConfig.setThresholdValueForBiasedRandomSelection(block03Map.get(aid).biasRandomlySelectedThreshold);// 0xDF15
            aidConfig.setMaxTargetPercentageForBiasedRandomSelection((int) block03Map.get(aid).biasRandomlySelectedMaxPercentage);// 0xDF16
            aidConfig.setTargetPercentageForRandomSelection((int) block03Map.get(aid).randomlySelectedPercentage);// 0xDF17
            aidConfig.setDefaultDDOL(block03Map.get(aid).ddol);// 0xDF14
            aidConfig.setOnLinePinCapability((int) block03Map.get(aid).appFlag);// 0xDF18
            aidConfig.setEcTransLimit(intToHex((int) block03Map.get(aid).ecTerminalLimit, 12));// 0x9F7B
            // DF19强制更新EMV相关参数标志,保留不用
            aidConfig.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF19
            aidConfig.setNciccTransLimit(intToHex((int) block03Map.get(aid).contactLessLimit, 12));// 0xDF20
            aidConfig.setNciccCVMLimit(intToHex((int) block03Map.get(aid).contactLessCVMLimit, 12));// 0xDF21
            // DF24在IST中标识RID列表
            aidConfig.setEcCapability(0);// 0xDF24
            aidConfig.setCoreConfigType(Integer.parseInt(block03Map.get(aid).paramVersion));// 0xDF25
            boolean addAIDResult = emvModule.addAID(aidConfig);
            if (!addAIDResult)
                mainActivity.showMessage("装载AID错误！", Const.MessageTag.ERROR);
        }
    }

    public void setCAPK(Map<String, Block04_1> block04_1Map, Map<String, Block04_2> block04_2Map) {
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);

        for (String rid : block04_1Map.keySet()) {
            CAPublicKey caKey = new CAPublicKey(block04_1Map.get(rid).publicKeyIndex,
                    block04_1Map.get(rid).hashAlgorithmIndicator, block04_1Map.get(rid).publicKeyAlgorithmIndicator,
                    block04_1Map.get(rid).publicKeyModulus, block04_1Map.get(rid).publicKeyExponent,
                    block04_1Map.get(rid).publicKeyHash, block04_1Map.get(rid).expiryDate);
            boolean addCAPK = emvModule.addCAPublicKey(block04_1Map.get(rid).rid, caKey);
            if (!addCAPK)
                mainActivity.showMessage("装载CAPK错误！", Const.MessageTag.ERROR);
        }

        for (String rid : block04_2Map.keySet()) {
            CAPublicKey caKey = new CAPublicKey(block04_2Map.get(rid).publicKeyIndex,
                    0, block04_2Map.get(rid).hashAlgorithmIndicator,
                    block04_2Map.get(rid).publicKeyModulus, HexUtil.parseHex("000003"),
                    block04_2Map.get(rid).publicKeyHash, block04_2Map.get(rid).expiryDate);
            boolean addCAPK = emvModule.addCAPublicKey(block04_2Map.get(rid).rid, caKey);
            if (!addCAPK)
                mainActivity.showMessage("装载CAPK错误！", Const.MessageTag.ERROR);
        }
    }

    private byte[] intToHex(int index, int length) {
        String hexString = "" + index;
        for (int i = 0; i < length - hexString.length(); i++) {
            hexString = "0" + hexString;
        }
        return HexUtil.parseHex(hexString);
    }
}
