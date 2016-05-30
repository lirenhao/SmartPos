package com.yada.smartpos.module.impl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.light.IndicatorLight;
import com.newland.mtype.module.common.light.LightType;
import com.yada.smartpos.module.IndicatorLightModule;
import com.yada.smartpos.util.ModuleBase;

/**
 * 指示灯模块接口实现
 */
public class IndicatorLightModuleImpl extends ModuleBase implements IndicatorLightModule {

	private IndicatorLight indicatorLight;

	public IndicatorLightModuleImpl() {
		indicatorLight = (IndicatorLight) factory.getModule(ModuleType.COMMON_INDICATOR_LIGHT);
	}

	// 对指示灯进行闪烁操作（非阻塞操作）
	@Override
	public boolean blinkLight(LightType[] lightType) {
		return indicatorLight.blinkLight(lightType);
	}

	// 获得所有支持的灯类型 通用灯类型及位置
	@Override
	public String[] getSupportedLight() {
		return indicatorLight.getSupportedLight();
	}

	// 对多个指示灯进行闪烁操作（阻塞操作）
	@Override
	public boolean operateLight(LightType[] lightType, int times, int interval) {
		Boolean operateMultLightResult = indicatorLight.operateLight(lightType, times, interval);
		return operateMultLightResult;
	}

	// 对单个指示灯进行闪烁操作（阻塞操作）
	@Override
	public boolean operateLight(LightType lightType, int times, int interval) {
		Boolean operateLight = indicatorLight.operateLight(lightType, times, interval);
		return operateLight;
	}

	// 对指示灯进行熄灭操作（非阻塞操作）
	@Override
	public boolean turnOffLight(LightType[] lightType) {
		Boolean turnOffLightResult = indicatorLight.turnOffLight(lightType);
		return turnOffLightResult;
	}

	// 对指示灯进行常亮操作（非阻塞操作）
	@Override
	public boolean turnOnLight(LightType[] lightType) {
		Boolean turnOnLight = indicatorLight.turnOnLight(lightType);
		return turnOnLight;
	}
}
