package com.yada.smartpos.util;

public class Const {
    /**
     * 密码输入完成
     */
    public static final int PIN_FINISH = 1;
    public static final String CHECK_MODULE = "checkedModule";

    public static class CheckedModuleName {
        public static final int TERNAL = 0;
        public static final int CARDREADER = 1;
        public static final int EMV = 2;

        public static final int ICCARD = 3;
        public static final int LIGHT = 4;
        public static final int CONSUME = 5;

        public static final int PIN = 6;
        public static final int PRINTER = 7;

        public static final int RFCARD = 8;
        public static final int SECURITY = 9;
        public static final int SCANNER = 10;
        public static final int SWIP = 11;
        public static final int STORAGE = 12;
    }

    /**
     * 主密钥索引
     * 各索引若相同则表示使用同一组主密钥索引
     */
    public static class MKIndexConst {

        // 主密钥索引
        public static final int DEFAULT_MK_INDEX = 1;
    }

    /**
     * dukpt索引
     */
    public static class DUKPTIndexConst {

        public static final int DEFAULT_DUKPT_INDEX = 1;
    }

    /**
     * 工作密钥类型:
     */
    public static class PinWKIndexConst {

        // 默认PIN加密工作密钥索引
        public static final int DEFAULT_PIN_WK_INDEX = 2;
        public static final int EXTERNAL_PIN_WK_INDEX = 0;
    }

    /**
     * 工作密钥类型:
     */
    public static class MacWKIndexConst {

        // 默认MAC加密工作密钥索引
        public static final int DEFAULT_MAC_WK_INDEX = 3;
        public static final int EXTERNAL_MAC_WK_INDEX = 1;
    }

    public static class DataEncryptWKIndexConst {

        // 默认磁道加密工作密钥索引
        public static final int DEFAULT_TRACK_WK_INDEX = 4;
        public static final int DEFAULT_MUTUALAUTH_WK_INDEX = 5;

    }

    /**
     * 操作提示信息类别
     */
    public static class MessageTag {

        // 正常消息
        public static final int NORMAL = 0;
        // 错误消息
        public static final int ERROR = 1;
        // 提示消息
        public static final int TIP = 2;
        // 数据
        public static final int DATA = 3;
    }

    /**
     * 设备参数存放相关规格
     */
    public static class DeviceParamsPattern {

        // 默认存放编码集
        public static final String DEFAULT_STORENCODING = "utf-8";

        // 日期格式化规格
        public static final String DEFAULT_DATEPATTERN = "yyyyMMddHHmmss";
    }

    /**
     * 设备参数<tt>tag</tt>
     */
    public static class DeviceParamsTag {

        // 商户号存放<tt>tag</tt>
        public static final int MRCH_NO = 0xFF9F11;
        // 终端号存放<tt>tag</tt>
        public static final int TRMNL_NO = 0xFF9F12;
        // 工作密钥存放<tt>tag</tt>
        public static final int WK_UPDATEDATE = 0xFF9F13;
        // pos标示存放<tt>tag</tt>
        public static final int DEVICE_TYPE = 0xFF9F14;
        // 终端号存放<tt>tag</tt>
        public static final int MRCH_NAME = 0xFF9F15;

    }

    public static class CardType {
        // 磁条卡
        public static final int COMMON = 0;
        // IC卡
        public static final int ICCARD = 1;
    }

    public static class DialogView {

        // Mac计算对话框
        public static final int MAC_CACL_DIALOG = 0;
        // 非接卡密钥对话框
        public static final int NC_CARD_KEY_DIALOG = 1;
        // IC卡对话框
        public static final int IC_CARD_ICCardSlot_DIALOG = 2;
    }
}
