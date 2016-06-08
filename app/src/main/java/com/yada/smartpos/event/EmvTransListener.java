package com.yada.smartpos.event;

import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.level2.EmvLevel2ControllerExtListener;

public interface EmvTransListener extends EmvLevel2ControllerExtListener {

	public void onQpbocFinished(EmvTransInfo emvTransInfo);

}
