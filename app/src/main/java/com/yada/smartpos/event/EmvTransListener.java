package com.yada.smartpos.event;

import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.level2.EmvLevel2ControllerExtListener;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.pos.sdk.util.ISO8583Exception;

import java.io.IOException;

public interface EmvTransListener extends EmvLevel2ControllerExtListener {

	public void onQpbocFinished(EmvTransInfo emvTransInfo);

	public void onSwipeFinished(SwipResult swipResult) throws IOException, ISO8583Exception;

}
