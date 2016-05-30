package com.yada.smartpos.module;

import android.content.Context;
import com.newland.mtype.module.common.scanner.ScannerListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by HJP on 2015/8/12.
 */
public interface ScannerModule {
	public void stopScanner();

	public void initScan(Context context);

	public void startScan(long timeout, TimeUnit timeunit, ScannerListener listener);
}
