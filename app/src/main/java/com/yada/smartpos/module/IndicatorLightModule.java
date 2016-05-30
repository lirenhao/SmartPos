package com.yada.smartpos.module;

import com.newland.mtype.module.common.light.LightType;


/**
 * Created by HJP on 2015/8/12.
 */
public interface IndicatorLightModule {
	// 对指示灯进行闪烁操作（非阻塞操作）
	public boolean blinkLight(LightType[] lightType);

	public String[] getSupportedLight();

	
	public boolean operateLight(LightType[] lightType, int times, int interval);

	public boolean operateLight(LightType lightType, int times, int interval);

	public boolean turnOffLight(LightType[] lightType);

	public boolean turnOnLight(LightType[] lightType);
}
