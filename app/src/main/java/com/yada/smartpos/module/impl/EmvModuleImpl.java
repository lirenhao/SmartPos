package com.yada.smartpos.module.impl;

import android.content.Context;
import com.newland.mtype.module.common.emv.*;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.MSDAlgorithm;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.util.ModuleBase;

import java.io.InputStream;
import java.util.List;

/**
 * K21连接方式下的EMV模块接口实现类
 */
public class EmvModuleImpl extends ModuleBase implements EmvModule {
	private com.newland.mtype.module.common.emv.EmvModule emvModule;

	public EmvModuleImpl() {
		 emvModule = (com.newland.mtype.module.common.emv.EmvModule) factory.getExModule("EMV_INNERLEVEL2");
//		emvModule = (EmvModule) factory.getModule(ModuleType.COMMON_EMV);
	}

	// 0.增加一个AID
	@Override
	public boolean addAID(AIDConfig aidConfig) {
		boolean issuccess = emvModule.addAID(aidConfig);
		return issuccess;
	}

	// 1.使用完整的aid数据源增加一个AID
	@Override
	public boolean addAIDWithDataSource(byte[] aidDatasource) {
		boolean issuccess = emvModule.addAIDWithDataSource(aidDatasource);
		return issuccess;
	}

	// 2.增加一组公钥
	@Override
	public boolean addCAPublicKey(byte[] rid, CAPublicKey capk) {
		boolean issuccess = emvModule.addCAPublicKey(rid, capk);
		return issuccess;
	}

	// 3.增加一组公钥（使用完整的公钥数据源）
	@Override
	public boolean addCAPublicKeyWithDataSource(byte[] caDataSource) {
		boolean issuccess = emvModule.addCAPublicKeyWithDataSource(caDataSource);
		return issuccess;
	}

	// 4.清理所有AID
	@Override
	public boolean clearAllAID() {
		boolean issuccess = emvModule.clearAllAID();
		return issuccess;
	}

	// 5.清理全部公钥
	@Override
	public boolean clearAllCAPublicKey() {
		boolean issuccess = emvModule.clearAllCAPublicKey();
		return issuccess;
	}

	// 6.清理一个rid以下全部的全部公钥
	@Override
	public boolean clearCAPublicKeyByRid(byte[] rid) {
		boolean issuccess = emvModule.clearCAPublicKeyByRid(rid);
		return issuccess;
	}

	// 7.删除一个AID
	@Override
	public boolean deleteAID(byte[] aid) {
		boolean issuccess = emvModule.deleteAID(aid);
		return issuccess;
	}

	// 8.删除一个rid以下某个索引对应公钥
	@Override
	public boolean deleteCAPublicKey(byte[] rid, int index) {
		boolean issuccess = emvModule.deleteCAPublicKey(rid, index);
		return issuccess;
	}

	// 9.获取全部的aid列表（返回数据仅包含 AID字段（9F06））
	@Override
	public List<AIDConfig> fetchAllAID() {
		List<AIDConfig> listAIDConfig = emvModule.fetchAllAID();
		if(listAIDConfig.size()==0){
			return null;
		}else{
			return listAIDConfig;
		}
		
	}

	// 10获取公钥列表（公钥数据只有认证中心公钥索引(0x9f22)、RID(9F06)）
	@Override
	public List<CAPublicKey> fetchAllCAPublicKey() {
		List<CAPublicKey> listAIDConfig = emvModule.fetchAllCAPublicKey();
		return listAIDConfig;
	}

	// 11获得最后一笔交易日志信息
	@Override
	public byte[] fetchLastTradeInfo(EmvDataType mode) {
//		if (expectedTlvTags == null) {
//			return null;
//		} else {
//			byte[] lastTradeInfo = emvModule.fetchLastTradeInfo(expectedTlvTags);
//			return lastTradeInfo;
//		}
		byte[] lastTradeInfo =emvModule.fetchLastTradeInfo(mode);
		return lastTradeInfo;	
	}

	// 12获得所有pboc交易日志
	@Override
	public List<PbocTransLog> fetchPbocLog(EmvControllerListener callbackListener) {
		List<PbocTransLog> listPbocTransLog = emvModule.fetchPbocLog(callbackListener);
		return listPbocTransLog;
	}
	// 14获取指定的aid
	@Override
	public AIDConfig fetchSpecifiedAID(byte[] aid) {
		AIDConfig aidConfig = emvModule.fetchSpecifiedAID(aid);
		return aidConfig;
	}

	// 15获取指定的公钥
	@Override
	public CAPublicKey fetchSpecifiedCAPublicKey(byte[] rid, int index) {
		CAPublicKey caPublicKey = emvModule.fetchSpecifiedCAPublicKey(rid, index);
		return caPublicKey;
	}

	// 16获得一个emv流程下的账户信息
	@Override
	public EmvCardInfo getAccountInfo(EmvControllerListener callbackListener) {
		EmvCardInfo emvCardInfo = emvModule.getAccountInfo(callbackListener);
		return emvCardInfo;
	}

	// 17获取EMV数据
	@Override
	public byte[] getEmvData(int tag) {
		byte[] emvData = emvModule.getEmvData(tag);
		return emvData;
	}

	// 18获得一个EMV交易控制器
	@Override
	public EmvTransController getEmvTransController(EmvControllerListener emvControllerListener) {
		EmvTransController emvTransController = emvModule.getEmvTransController(emvControllerListener);
		return emvTransController;
	}

	// 19获得一个系统内支持的标签
	@Override
	public EmvTagRef getSystemSupportTagRef(int tag) {
		EmvTagRef emvTagRef = emvModule.getSystemSupportTagRef(tag);
		return emvTagRef;
	}

	// 20初始化emv模块
	@Override
	public void initEmvModule(Context context) {
		emvModule.initEmvModule(context);
	}

	// 21初始化emv模块
	@Override
	public void initEmvModule(Context context, InputStream inputStream) {
		emvModule.initEmvModule(context, inputStream);
	}

	// 22获取ic卡磁道等效数据
	@Override
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg) {
		SwipResult swipResult = emvModule.readEncryptResult(readModel, wk, alg);
		if (swipResult == null) {
			return null;
		} else {
			return swipResult;
		}

	}

	// 23
	@Override
	public boolean setEmvData(int tag, byte[] value) {
		Boolean setEmvData = emvModule.setEmvData(tag, value);
		return setEmvData;
	}

	// 24联机pin参数设置
	@Override
	public void setOnlinePinConfig(OnlinePinConfig onlinePinConfig) {
		emvModule.setOnlinePinConfig(onlinePinConfig);
	}

	// 25设置终端参数
	@Override
	public boolean setTrmnlParams(TerminalConfig trmnlConfig) {
		Boolean setTrmnlParams = emvModule.setTrmnlParams(trmnlConfig);
		return setTrmnlParams;
	}

	// 26设置工作模式
	@Override
	public void setWorkingMode(EmvWorkingMode workingMode) {
		emvModule.setWorkingMode(workingMode);
	}
}
