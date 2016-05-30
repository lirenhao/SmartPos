package com.yada.smartpos.module.impl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.*;
import com.yada.smartpos.module.SwiperModule;
import com.yada.smartpos.util.ModuleBase;

/**
 * K21连接方式下的刷卡接口实现
 */
public class SwiperModuleImpl extends ModuleBase implements SwiperModule {
	private K21Swiper swiper;
	SwipResult swipResult;

	public SwiperModuleImpl() {
		swiper = (K21Swiper) factory.getModule(ModuleType.COMMON_SWIPER);
	}

	// 根据二磁道明文计算加密的磁道数据
	public SwipResult k21CalculateTrackData(String secondTrackData, String thirdTrackData, WorkingKey wk, MSDAlgorithm alg) {
		SwipResult swipResult = swiper.calculateTrackData(secondTrackData, thirdTrackData, wk, alg);
		return swipResult;
	}

	//以掩码方式获取加密磁道信息
	@Override
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, byte[] acctMask, MSDAlgorithm alg) {
		swipResult = swiper.readEncryptResult(readModel, wk, acctMask, alg);
		return swipResult;
	}

	//获取加密的磁道信息
	@Override
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg) {
		swipResult = swiper.readEncryptResult(readModel, wk, alg);
		return swipResult;
	}

	//获取明文磁道信息
	@Override
	public SwipResult readPlainResult(SwiperReadModel[] readModel) {
		swipResult = swiper.readPlainResult(readModel);
		if (null != swipResult && swipResult.getRsltType() == SwipResultType.SUCCESS) {
			return swipResult;
		}
		return null;
	}
}
