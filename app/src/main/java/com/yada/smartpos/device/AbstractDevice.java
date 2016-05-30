package com.yada.smartpos.device;

import com.yada.smartpos.module.DeviceControllerModule;

public abstract class AbstractDevice {
	public abstract void initController();

	public abstract void disconnect();

	public abstract boolean isControllerAlive();

	public abstract DeviceControllerModule getController();

	public abstract void connectDevice();

}
