package com.yada.smartpos.module;

import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardReaderResult;
import com.newland.mtype.module.common.rfcard.RFCardType;

import java.util.concurrent.TimeUnit;

public interface CardReaderModule {
	public void cancelCardRead();

	public void closeCardReader();

	public ModuleType[] getLastReaderTypes();

	public ModuleType[] getSupportCardReaderModule();

	public OpenCardReaderResult openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes,
											   boolean isAllowfallback, boolean isMSDChecking, long timeout, TimeUnit timeunit);

	public void openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes, boolean isAllowfallback,
							   boolean isMSDChecking, long timeout, TimeUnit timeunit, DeviceEventListener<OpenCardReaderEvent> listener);
}
