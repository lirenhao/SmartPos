package com.yada.smartpos.module;

import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.MSDAlgorithm;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;

public interface SwiperModule {
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, byte[] acctMask, MSDAlgorithm alg);// 读取加密的磁道信息

	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg);

	public SwipResult readPlainResult(SwiperReadModel[] readModel);// 通过安全认证后，使用明文方式返回刷卡结果

}
