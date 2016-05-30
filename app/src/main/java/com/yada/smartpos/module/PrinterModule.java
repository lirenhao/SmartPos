package com.yada.smartpos.module;

import android.graphics.Bitmap;
import com.newland.mtype.module.common.printer.*;
import com.yada.smartpos.event.PrinterListener;

import java.util.concurrent.TimeUnit;

public interface PrinterModule {
	public PrinterResult checkThenPrint(PrintContext printContext, byte[] data, long timeout, TimeUnit timeunit);

	public PrinterStatus getStatus();

	public void init();

	public void paperThrow(ThrowType type, int distance);// 打印机走纸指令

	public void printBitMap(int position, Bitmap bitmap);

	public PrinterResult printString(String data);

	public PrinterResult printScript(String data);
	
	public void printScript(String json, Bitmap[] bitmaps, PrinterListener listener);

	public void setDensity(int value);

	public void setFontType(LiteralType literalType, FontSettingScope settingScope, FontType fontType);

	public void setLineSpace(int value);

	public void setWordStock(WordStockType type);
}
