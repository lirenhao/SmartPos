package com.yada.smartpos.module.impl;

import android.graphics.Bitmap;
import com.alibaba.fastjson.JSONObject;
import com.newland.mtype.DeviceRTException;
import com.newland.mtype.ModuleType;
import com.newland.mtype.log.DeviceLogger;
import com.newland.mtype.log.DeviceLoggerFactory;
import com.newland.mtype.module.common.printer.*;
import com.yada.smartpos.event.PrinterListener;
import com.yada.smartpos.module.PrinterModule;
import com.yada.smartpos.util.ModuleBase;
import com.yada.smartpos.util.PrinterObj;
import com.yada.smartpos.util.PrinterObjs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 打印模块接口实现.
 */
public class PrinterModuleImpl extends ModuleBase implements PrinterModule {
    private Printer printer;
    private static DeviceLogger logger = DeviceLoggerFactory.getLogger(PrinterModuleImpl.class);

    public PrinterModuleImpl() {
        printer = (Printer) factory.getModule(ModuleType.COMMON_PRINTER);
        printer.init();
    }

    public PrinterResult checkThenPrint(PrintContext printContext, byte[] data, long timeout, TimeUnit timeunit) {
        PrinterResult reslt = printer.checkThenPrint(printContext, data, timeout, timeunit);
        return reslt;
    }

    @Override
    public PrinterStatus getStatus() {
        return printer.getStatus();
    }

    @Override
    public void init() {
        printer.init();
    }

    @Override
    public void paperThrow(ThrowType type, int distance) {
        printer.paperThrow(type, distance);
    }

    // 打印图片
    @Override
    public void printBitMap(int position, Bitmap bitmap) {
        printer.autoSetThreshold(false);
        printer.setBitmapThreshold(0xff);
        printer.print(position, bitmap, 30, TimeUnit.SECONDS);
    }

    // 打印信息，超时时间，超时时间单位
    @Override
    public PrinterResult printString(String data) {
        return printer.print(data, 30, TimeUnit.SECONDS);
    }

    // 打印脚本
    @Override
    public PrinterResult printScript(String data) {
        byte[] printData;
        try {
            printData = data.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new DeviceRTException(-1, "脚本执行失败!");
        }
        return printer.printByScript(PrintContext.defaultContext(), printData, 60, TimeUnit.SECONDS);
    }

    @Override
    public void setDensity(int value) {
        printer.setDensity(value);
    }

    @Override
    public void setFontType(LiteralType literalType, FontSettingScope settingScope, FontType fontType) {
        printer.setFontType(literalType, settingScope, fontType);
    }

    @Override
    public void setLineSpace(int value) {
        printer.setLineSpace(value);
    }

    @Override
    public void setWordStock(WordStockType type) {
        printer.setWordStock(type);
    }

    /**
     * 打印条形码
     * @param barCode 条形码内容；
     * @param i 打印的位置，0：左边，1中间，2右边
     * @return  PrinterResult SUCCESS打印成功
     */
    public PrinterResult printBarCode(String barCode, int i) {
        PrinterResult printerResult = null;
        try {
            switch (i) {
                case 0:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*barcode l " + barCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;
                case 1:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*barcode c " + barCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;
                case 2:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*barcode r " + barCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return printerResult;

    }

    /*
    打印二维码，
    @param qrCode:二维码内容；
    @param i打印的位置，0：左边，1中间，2右边
    @return PrinterResult SUCCESS打印成功
    */
    public PrinterResult printQrCode(String qrCode, int i) {
        PrinterResult printerResult = null;
        try {
            switch (i) {
                case 0:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*qrcode l " + qrCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;
                case 1:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*qrcode c " + qrCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;
                case 2:
                    printerResult = printer.printByScript(PrintContext.defaultContext(), ("*qrcode r " + qrCode + "\n").getBytes("GBK"), 60, TimeUnit.SECONDS);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return printerResult;

    }

    public static class PrinterErrCode {

        // 低压保护
        public static final int ERROR_LOWVOL = 0xE1;

        // 打印头过热
        public static final int ERROR_OVERHEAT = 0xF3;

        // 缺纸，不能打印
        public static final int ERROR_PAPERENDED = 0xF0;

        // 正常状态，无错误
        public static final int ERROR_NONE = 0x00;

        // 打印机处于忙状态
        public static final int ERROR_BUSY = 0xF7;

        public static final int ERROR_OTHER = 0xFF;

    }

    public static final int maxWidth = 384;

    // 打印脚本
    @Override
    public void printScript(String json, Bitmap[] bitmaps, PrinterListener listener) {

        try {
            if (json != null) {
                logger.debug(json);
            } else {
                logger.debug("无打印数据 json是null!");
                listener.onError(PrinterErrCode.ERROR_OTHER, "打印数据源为空！");
                return;
            }
            PrinterObjs objs = JSONObject.parseObject(json, PrinterObjs.class);
            if (objs.getSpos().size() == 0) {
                logger.debug("无打印数据!");
                listener.onError(PrinterErrCode.ERROR_OTHER, "打印数据源为空！");
                return;
            }
            int index = 0;
            listener.onStart();
            PrinterResult result = null;
            List<Object> values = new ArrayList<Object>();
            StringBuffer sb = new StringBuffer();
            for (PrinterObj obj : objs.getSpos()) {
                String scritp = obj.getScript();
                if (scritp == null) {
                    logger.debug("获取打印脚本失败!");
                    listener.onError(PrinterErrCode.ERROR_OTHER, "获取打印脚本失败！");
                    return;
                } else {
                    logger.debug(scritp);
                    if (scritp.equals(PrinterObj.BITMAP)) {
                        Bitmap bitmap = bitmaps[index++];
                        obj.setBitmap(bitmap);
                        if (PrinterObj.RIGHT.equals(obj.getPosition())) {
                            obj.setmOffset(maxWidth - bitmap.getWidth());
                        } else if (PrinterObj.CENTER.equals(obj.getPosition())) {
                            obj.setmOffset((maxWidth - bitmap.getWidth()) / 2);
                        }
                        if (sb.length() != 0) {
                            values.add(sb.toString());
                            sb = new StringBuffer();
                        }
                        values.add(obj);

                    } else {
                        if (scritp.startsWith("!barcode")) {
                            if (sb.length() != 0) {
                                values.add(sb.toString());
                                sb = new StringBuffer();
                            }
                            values.add(scritp);
                        } else {
                            sb.append(scritp);
                        }
                    }

                }
            }
            if (sb.length() != 0) {
                values.add(sb.toString());
            }

            for (Object obj : values) {
                if (obj instanceof String) {
                    logger.debug("脚本：" + (String) obj);
                    result = printer.printByScript(PrintContext.defaultContext(), ((String) obj).getBytes("gbk"), (long) 30, TimeUnit.SECONDS);
                } else {
                    printer.autoSetThreshold(false);
                    printer.setBitmapThreshold(200);
                    result = printer.print(((PrinterObj) obj).getmOffset(), ((PrinterObj) obj).getBitmap(), (long) 30, TimeUnit.SECONDS);
                }
                if (result != PrinterResult.SUCCESS) {
                    switch (result) {
                        case HEAT_LIMITED:
                            listener.onError(PrinterErrCode.ERROR_OVERHEAT, "打印头过热！");
                            break;
                        case OUTOF_PAPER:
                            listener.onError(PrinterErrCode.ERROR_PAPERENDED, "缺纸，不能打印！");
                            break;
                        default:
                            listener.onError(PrinterErrCode.ERROR_BUSY, "打印机处于忙状态！");
                            break;
                    }
                    return;
                }
            }
            listener.onFinish();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(PrinterErrCode.ERROR_OTHER, "打印数据失败！");
        }
    }

}
