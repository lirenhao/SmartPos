package com.yada.smartpos.module.impl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.rfcard.RFCardType;
import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mtype.module.common.rfcard.RFResult;
import com.yada.smartpos.module.RFCardModule;
import com.yada.smartpos.util.ModuleBase;

import java.util.concurrent.TimeUnit;

/**
 * 非接卡模块接口实现
 */
public class RFCardModuleImpl extends ModuleBase implements RFCardModule {
	private com.newland.mtype.module.common.rfcard.RFCardModule rfCardModule;

	public RFCardModuleImpl() {
		rfCardModule = (com.newland.mtype.module.common.rfcard.RFCardModule) factory.getModule(ModuleType.COMMON_RFCARDREADER);
	}

	// 0使用外部的密钥进行认证
	@Override
	public void authenticateByExtendKey(RFKeyMode RFKeyMode, byte[] SNR, int blockNo, byte[] key) {
		rfCardModule.authenticateByExtendKey(RFKeyMode, SNR, blockNo, key);
	}

	// 1使用加载的密钥进行认证
	@Override
	public void authenticateByLoadedKey(RFKeyMode RFKeyMode, byte[] SNR, int blockNo) {
		rfCardModule.authenticateByLoadedKey(RFKeyMode, SNR, blockNo);
	}

	// 2.非接CPU卡通讯
	@Override
	public byte[] call(byte[] req, long timeout, TimeUnit timeunit) {
		return rfCardModule.call(req, timeout, timeunit);
	}

	// 3非接卡选卡
	@Override
	public void chooseCard(byte[] serial) {
		rfCardModule.chooseCard(serial);
	}

	// 4减量操作
	@Override
	public void decrementOperation(int blockNo, byte[] data) {
		rfCardModule.decrementOperation(blockNo, data);
	}

	// 5增量操作
	@Override
	public void incrementOperation(int blockNo, byte[] data) {
		rfCardModule.incrementOperation(blockNo, data);
	}

	// 6加载密钥
	@Override
	public void loadKey(RFKeyMode rfKeyMode, int keyIndex) {
		rfCardModule.loadKey(rfKeyMode, keyIndex);
	}

	// 7下电
	@Override
	public void powerOff(int timeout) {
		rfCardModule.powerOff(timeout);
	}

	// 、8寻卡并上电
	@Override
	public RFResult powerOn(RFCardType rfCardType, int timeout) {
		return rfCardModule.powerOn(rfCardType, timeout);

	}

	// 9寻卡上电
	@Override
	public RFResult powerOn(RFCardType cardType, int timeout, String showMsg) {
		return rfCardModule.powerOn(cardType, timeout, null);
	}

	// 10非接卡防冲突
	@Override
	public byte[] preventConflict() {
		return rfCardModule.preventConflict();
	}

	// 11读块数据
	@Override
	public byte[] readDataBlock(int blockNo) {
		return rfCardModule.readDataBlock(blockNo);
	}

	// 12非接寻卡
	@Override
	public RFResult searchCard(RFCardType RFCardType, int timeout) {
		return rfCardModule.searchCard(RFCardType, timeout);
	}

	// 13存储密钥
	@Override
	public void storeKey(RFKeyMode rfKeyMode, int keyIndex, byte[] key) {
		rfCardModule.storeKey(rfKeyMode, keyIndex, key);
	}

	// 14写块数据
	@Override
	public void writeDataBlock(int blockNo, byte[] data) {
		rfCardModule.writeDataBlock(blockNo, data);
	}
}
