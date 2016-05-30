package com.yada.smartpos.module;

import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.*;

import java.util.concurrent.TimeUnit;

public interface PinInputModule {
	public MacResult calcMac(MacAlgorithm macAlgorithm, KeyManageType pinManageType, WorkingKey wk, byte[] input);

	public void cancelPinInput();

	public byte[] decrypt(EncryptAlgorithm encryptAlgorithm, WorkingKey wk, byte[] input, byte[] cbcInit);

	public byte[] encrypt(EncryptAlgorithm encryptAlgorithm, WorkingKey wk, byte[] input, byte[] cbcInit);

	public PinInputResult encryptPIN(WorkingKey wk, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol, byte[] pin);

	public KSNLoadResult loadIPEK(KSNKeyType keytype, int KSNIndex, byte[] ksn, byte[] defaultKeyData, int mainKeyIndex, byte[] checkValue);

	public byte[] loadMainKey(KekUsingType kekUsingType, int mainIndex, byte[] data, byte[] checkValue, int kekIndex);

	public LoadPKResultCode loadPublicKey(LoadPKType keytype, int pkIndex, String pkLength, byte[] pkModule, byte[] pkExponent, byte[] index,
										  byte[] mac);

	public byte[] loadWorkingKey(WorkingKeyType type, int mainKeyIndex, int workingKeyIndex, byte[] data, byte[] checkValue);


	public void startK21StandPininput(String displayContent, WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType,
									  String acctSymbol, int inputMaxLen, byte[] pwdLengthRange, byte[] pinPadding, PinConfirmType pinConfirmType, long timeout,
									  TimeUnit timeunit, KeySoundParams keySoundParams, PeripheralMonitor[] peripheralMonitor,
									  DeviceEventListener<K21PininutEvent> inputListener);
	/**
	 * 设置键盘位置并获取随机键盘键值（N900专用）
	 * 
	 * @param coordinate
	 *            按键坐标（一个按键定位包含左上角和右下角坐标,每个坐标4字节）
	 * @return 键盘按键的值
	 */
	public byte[] loadRandomKeyboard(KeyboardRandom coordinate);
}
