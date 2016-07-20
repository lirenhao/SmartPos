package com.yada.smartpos.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.newland.mtype.module.common.emv.AIDConfig;
import com.newland.mtype.module.common.emv.CAPublicKey;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.device.encryption.IEncryption;
import com.yada.sdk.packages.transaction.IPacker;
import com.yada.smartpos.R;
import com.yada.smartpos.device.N900Device;
import com.yada.smartpos.encryption.EncryptionPos;
import com.yada.smartpos.fragment.*;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.spos.SposPacker;
import com.yada.smartpos.spos.VirtualPos;
import com.yada.smartpos.util.Const;

public class MainActivity extends Activity {

    private MainActivity mainActivity;
    private FragmentManager fragmentManager;
    private Handler fragmentHandler;
    private WaitThreat waitThreat;
    private VirtualPos virtualPos;

    private WaitThreat authPasswordWaitThreat = new WaitThreat();
    private WaitThreat amountWaitThreat = new WaitThreat();
    private WaitThreat swipeCardWaitThreat = new WaitThreat();
    private WaitThreat inputPinWaitThreat = new WaitThreat();
    private WaitThreat proofNoWaitThreat = new WaitThreat();
    private WaitThreat authCodeWaitThreat = new WaitThreat();
    private WaitThreat showFormWaitThreat = new WaitThreat();
    private WaitThreat installmentWaitThreat = new WaitThreat();
    private WaitThreat dateWheelWaitThreat = new WaitThreat();
    private WaitThreat timeWheelWaitThreat = new WaitThreat();

    public Handler getFragmentHandler() {
        return fragmentHandler;
    }

    public WaitThreat getWaitThreat() {
        return waitThreat;
    }

    public WaitThreat getAuthPasswordWaitThreat() {
        return authPasswordWaitThreat;
    }

    public WaitThreat getAmountWaitThreat() {
        return amountWaitThreat;
    }

    public WaitThreat getSwipeCardWaitThreat() {
        return swipeCardWaitThreat;
    }

    public WaitThreat getInputPinWaitThreat() {
        return inputPinWaitThreat;
    }

    public WaitThreat getProofNoWaitThreat() {
        return proofNoWaitThreat;
    }

    public WaitThreat getAuthCodeWaitThreat() {
        return authCodeWaitThreat;
    }

    public WaitThreat getShowFormWaitThreat() {
        return showFormWaitThreat;
    }

    public WaitThreat getInstallmentWaitThreat() {
        return installmentWaitThreat;
    }

    public WaitThreat getDateWheelWaitThreat() {
        return dateWheelWaitThreat;
    }

    public WaitThreat getTimeWheelWaitThreat() {
        return timeWheelWaitThreat;
    }

    public VirtualPos getVirtualPos(){
        return virtualPos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        waitThreat = new WaitThreat();

        // 初始化设备
        N900Device k21Device = new N900Device(this);
        k21Device.initController();
        k21Device.connectDevice();
        initEmv();

        // 初始化POS
        IPacker packer = new SposPacker(this, HexUtil.parseHex("60001200001306"));
        IEncryption encryption = new EncryptionPos();
        String zmkTmk = "E5998509E585542884F1B3C0B3CD1053";
        virtualPos = new VirtualPos("104110070110814", "11000897", packer, "10.2.54.14", 1000, zmkTmk,
                50000, encryption, mainActivity);

        // 初始化界面调度
        fragmentManager = getFragmentManager();
        fragmentHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                FragmentTransaction tx = fragmentManager.beginTransaction();
                switch (msg.what) {
                    case 0:
                        tx.replace(R.id.main, new MenuFragment(mainActivity), msg.obj.toString()).commit();
                        break;
                    case 1:
                        tx.replace(R.id.main, new AmountFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 2:
                        tx.replace(R.id.main, new SwipeCardFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 3:
                        tx.replace(R.id.main, new InputPinFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 4:
                        tx.replace(R.id.main, new ProofNoFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 5:
                        tx.replace(R.id.main, new AuthCodeFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 6:
                        tx.replace(R.id.main, new ShowFormFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 7:
                        tx.replace(R.id.main, new InstallmentFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 8:
                        tx.replace(R.id.main, new DateWheelFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 9:
                        tx.replace(R.id.main, new TimeWheelFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 10:
                        tx.replace(R.id.main, new AuthPasswordFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    case 11:
                        tx.add(R.id.main, new LoadingFragment(mainActivity), msg.obj.toString()).commit();
                        break;
                    case 100:
                        tx.replace(R.id.main, new OrderFragment(mainActivity), msg.obj.toString())
                                .addToBackStack(null).commit();
                        break;
                    default:
                        break;
                }
            }
        };

        Message msg = fragmentHandler.obtainMessage(0);
        msg.obj = "menu";
        msg.sendToTarget();
    }

    @Override
    protected void onDestroy() {
        virtualPos.store();
        super.onDestroy();
    }

    private void initEmv() {
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);
        // emv添加aid
        AIDConfig aidConfig = new AIDConfig();
        aidConfig.setAid(HexUtil.parseHex("A000000333010102"));// 0x9f06
        aidConfig.setAppSelectIndicator(0);// 0xDF01
        aidConfig.setAppVersionNumberTerminal(new byte[]{0x00, (byte) 0x20});// 0x9f09
        aidConfig.setTacDefault(HexUtil.parseHex("FC78FCF8F0"));// 0xDF11
        aidConfig.setTacOnLine(HexUtil.parseHex("FC78FCF8F0"));// 0xDF12
        aidConfig.setTacDenial(HexUtil.parseHex("0010000000"));// 0xDF13
        aidConfig.setTerminalFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x05});// 0x9f1b
        aidConfig.setThresholdValueForBiasedRandomSelection(new byte[]{0x00, 0x00, 0x00, (byte) 0x28});// 0xDF15
        aidConfig.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
        aidConfig.setTargetPercentageForRandomSelection(14);// 0xDF17
        aidConfig.setDefaultDDOL(HexUtil.parseHex("9F3704"));// 0xDF14
        aidConfig.setOnLinePinCapability(1);// 0xDF18
        aidConfig.setEcTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0x9F7B
        aidConfig.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF19
        aidConfig.setNciccTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF20
        aidConfig.setNciccCVMLimit(new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00});// 0xDF21
        aidConfig.setEcCapability(0);// 0xDF24
        aidConfig.setCoreConfigType(2);// 0xDF25
        boolean addAIDResult = emvModule.addAID(aidConfig);
        if (!addAIDResult)
            mainActivity.showMessage("装载AID1错误！", Const.MessageTag.ERROR);

        AIDConfig aidConfig2 = new AIDConfig();
        aidConfig2.setAid(HexUtil.parseHex("A000000333010101"));// 0x9f06
        aidConfig2.setAppSelectIndicator(0);// 0xDF01
        aidConfig2.setAppVersionNumberTerminal(new byte[]{0x00, (byte) 0x20});// 0x9f09
        aidConfig2.setTacDefault(HexUtil.parseHex("FC78FCF8F0"));// 0xDF11
        aidConfig2.setTacOnLine(HexUtil.parseHex("FC78FCF8F0"));// 0xDF12
        aidConfig2.setTacDenial(HexUtil.parseHex("0010000000"));// 0xDF13
        aidConfig2.setTerminalFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x05});// 0x9f1b
        aidConfig2.setThresholdValueForBiasedRandomSelection(new byte[]{0x00, 0x00, 0x00, (byte) 0x28});// 0xDF15
        aidConfig2.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
        aidConfig2.setTargetPercentageForRandomSelection(14);// 0xDF17
        aidConfig2.setDefaultDDOL(HexUtil.parseHex("9F3704"));// 0xDF14
        aidConfig2.setOnLinePinCapability(1);// 0xDF18
        aidConfig2.setEcTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0x9F7B
        aidConfig2.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF19
        aidConfig2.setNciccTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF20
        aidConfig2.setNciccCVMLimit(new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00});// 0xDF21
        aidConfig2.setEcCapability(0);// 0xDF24
        aidConfig2.setCoreConfigType(2);// 0xDF25
        emvModule.initEmvModule(mainActivity);
        boolean addAIDResult2 = emvModule.addAID(aidConfig2);
        if (!addAIDResult2)
            mainActivity.showMessage("装载AID2错误！", Const.MessageTag.ERROR);

        AIDConfig aidConfig3 = new AIDConfig();
        aidConfig3.setAid(HexUtil.parseHex("A000000333010103"));// 0x9f06
        aidConfig3.setAppSelectIndicator(0);// 0xDF01
        aidConfig3.setAppVersionNumberTerminal(new byte[]{0x00, (byte) 0x20});// 0x9f09
        aidConfig3.setTacDefault(HexUtil.parseHex("FC78FCF8F0"));// 0xDF11
        aidConfig3.setTacOnLine(HexUtil.parseHex("FC78FCF8F0"));// 0xDF12
        aidConfig3.setTacDenial(HexUtil.parseHex("0010000000"));// 0xDF13
        aidConfig3.setTerminalFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x05});// 0x9f1b
        aidConfig3.setThresholdValueForBiasedRandomSelection(new byte[]{0x00, 0x00, 0x00, (byte) 0x28});// 0xDF15
        aidConfig3.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
        aidConfig3.setTargetPercentageForRandomSelection(14);// 0xDF17
        aidConfig3.setDefaultDDOL(HexUtil.parseHex("9F3704"));// 0xDF14
        aidConfig3.setOnLinePinCapability(1);// 0xDF18
        aidConfig3.setEcTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0x9F7B
        aidConfig3.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF19
        aidConfig3.setNciccTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF20
        aidConfig3.setNciccCVMLimit(new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00});// 0xDF21
        aidConfig3.setEcCapability(0);// 0xDF24
        aidConfig3.setCoreConfigType(2);// 0xDF25
        emvModule.initEmvModule(mainActivity);
        boolean addAIDResult3 = emvModule.addAID(aidConfig3);
        if (!addAIDResult3)
            mainActivity.showMessage("装载AID3错误！", Const.MessageTag.ERROR);

        AIDConfig aidConfig4 = new AIDConfig();
        aidConfig4.setAid(HexUtil.parseHex("A000000333010106"));// 0x9f06
        aidConfig4.setAppSelectIndicator(0);// 0xDF01
        aidConfig4.setAppVersionNumberTerminal(new byte[]{0x00, (byte) 0x20});// 0x9f09
        aidConfig4.setTacDefault(HexUtil.parseHex("FC78FCF8F0"));// 0xDF11
        aidConfig4.setTacOnLine(HexUtil.parseHex("FC78FCF8F0"));// 0xDF12
        aidConfig4.setTacDenial(HexUtil.parseHex("0010000000"));// 0xDF13
        aidConfig4.setTerminalFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x05});// 0x9f1b
        aidConfig4.setThresholdValueForBiasedRandomSelection(new byte[]{0x00, 0x00, 0x00, (byte) 0x28});// 0xDF15
        aidConfig4.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
        aidConfig4.setTargetPercentageForRandomSelection(14);// 0xDF17
        aidConfig4.setDefaultDDOL(HexUtil.parseHex("9F3704"));// 0xDF14
        aidConfig4.setOnLinePinCapability(1);// 0xDF18
        aidConfig4.setEcTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0x9F7B
        aidConfig4.setNciccOffLineFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF19
        aidConfig4.setNciccTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});// 0xDF20
        aidConfig4.setNciccCVMLimit(new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00});// 0xDF21
        aidConfig4.setEcCapability(0);// 0xDF24
        aidConfig4.setCoreConfigType(2);// 0xDF25

        emvModule.initEmvModule(mainActivity);
        boolean addAIDResult4 = emvModule.addAID(aidConfig4);
        if (!addAIDResult4)
            mainActivity.showMessage("装载AID4错误！", Const.MessageTag.ERROR);

        // EMV添加公钥
        int P9f22_1 = 1;
        byte[] df02_2 = HexUtil.parseHex("BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93B");
        byte[] df04_2 = HexUtil.parseHex("000003");
        byte[] df03_2 = HexUtil.parseHex("E881E390675D44C2DD81234DCE29C3F5AB2297A0");
        CAPublicKey caKey = new CAPublicKey(P9f22_1, 1, 1, df02_2, df04_2, df03_2, "20091231");
        boolean addCAPK = emvModule.addCAPublicKey(new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33}, caKey);
        if (!addCAPK)
            mainActivity.showMessage("装载CAPK错误！", Const.MessageTag.ERROR);
    }

    // 显示操作返回的信息
    public void showMessage(final String message, final int messageType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (messageType) {
                    case Const.MessageTag.NORMAL:
                        Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                        break;
                    case Const.MessageTag.ERROR:
                        Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                        break;
                    case Const.MessageTag.TIP:
                        Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                        break;
                    case Const.MessageTag.DATA:
                        System.out.println(message);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    // 线程等待、唤醒
    public class WaitThreat {
        final Object syncObj = new Object();

        public void waitForRslt() {
            synchronized (syncObj) {
                try {
                    syncObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void notifyThread() {
            synchronized (syncObj) {
                syncObj.notify();
            }
        }
    }
}
