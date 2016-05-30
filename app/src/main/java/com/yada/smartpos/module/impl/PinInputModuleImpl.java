package com.yada.smartpos.module.impl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.*;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.util.ModuleBase;

import java.util.concurrent.TimeUnit;

/**
 * Created by YJF . K21连接方式下的密码输入接口实现类
 */
public class PinInputModuleImpl extends ModuleBase implements PinInputModule {
	private K21Pininput pinInput;

	public PinInputModuleImpl() {
		pinInput = (K21Pininput) factory.getModule(ModuleType.COMMON_PININPUT);
	}
	// 0.大数据mac计算
	@Override
	public MacResult calcMac(MacAlgorithm macAlgorithm, KeyManageType pinManageType, WorkingKey wk, byte[] input) {
		MacResult macResult = pinInput.calcMac(macAlgorithm, pinManageType, wk, input);
		return macResult;
	}

	// 1.撤消上一次的密码输入
	@Override
	public void cancelPinInput() {
		pinInput.cancelPinInput();
	}

	// 2.解密一串数据
	@Override
	public byte[] decrypt(EncryptAlgorithm encryptAlgorithm, WorkingKey wk, byte[] input, byte[] cbcInit) {
		byte[] result = pinInput.decrypt(encryptAlgorithm,wk, input, cbcInit);
		return result;
	}

	// 3.加密一串数据
	@Override
	public byte[] encrypt(EncryptAlgorithm encryptAlgorithm, WorkingKey wk, byte[] input, byte[] cbcInit) {
		byte[] result = pinInput.encrypt(encryptAlgorithm, wk, input, cbcInit);
		return result;
	}

	// 4.无键盘输入密码
	@Override
	public PinInputResult encryptPIN(WorkingKey wk, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol, byte[] pin) {
		return pinInput.encryptPIN(wk, pinManageType, acctInputType, acctSymbol, pin);
	}

	// 5.loadIPEK
	@Override
	public KSNLoadResult loadIPEK(KSNKeyType keytype, int KSNIndex, byte[] ksn, byte[] defaultKeyData, int mainKeyIndex, byte[] checkValue) {
		KSNLoadResult ksnLoadResult = pinInput.loadIPEK(keytype, KSNIndex, ksn, defaultKeyData, mainKeyIndex, checkValue);
		return ksnLoadResult;
	}

	// 6.装载主密钥
	@Override
	public byte[] loadMainKey(KekUsingType kekUsingType, int mainIndex, byte[] data, byte[] checkValue, int kekIndex) {
		byte[] mainKey = pinInput.loadMainKey(kekUsingType, mainIndex, data, checkValue, kekIndex);
		return mainKey;
	}

	//7 
	public LoadPKResultCode loadPublicKey(LoadPKType keytype, int pkIndex, String pkLength, byte[] pkModule, byte[] pkExponent, byte[] index, byte[] mac) {
			LoadPKResultCode loadpkresult = pinInput.loadPublicKey(keytype, pkIndex, pkLength, pkModule, pkExponent, index, mac);
			return loadpkresult;
	}
	// 8.装载工作密钥
	@Override
	public byte[] loadWorkingKey(WorkingKeyType type, int mainKeyIndex, int workingKeyIndex, byte[] data, byte[] checkValue) {
		byte[] wk = pinInput.loadWorkingKey(type, mainKeyIndex, workingKeyIndex, data, checkValue);
		return wk;
	}

	//9开启一个按鍵音可控的密码输入过程
	@Override
	public void startK21StandPininput(String displayContent, WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType,
									  String acctSymbol, int inputMaxLen, byte[] pwdLengthRange, byte[] pinPadding, PinConfirmType pinConfirmType, long timeout,
									  TimeUnit timeunit, KeySoundParams keySoundParams, PeripheralMonitor[] peripheralMonitor,
									  DeviceEventListener<K21PininutEvent> inputListener) {
		pinInput.startStandardPinInput(displayContent, workingKey, pinManageType, acctInputType, acctSymbol, inputMaxLen, pwdLengthRange, pinPadding,
				pinConfirmType, timeout, timeunit, keySoundParams, peripheralMonitor, inputListener);
	}
	@Override
	public byte[] loadRandomKeyboard(KeyboardRandom coordinate) {
		byte[] keyboard =pinInput.loadRandomKeyboard(coordinate);
		return keyboard;
	}

}
