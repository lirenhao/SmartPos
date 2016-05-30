package com.yada.smartpos.module.impl;

import com.newland.mtype.DeviceInfo;
import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.security.*;
import com.yada.smartpos.module.SecurityModule;
import com.yada.smartpos.util.ModuleBase;

/**
 * Created by YJF 设备安全认证模块接口实现
 */
public class SecurityModuleImpl extends ModuleBase implements SecurityModule {
	private com.newland.mtype.module.common.security.SecurityModule securityModule;

	public SecurityModuleImpl() {
		securityModule = (com.newland.mtype.module.common.security.SecurityModule) factory.getModule(ModuleType.COMMON_SECURITY);
	}

	// 0.双向对称认证
	@Override
	public AuthenticationResult bidirectionalAuthentication(AuthenticationType authType, AuthDataMode authDataMode, int returnDataLen, int keyIndex,
															byte[] workKey, byte[] authData, byte[] randomNum) {
		AuthenticationResult authenticationResult = securityModule.bidirectionalAuthentication(authType, authDataMode, returnDataLen, keyIndex,
				workKey, authData, randomNum);
		return authenticationResult;
	}

	// 1设备认证
	@Override
	public String deviceIdentify(CertifiedModel model, byte[] random) {
		String deviceIdentify = securityModule.deviceIdentify(model, random);
		return deviceIdentify;
	}

	// 2产生非对称认证数据
	@Override
	public AuthenticationResult generateAsymmetricData(int pubKeyIndex, int randomLength, int mainKeyIndex) {
		AuthenticationResult authenticationResult = securityModule.generateAsymmetricData(pubKeyIndex, randomLength, mainKeyIndex);
		return authenticationResult;
	}

	// 3获得设备信息
	@Override
	public DeviceInfo getDeviceInfo() {
		DeviceInfo deviceInfo = securityModule.getDeviceInfo();
		return deviceInfo;
	}

	// 4获得设备信息（me11音频口方式）
	@Override
	public DeviceInfo getDeviceInfoByAudio() {
		return null;
	}

	// 5获得一个线路保护的随机数。
	@Override
	public byte[] getSecurityRandom() {
		byte[] securityRadom = securityModule.getSecurityRandom();
		return securityRadom;
	}

	// 6服务端认证 该命令用于设备外部认证。。
	@Override
	public void serverIdentify(CertifiedModel model, byte[] security) {
		securityModule.serverIdentify(model, security);
	}

	// 7设置CSN
	@Override
	public void setCSN(String csn) {
		securityModule.setCSN(csn);
	}

	// 8在线更新密钥接口
	@Override
	public void updateIdentifySecurity(CertifiedModel model, byte[] pkData) {
		securityModule.updateIdentifySecurity(model, pkData);
	}
}
