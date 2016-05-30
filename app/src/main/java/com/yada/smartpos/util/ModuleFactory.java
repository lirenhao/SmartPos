package com.yada.smartpos.util;

import com.newland.me.DeviceManager;
import com.newland.mtype.Module;
import com.newland.mtype.ModuleType;
import com.yada.smartpos.module.impl.DeviceControllerModuleImpl;

/**
 * 模块获取
 */
public class ModuleFactory {
	private DeviceManager deviceManager;

	public ModuleFactory() {
		deviceManager = DeviceControllerModuleImpl.getDeviceManager();
	}

	public Module getModule(ModuleType moduleType) {
		try {
			Module module = deviceManager.getDevice().getStandardModule(moduleType);
			return module;
		} catch (Exception e) {
			e.getStackTrace();
			return null;
		}

	}

	public Module getExModule(String moduleType) {
		try {
			Module module = deviceManager.getDevice().getExModule(moduleType);
			return module;
		} catch (Exception e) {
			e.getStackTrace();
			return null;
		}

	}
}
