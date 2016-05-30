package com.yada.smartpos.module;

import com.newland.mtype.DeviceInfo;
import com.newland.mtype.module.common.security.AuthDataMode;
import com.newland.mtype.module.common.security.AuthenticationResult;
import com.newland.mtype.module.common.security.AuthenticationType;
import com.newland.mtype.module.common.security.CertifiedModel;


/**
 * Created by HJP on 2015/8/12.
 */
public interface SecurityModule {
	public AuthenticationResult bidirectionalAuthentication(AuthenticationType authType, AuthDataMode authDataMode, int returnDataLen, int keyIndex,
															byte[] workKey, byte[] authData, byte[] randomNum);

	public String deviceIdentify(CertifiedModel model, byte[] random);

	public AuthenticationResult generateAsymmetricData(int pubKeyIndex, int randomLength, int mainKeyIndex);

	public DeviceInfo getDeviceInfo();

	public DeviceInfo getDeviceInfoByAudio();

	public byte[] getSecurityRandom();

	public void serverIdentify(CertifiedModel model, byte[] security);

	public void setCSN(String csn);

	public void updateIdentifySecurity(CertifiedModel model, byte[] pkData);
}
