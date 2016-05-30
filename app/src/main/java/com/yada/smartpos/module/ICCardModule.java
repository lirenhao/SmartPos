package com.yada.smartpos.module;

import com.newland.mtype.module.common.iccard.ICCardSlot;
import com.newland.mtype.module.common.iccard.ICCardSlotState;
import com.newland.mtype.module.common.iccard.ICCardType;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ICCardModule {
	public byte[] call(ICCardSlot slot, ICCardType cardType, byte[] req, long timeout, TimeUnit timeunit);

	public Map<ICCardSlot, ICCardSlotState> checkSlotsState();

	public void powerOff(ICCardSlot slot, ICCardType cardType);

	public byte[] powerOn(ICCardSlot slot, ICCardType cardType);

	public void setICCardType(ICCardSlot slot, ICCardType cardType);

}
