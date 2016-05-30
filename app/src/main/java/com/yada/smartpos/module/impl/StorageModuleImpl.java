package com.yada.smartpos.module.impl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.storage.ReadFileResult;
import com.newland.mtype.module.common.storage.Storage;
import com.newland.mtype.module.common.storage.StorageResult;
import com.newland.mtype.module.common.storage.WriteFileResult;
import com.yada.smartpos.module.StorageModule;
import com.yada.smartpos.util.ModuleBase;

/**
 * Created by YJF 存储模块接口实现
 */
public class StorageModuleImpl extends ModuleBase implements StorageModule {
	private Storage storage;

	public StorageModuleImpl() {
		storage = (Storage) factory.getModule(ModuleType.COMMON_STORAGE);
	}

	//增加存储记录
	@Override
	public StorageResult addRecord(String recordName, byte[] content) {
		StorageResult storageResult = storage.addRecord(recordName, content);
		return storageResult;
	}

	//获取存储记录
	@Override
	public byte[] fetchRecord(String recordName, int recordNo, String checkParams1, String checkParams2) {
		byte[] fetchrecord = storage.fetchRecord(recordName, recordNo, checkParams1, checkParams2);
		return fetchrecord;
	}

	//获取存储记录数
	@Override
	public int fetchRecordCount(String recordName) {
		int count = storage.fetchRecordCount(recordName);
		return count;
	}

	//初始化存储记录
	@Override
	public boolean initializeRecord(String recordName, int recordLength, int params1Offset, int params1Length, int params2Offset, int params2Length) {
		boolean isInitializeRecordsuccess = storage.initializeRecord(recordName, recordLength, params1Offset, params1Length, params2Offset,
				params2Length);
		return isInitializeRecordsuccess;
	}

	//读文件
	@Override
	public ReadFileResult readFile(String fileName, int offset, int dataLength) {
		ReadFileResult readFileResult = storage.readFile(fileName, offset, dataLength);
		return readFileResult;
	}

	//更新
	@Override
	public StorageResult updateRecord(String recordName, int recordNo, String checkParams1, String checkParams2, byte[] content) {
		StorageResult storageResult = storage.updateRecord(recordName, recordNo, checkParams1, checkParams2, content);
		return storageResult;
	}

	//写文件
	@Override
	public WriteFileResult writeFile(String fileName, int offset, byte[] content) {
		WriteFileResult writeFileResult = storage.writeFile(fileName, offset, content);
		return writeFileResult;
	}
}
