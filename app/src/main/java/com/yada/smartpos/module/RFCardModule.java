package com.yada.smartpos.module;

import com.newland.mtype.module.common.rfcard.RFCardType;
import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mtype.module.common.rfcard.RFResult;

import java.util.concurrent.TimeUnit;

public interface RFCardModule {
	public void authenticateByExtendKey(RFKeyMode RFKeyMode, byte[] SNR, int blockNo, byte[] key);

	public void authenticateByLoadedKey(RFKeyMode RFKeyMode, byte[] SNR, int blockNo);

	public byte[] call(byte[] req, long timeout, TimeUnit timeunit);

	public void chooseCard(byte[] serial);

	public void decrementOperation(int blockNo, byte[] data);

	public void incrementOperation(int blockNo, byte[] data);

	public void loadKey(RFKeyMode rfKeyMode, int keyIndex);

	public void powerOff(int timeout);

	public RFResult powerOn(RFCardType rfCardType, int timeout);

	public RFResult powerOn(RFCardType cardType, int timeout, String showMsg);

	public byte[] preventConflict();

	public byte[] readDataBlock(int blockNo);

	public RFResult searchCard(RFCardType rfKeyMode, int timeout);

	public void storeKey(RFKeyMode rfKeyMode, int keyIndex, byte[] key);

	public void writeDataBlock(int blockNo, byte[] data);
}
