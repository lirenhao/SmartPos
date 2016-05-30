package com.yada.smartpos.module.impl;

import android.content.Context;
import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.scanner.BarcodeScanner;
import com.newland.mtype.module.common.scanner.BarcodeScannerManager;
import com.newland.mtype.module.common.scanner.ScannerListener;
import com.yada.smartpos.module.ScannerModule;
import com.yada.smartpos.util.ModuleBase;

import java.util.concurrent.TimeUnit;

/**
 * 扫描模块接口实现
 */
public class ScannerModuleImpl extends ModuleBase implements ScannerModule {
	private BarcodeScanner scanner=null;

	public ScannerModuleImpl() {
	}

	//初始化
	public void initScan(Context context) {
		BarcodeScannerManager barcodeScannerManager = (BarcodeScannerManager) factory.getModule(ModuleType.COMMON_BARCODESCANNER);
		scanner = barcodeScannerManager.getDefault();
		scanner.initScanner(context);
	}

	//开始扫描
	public void startScan(long timeout, TimeUnit timeunit, ScannerListener listener) {
		if(scanner == null)
			throw new IllegalArgumentException("scanner should be inited!");
		scanner.startScan(timeout, timeunit, listener);
	}

	//停止扫描
	public void stopScanner() {
		if(scanner == null)
			throw new IllegalArgumentException("scanner should be inited!");
		scanner.stopScan();
	}
}
