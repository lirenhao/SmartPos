package com.yada.smartpos.module;

import com.newland.mtype.module.common.storage.ReadFileResult;
import com.newland.mtype.module.common.storage.StorageResult;
import com.newland.mtype.module.common.storage.WriteFileResult;


/**
 * Created by HJP on 2015/8/12.
 */
public interface StorageModule {
	public StorageResult addRecord(String recordName, byte[] content);

	public byte[] fetchRecord(String recordName, int recordNo, String checkParams1, String checkParams2);

	public int fetchRecordCount(String recordName);

	public boolean initializeRecord(String recordName, int recordLength, int params1Offset, int params1Length, int params2Offset, int params2Length);

	public ReadFileResult readFile(String fileName, int offset, int dataLength);

	public StorageResult updateRecord(String recordName, int recordNo, String checkParams1, String checkParams2, byte[] content);

	public WriteFileResult writeFile(String fileName, int offset, byte[] content);
}
