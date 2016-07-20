package com.yada.smartpos.spos;

import com.payneteasy.tlv.*;
import com.yada.sdk.device.encryption.TerminalAuth;
import com.yada.sdk.device.pos.AbsTraner;
import com.yada.sdk.device.pos.ISequenceGenerator;
import com.yada.sdk.device.pos.posp.params.Block01;
import com.yada.sdk.device.pos.posp.params.Block02;
import com.yada.sdk.device.pos.posp.params.Block03;
import com.yada.sdk.net.FixLenPackageSplitterFactory;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.sdk.packages.transaction.IPacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Traner extends AbsTraner {

    private final static Logger LOGGER = LoggerFactory.getLogger(Traner.class);

    private CheckSignIn cs;
    private BerTlvParser tlvParser;

    public Traner(String merchantId, String terminalId, String tellerNo,
                  String batchNo, IPacker packer,
                  String serverIp, int serverPort, int timeout,
                  CheckSignIn cs, TerminalAuth terminalAuth,
                  ISequenceGenerator traceNoSeqGenerator,
                  ISequenceGenerator cerNoSeqGenerator,
                  LinkedBlockingQueue<IMessage> queue) throws IOException {
        super(merchantId, terminalId, tellerNo, batchNo,
                new FixLenPackageSplitterFactory(2, false), packer,
                serverIp, serverPort, timeout, terminalAuth, traceNoSeqGenerator,
                cerNoSeqGenerator, queue);
        this.cs = cs;
        this.tlvParser = new BerTlvParser();
    }

    /**
     * 签到
     *
     * @throws PackagingException
     * @throws IOException
     */
    public SigninInfo singIn() throws PackagingException, IOException {
        IMessage reqMessage = createMessage();
        reqMessage.setFieldString(0, "0800");
        reqMessage.setFieldString(3, "990000");
        reqMessage.setFieldString(11, getTraceNo());
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(41, getTerminalId());
        reqMessage.setFieldString(42, getMerchantId());
        reqMessage.setFieldString(61, getBatchNo() + "001");

        IMessage respMessage = sendTran(reqMessage);

        // TODO field56Handle(respMessage);

        String temp = new String(respMessage.getField(48).array(), Charset.forName("GBK"));

        //返回参数数据继续发送签到交易直到返回密钥为止
        while (temp.substring(0, 2).equals("97")) {
            respMessage = sendTran(reqMessage);
            temp = new String(respMessage.getField(48).array(), Charset.forName("GBK"));
        }

        SigninInfo si = new SigninInfo();
        si.batchNo = respMessage.getFieldString(61).substring(0, 6);

        String field48 = new String(respMessage.getField(48).array(), Charset.forName("GBK"));
        String tag, len, value;
        int index = 0;

        while (index < field48.length()) {
            tag = field48.substring(index, index + 2);
            len = field48.substring(index + 2, index + 2 + 2);
            int ilen = Integer.parseInt(len);
            value = field48.substring(index + 2 + 2, index + 2 + 2 + ilen);
            index = index + 2 + 2 + ilen;
            if (tag.equals("98")) {
                si.tmkTpk = getStringKey(value);
            }

            if (tag.equals("99")) {
                si.tmkTak = getStringKey(value);
            }
        }

        return si;
    }

    //解48域密钥
    private String getStringKey(String value) {
        String key;
        if (value.length() == 23) key = value.substring(1, 17);
        else {
            key = value.substring(1, 33);
        }
        return key;
    }

    //参数下载
    public ParamInfo paramDownload() throws PackagingException, IOException {
        ParamInfo paramInfo = new ParamInfo();

        paramInfo.setBlock01(paramDownloadHandle1());
        // 第二块参数下载用于TMS扩展参数下载，暂不去查询
//        paramInfo.setBlock02(paramDownloadHandle2("2020080101000000"));
//        paramInfo.setBlock03Map(paramDownloadHandle3());
//        Map<String, String> ridParams = paramDownloadHandle4();
//        Map<String, Block04_1> block04_1Map = new HashMap<>();
//        Map<String, Block04_2> block04_2Map = new HashMap<>();
//        for (String rid : ridParams.keySet()) {
//            long df28 = Long.parseLong(rid.substring(10), 16);
//            if (df28 < 7) {
//                // 国际公钥
//                Block04_1 block04_1 = new Block04_1(HexUtil.parseHex(ridParams.get(rid)));
//                block04_1Map.put(rid, block04_1);
//            } else {
//                // 国密公钥
//                Block04_2 block04_2 = new Block04_2(HexUtil.parseHex(ridParams.get(rid)));
//                block04_2Map.put(rid, block04_2);
//            }
//        }
//        paramInfo.setBlock04_1Map(block04_1Map);
//        paramInfo.setBlock04_2Map(block04_2Map);

        return paramInfo;
    }

    /**
     * 参数下载第一块参数
     *
     * @return 返回终端基本参数
     * @throws PackagingException
     * @throws IOException
     */
    public Block01 paramDownloadHandle1()
            throws PackagingException, IOException {
        IMessage reqMessage = createMessage();
        reqMessage.setFieldString(0, "0800");
        reqMessage.setFieldString(3, "990000");
        reqMessage.setFieldString(11, getTraceNo());
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(41, getTerminalId());
        reqMessage.setFieldString(42, getMerchantId());
        reqMessage.setFieldString(61, getBatchNo() + "090");

        IMessage respMessage = sendTran(reqMessage);

        return new Block01(respMessage.getFieldString(48).substring(5));
    }

    /**
     * 参数下载第二块参数下装程序用参数
     * TMS扩展的参数
     *
     * @param version 参数版本号
     * @return 下装程序用参数 DF29 DF25
     * @throws PackagingException
     * @throws IOException
     */
    public Block02 paramDownloadHandle2(String version)
            throws PackagingException, IOException {
        IMessage reqMessage = createMessage();
        reqMessage.setFieldString(0, "0800");
        reqMessage.setFieldString(3, "990000");
        reqMessage.setFieldString(11, getTraceNo());
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(41, getTerminalId());
        reqMessage.setFieldString(42, getMerchantId());
        reqMessage.setFieldString(61, getBatchNo() + "093");

        BerTlvBuilder builder = new BerTlvBuilder();
        builder.addHex(new BerTag(0xdf, 0x25), version);
        reqMessage.setFieldString(56, HexUtil.toHexString(builder.buildArray()));

        IMessage respMessage = sendTran(reqMessage);
        return new Block02(HexUtil.parseHex(respMessage.getFieldString(56)));
    }

    /**
     * AID应用参数版本查询
     *
     * @param df27value 参数下装报文索引号
     * @return aid、参数版本号列表
     */
    public Map<String, String> aidQueryHandle(String df27value)
            throws PackagingException, IOException {
        List<String> df26s = new ArrayList<>();
        do {
            IMessage reqMessage = createMessage();

            reqMessage.setFieldString(0, "0800");
            reqMessage.setFieldString(3, "990000");
            reqMessage.setFieldString(11, getTraceNo());
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(61, getBatchNo() + "091");

            BerTlvBuilder builder = new BerTlvBuilder();
            builder.addHex(new BerTag(0xdf, 0x27), df27value);
            reqMessage.setFieldString(56, HexUtil.toHexString(builder.buildArray()));

            IMessage respMessage = sendTran(reqMessage);

            if (respMessage.getFieldString(39).equals("00") && null != respMessage.getFieldString(56)) {
                BerTlvs tlv56 = tlvParser.parse(HexUtil.parseHex(respMessage.getFieldString(56)));
                // DF26中包含的是应用列表
                df26s.add(tlv56.find(new BerTag(0xdf, 0x26)).getHexValue());
                // DF27中包含的是参数下装报文索引号
                df27value = tlv56.find(new BerTag(0xdf, 0x27)).getHexValue();
                // 56域中返回的AID参数版本查询结果数据格式为： DF26 DF27
                // DF26中包含的是应用列表，DF27中包含的是参数下装报文索引号
                // 如果DF27所对应的值不为0，表示该终端还有应用列表为下装完，需要终端自动继续发出AID应用参数版本查询请求报文，并带上IST返回的DF27
            } else {
                df27value = "00";
            }
        } while (!df27value.equals("00"));

        Map<String, String> aids = new HashMap<>();
        for (String df26 : df26s) {
            BerTlvs tlvDF26 = tlvParser.parse(HexUtil.parseHex(df26));
            // 外层循环是AID查询了几次，内层循环是一次查询返回的DF26的处理
            for (int i = 0; i < tlvDF26.getList().size(); i = i + 2) {
                // TODO 判断版本是否过期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDDHHmmss");
                aids.put(tlvDF26.getList().get(i).getHexValue(), tlvDF26.getList().get(i + 1).getHexValue());
            }
        }

        return aids;
    }

    /**
     * 参数下载第三块参数(被动参数下载)
     *
     * @param aids      要查询的AID列表
     * @param df27value 参数下装报文索引号
     * @return 应用参数 9F06、DF01、9F09、DF11、DF12、DF13、9F1B、DF15、DF16、DF17、DF14、DF18、9F35、9F15、DF25、9F7B、DF40、DF20、DF21
     */
    public Map<String, Block03> paramDownloadHandle3(Map<String, String> aids, String df27value)
            throws IOException, PackagingException {
        Map<String, Block03> params = new HashMap<>();

        // 当df27不为零时接着查询剩余的aid列表
        if (!df27value.equals("00")) {
            Map<String, String> otherAids = aidQueryHandle(df27value);
            for (String aid : otherAids.keySet()) {
                aids.put(aid, otherAids.get(aid));
            }
        }
        // 查询AID列表中对应AID的参数
        for (String aid : aids.keySet()) {
            IMessage reqMessage = createMessage();
            reqMessage.setFieldString(0, "0800");
            reqMessage.setFieldString(3, "990000");
            reqMessage.setFieldString(11, getTraceNo());
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(61, getBatchNo() + "093");

            BerTlvBuilder builder = new BerTlvBuilder();
            builder.addHex(new BerTag(0x9f, 0x06), aid);
            builder.addHex(new BerTag(0xdf, 0x25), aids.get(aid));
            reqMessage.setFieldString(56, HexUtil.toHexString(builder.buildArray()));

            IMessage respMessage = sendTran(reqMessage);
            params.put(aid, new Block03(HexUtil.parseHex(respMessage.getFieldString(56))));
        }
        return params;
    }

    /**
     * 参数下载第三块参数(主动参数下载)
     *
     * @return AID应用参数
     */
    public Map<String, Block03> paramDownloadHandle3() throws IOException, PackagingException {
        String df27value = "00";
        Map<String, String> aids = aidQueryHandle(df27value);
        return paramDownloadHandle3(aids, df27value);
    }

    /**
     * 公钥参数版本查询
     *
     * @return rid、参数版本列表
     */
    public Map<String, String> ridQueryHandle() throws PackagingException, IOException {
        Map<String, String> rids = new HashMap<>();

        IMessage reqMessage = createMessage();

        reqMessage.setFieldString(0, "0800");
        reqMessage.setFieldString(3, "990000");
        reqMessage.setFieldString(11, getTraceNo());
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(41, getTerminalId());
        reqMessage.setFieldString(42, getMerchantId());
        reqMessage.setFieldString(61, getBatchNo() + "092");

        IMessage respMessage = sendTran(reqMessage);

        BerTlv tlv56 = tlvParser.parseConstructed(HexUtil.parseHex(respMessage.getFieldString(56)));
        BerTlvs tlvDF24 = tlvParser.parse(tlv56.getBytesValue());

        for (int i = 0; i < tlvDF24.getList().size(); i = i + 2) {
            // TODO 判断版本是否过期
            rids.put(tlvDF24.getList().get(i).getHexValue(), tlvDF24.getList().get(i + 1).getHexValue());
        }

        return rids;
    }

    /**
     * 参数下载第四块参数(被动参数下载)
     * 公钥的参数版本号列表
     *
     * @param rids rid、参数版本号列表
     * @return 公钥参数
     */
    public Map<String, String> paramDownloadHandle4(Map<String, String> rids)
            throws PackagingException, IOException {
        Map<String, String> params = new HashMap<>();

        for (String rid : rids.keySet()) {
            IMessage reqMessage = createMessage();
            reqMessage.setFieldString(0, "0800");
            reqMessage.setFieldString(3, "990000");
            reqMessage.setFieldString(11, getTraceNo());
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(61, getBatchNo() + "093");

            // 每个rid有六个密钥，需要循环六次
            for (int i = 1; i <= 6; i++) {
                BerTlvBuilder builder = new BerTlvBuilder();
                builder.addHex(new BerTag(0x9f, 0x06), rid);
                builder.addHex(new BerTag(0xdf, 0x25), rids.get(rid));
                builder.addHex(new BerTag(0xdf, 0x28), "0" + i);
                reqMessage.setFieldString(56, HexUtil.toHexString(builder.buildArray()));

                IMessage respMessage = sendTran(reqMessage);
                params.put(rid + "0" + i, respMessage.getFieldString(56));
            }

            // A000000333是银联的RID
            if (rid.equals("A000000333")) {
                // 银联国密存储在7-12位
                for (int i = 7; i <= 12; i++) {
                    String df28;
                    switch (i) {
                        case 10:
                            df28 = "0A";
                            break;
                        case 11:
                            df28 = "0B";
                            break;
                        case 12:
                            df28 = "0C";
                            break;
                        default:
                            df28 = "0" + i;
                    }
                    BerTlvBuilder builder = new BerTlvBuilder();
                    builder.addHex(new BerTag(0x9f, 0x06), rid);
                    builder.addHex(new BerTag(0xdf, 0x25), rids.get(rid));
                    builder.addHex(new BerTag(0xdf, 0x28), df28);
                    reqMessage.setFieldString(56, HexUtil.toHexString(builder.buildArray()));

                    IMessage respMessage = sendTran(reqMessage);
                    params.put(rid + df28, respMessage.getFieldString(56));
                }
            }
        }
        return params;
    }

    /**
     * 参数下载第四块参数(主动参数下载)
     * 公钥的参数版本号列表
     *
     * @return 公钥参数
     */
    public Map<String, String> paramDownloadHandle4()
            throws PackagingException, IOException {
        Map<String, String> rids = ridQueryHandle();
        return paramDownloadHandle4(rids);
    }

    public void field56Handle(IMessage responseMessage) throws IOException, PackagingException {
        if (null != responseMessage.getFieldString(56) && !"".equals(responseMessage.getFieldString(56))) {
            BerTlvs tlv56 = tlvParser.parse(HexUtil.parseHex(responseMessage.getFieldString(56)));

            BerTlv tlvDF25 = tlv56.find(new BerTag(0xdf, 0x25));
            if (null != tlvDF25) {
                paramDownloadHandle2(tlvDF25.getHexValue());
            }

            BerTlv tlvDF26 = tlv56.find(new BerTag(0xdf, 0x26));
            BerTlv tlvDF27 = tlv56.find(new BerTag(0xdf, 0x27));
            if (null != tlvDF26 && null != tlvDF27) {
                Map<String, String> aids = new HashMap<>();
                BerTlvs tlvs = tlvParser.parse(tlvDF26.getBytesValue());
                for (int i = 0; i < tlvs.getList().size(); i = i + 2) {
                    // TODO 判断版本是否过期
                    aids.put(tlvs.getList().get(i).getHexValue(), tlvs.getList().get(i + 1).getHexValue());
                }
                paramDownloadHandle3(aids, tlvDF27.getHexValue());
            }

            BerTlv tlvDF24 = tlv56.find(new BerTag(0xdf, 0x24));
            if (null != tlvDF24) {
                Map<String, String> rids = new HashMap<>();
                BerTlvs tlvs = tlvParser.parse(tlvDF24.getBytesValue());
                for (int i = 0; i < tlvs.getList().size(); i = i + 2) {
                    // TODO 判断版本是否过期
                    rids.put(tlvs.getList().get(i).getHexValue(), tlvs.getList().get(i + 1).getHexValue());
                }
                paramDownloadHandle4(rids);
            }
        }
    }

    /**
     * 余额查询
     *
     * @param cardNo          卡号
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param icCardData      IC卡数据域
     * @return 返回交易响应的message
     */
    public IMessage query(String cardNo, String validity, String posInputType, String sequenceNumber,
                          String secondTrackData, String thirdTrackData, String pin, String icCardData) {
        String processCode = "310008";
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;

        try {
            IMessage reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);// 主帐号
            reqMessage.setFieldString(3, processCode);// 处理码
            reqMessage.setFieldString(11, traceNo);// 系统跟踪号
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);// POS输入方式
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            }
            reqMessage.setFieldString(41, getTerminalId());// 终端号
            reqMessage.setFieldString(42, getMerchantId());// 商户号
            reqMessage.setFieldString(49, currency);// 货币代码
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, pin);// 个人识别码
            }
            if (null != icCardData && !"".equals(icCardData)) {
                reqMessage.setFieldString(55, icCardData);
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, null, traceNo, null, null, currency, null, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 消费
     *
     * @param cardNo          卡号
     * @param amt             消费金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param icCardData      IC卡数据域
     * @return 返回交易响应的message
     */
    public IMessage pay(String cardNo, String amt, String validity, String posInputType,
                        String sequenceNumber, String secondTrackData, String thirdTrackData,
                        String pin, String icCardData) {
        String processCode = "000008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);
            reqMessage.setFieldString(3, processCode);
            reqMessage.setFieldString(4, formatAmt);
            reqMessage.setFieldString(11, traceNo);
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);
            }
            reqMessage.setFieldString(22, posInputType);
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);
            }
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(25, "14");
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);
            }
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(49, currency);
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, getPin(cardNo, pin));
            }
            if (null != icCardData && !"".equals(icCardData)) {
                reqMessage.setFieldString(55, icCardData);
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, null, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when pay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when pay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 撤销
     *
     * @param cardNo          卡号
     * @param amt             撤销金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @return 返回交易响应的message
     */
    public IMessage revoke(String cardNo, String amt, String validity, String posInputType,
                           String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                           String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime) {
        String processCode = "200008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);// 主帐号
            reqMessage.setFieldString(3, processCode);// 处理码
            reqMessage.setFieldString(4, formatAmt);// 交易金额
            reqMessage.setFieldString(11, traceNo);// 系统跟踪号
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);// POS输入方式
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            }
            reqMessage.setFieldString(38, oldAuthCode);// 原消费交易授权号
            reqMessage.setFieldString(41, getTerminalId());// 终端号
            reqMessage.setFieldString(42, getMerchantId());// 商户号
            reqMessage.setFieldString(49, currency);// 货币代码
            if (null != pin && !"".equals(pin))
                reqMessage.setFieldString(52, pin);// 个人识别码
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setFieldString(62, "0200" + oldTraceNo
                    + oldTransDate + oldTransTime);// 自定义域 信息类型码+系统跟踪号+交易日期和时间
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, oldAuthCode, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 退货
     *
     * @param cardNo          卡号
     * @param amt             退货金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @return 返回交易响应的message
     */
    public IMessage refund(String cardNo, String amt, String validity, String posInputType,
                           String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                           String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime) {
        String processCode = "270008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0220");
            reqMessage.setFieldString(2, cardNo);// 主帐号
            reqMessage.setFieldString(3, processCode);// 处理码
            reqMessage.setFieldString(4, formatAmt);// 交易金额
            reqMessage.setFieldString(11, traceNo);// 系统跟踪号
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);// POS输入方式
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS
            if (null != secondTrackData && !"".equals(secondTrackData))
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            if (null != thirdTrackData && !"".equals(thirdTrackData))
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            reqMessage.setFieldString(38, oldAuthCode);// 原消费交易授权号
            reqMessage.setFieldString(41, getTerminalId());// 终端号
            reqMessage.setFieldString(42, getMerchantId());// 商户号
            reqMessage.setFieldString(49, "156");// 货币代码
            if (null != pin && !"".equals(pin))
                reqMessage.setFieldString(52, pin);// 个人识别码
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setFieldString(62, "0200" + oldTraceNo
                    + oldTransDate + oldTransTime);// 自定义域 信息类型码+系统跟踪号+交易日期和时间
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, oldAuthCode, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            //退货不冲正
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 分期消费
     *
     * @param cardNo          卡号
     * @param amt             消费金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param icCardData      IC卡数据域
     * @param stagesId        分期交易ID
     * @param stagesCount     分期期数
     * @param stagesType      分期类型
     * @return
     */
    public IMessage stagesPay(String cardNo, String amt, String validity, String posInputType, String sequenceNumber,
                              String secondTrackData, String thirdTrackData, String pin, String icCardData,
                              String stagesId, int stagesCount, String stagesType) {
        String processCode = "000008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);
            reqMessage.setFieldString(3, processCode);
            reqMessage.setFieldString(4, formatAmt);
            reqMessage.setFieldString(11, traceNo);
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(25, "14");
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);
            }
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());

            String field48 = "9003" + stagesType + "9106" + stagesId + String.format("%02d", stagesCount);
            reqMessage.setFieldString(48, field48);
            reqMessage.setFieldString(49, currency);
            reqMessage.setFieldString(52, getPin(cardNo, pin));
            if (null != icCardData && !"".equals(icCardData)) {
                reqMessage.setFieldString(55, icCardData);
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, null, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 分期退货
     *
     * @param cardNo          卡号
     * @param amt             撤销金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @param stagesId        分期交易ID
     * @param stagesCount     分期期数
     * @return
     */
    public IMessage stagesRevoke(String cardNo, String amt, String validity, String posInputType,
                                 String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                                 String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime,
                                 String stagesId, String stagesCount) {
        String processCode = "200008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);
            reqMessage.setFieldString(3, processCode);
            reqMessage.setFieldString(4, formatAmt);
            reqMessage.setFieldString(11, traceNo);
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS

            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            }
            reqMessage.setFieldString(38, oldAuthCode);// 原消费交易授权号
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());

            // 使用90子域，值为”905”；还需要使用91子域以上送期数与PLAN ID
            String field48 = "9003905" + "9106" + stagesId + String.format("%02d", stagesCount);
            reqMessage.setFieldString(48, field48);
            reqMessage.setFieldString(49, currency);
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, pin);// 个人识别码
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setFieldString(62, "0200" + oldTraceNo
                    + oldTransDate + oldTransTime);// 自定义域 信息类型码+系统跟踪号+交易日期和时间
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, oldAuthCode, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 分期退货
     *
     * @param cardNo          卡号
     * @param amt             退货金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @param stagesId        分期交易ID
     * @param stagesCount     分期期数
     * @return 返回交易响应的message
     */
    public IMessage stagesRefund(String cardNo, String amt, String validity, String posInputType,
                                 String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                                 String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime,
                                 String stagesId, String stagesCount) {
        String processCode = "270008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0220");
            reqMessage.setFieldString(2, cardNo);// 主帐号
            reqMessage.setFieldString(3, processCode);// 处理码
            reqMessage.setFieldString(4, formatAmt);// 交易金额
            reqMessage.setFieldString(11, traceNo);// 系统跟踪号
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);// 卡有效期
            }
            reqMessage.setFieldString(22, posInputType);// POS输入方式
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            }
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS
            if (null != secondTrackData && !"".equals(secondTrackData))
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            if (null != thirdTrackData && !"".equals(thirdTrackData))
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            reqMessage.setFieldString(38, oldAuthCode);// 原消费交易授权号
            reqMessage.setFieldString(41, getTerminalId());// 终端号
            reqMessage.setFieldString(42, getMerchantId());// 商户号
            // 使用90子域，值为”905”；还需要使用91子域以上送期数与PLAN ID
            reqMessage.setFieldString(48, "90039059106" + stagesId + stagesCount);
            reqMessage.setFieldString(49, "156");// 货币代码
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, pin);// 个人识别码
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setFieldString(62, "0200" + oldTraceNo
                    + oldTransDate + oldTransTime);// 自定义域 信息类型码+系统跟踪号+交易日期和时间
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, oldAuthCode, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            //退货不冲正
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 预授权
     *
     * @param cardNo          卡号
     * @param amt             消费金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param icCardData      IC卡数据域
     * @return 返回交易响应的message
     */
    public IMessage preAuth(String cardNo, String amt, String validity, String posInputType,
                            String sequenceNumber, String secondTrackData, String thirdTrackData,
                            String pin, String icCardData) {
        String processCode = "000008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0100");
            reqMessage.setFieldString(2, cardNo);
            reqMessage.setFieldString(3, processCode);
            reqMessage.setFieldString(4, formatAmt);
            reqMessage.setFieldString(11, traceNo);
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);
            }
            reqMessage.setFieldString(22, posInputType);
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);
            }
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(25, "14");
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);
            }
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(49, currency);
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, pin);
            }
            if (null != icCardData && !"".equals(icCardData)) {
                reqMessage.setFieldString(55, icCardData);
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, null, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 预授权撤销
     *
     * @param cardNo          卡号
     * @param amt             撤销金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @return 返回交易响应的message
     */
    public IMessage preAuthRevoke(String cardNo, String amt, String validity, String posInputType,
                                  String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                                  String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime) {
        String processCode = "200008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0100");
            reqMessage.setFieldString(2, cardNo);// 主帐号
            reqMessage.setFieldString(3, processCode);// 处理码
            reqMessage.setFieldString(4, formatAmt);// 交易金额
            reqMessage.setFieldString(11, traceNo);// 系统跟踪号
            if (null != validity && !"".equals(validity))
                reqMessage.setFieldString(14, validity);// 卡有效期
            reqMessage.setFieldString(22, posInputType);// POS输入方式
            if (null != sequenceNumber && !"".equals(sequenceNumber))
                reqMessage.setFieldString(23, sequenceNumber);// 卡片序列号
            reqMessage.setFieldString(24, "009");// NII
            reqMessage.setFieldString(25, "14");// 服务点条件码 14代表POS
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);// 二磁道数据
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);// 三磁道数据
            }
            reqMessage.setFieldString(38, oldAuthCode);// 原消费交易授权号
            reqMessage.setFieldString(41, getTerminalId());// 终端号
            reqMessage.setFieldString(42, getMerchantId());// 商户号
            reqMessage.setFieldString(49, "156");// 货币代码
            if (null != pin && !"".equals(pin))
                reqMessage.setFieldString(52, pin);// 个人识别码
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());// 自定义域 交易批次号+操作员号+票据号
            reqMessage.setFieldString(62, "0200" + oldTraceNo
                    + oldTransDate + oldTransTime);// 自定义域 信息类型码+系统跟踪号+交易日期和时间
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, oldAuthCode, null)));

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 预授权完成
     *
     * @param cardNo          卡号
     * @param amt             消费金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @return 返回交易响应的message
     */
    public IMessage preAuthComplete(String cardNo, String amt, String validity, String posInputType,
                                    String sequenceNumber, String secondTrackData, String thirdTrackData, String pin) {
        String processCode = "000008";
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0200");
            reqMessage.setFieldString(2, cardNo);
            reqMessage.setFieldString(3, processCode);
            reqMessage.setFieldString(4, formatAmt);
            reqMessage.setFieldString(11, traceNo);
            if (null != validity && !"".equals(validity)) {
                reqMessage.setFieldString(14, validity);
            }
            reqMessage.setFieldString(22, posInputType);
            if (null != sequenceNumber && !"".equals(sequenceNumber)) {
                reqMessage.setFieldString(23, sequenceNumber);
            }
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(25, "14");
            if (null != secondTrackData && !"".equals(secondTrackData)) {
                reqMessage.setFieldString(35, secondTrackData);
            }
            if (null != thirdTrackData && !"".equals(thirdTrackData)) {
                reqMessage.setFieldString(36, thirdTrackData);
            }
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(49, currency);
            if (null != pin && !"".equals(pin)) {
                reqMessage.setFieldString(52, pin);
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
            reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, null, null)));

            respMessage = sendTran(reqMessage);
            // 39域返回Z9向IST发送预授权完成通知
            if ("Z9".equals(respMessage.getFieldString(39))) {
                IMessage noticeMessage = preAuthCompleteNotice(reqMessage,
                        respMessage.getFieldString(12), respMessage.getFieldString(13));
                if (null != noticeMessage) {
                    respMessage = noticeMessage;
                } else {
                    respMessage.setFieldString(39, "00");
                }
            }

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    /**
     * 预授权完成撤销
     *
     * @param cardNo          卡号
     * @param amt             撤销金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param oldAuthCode     原消费交易授权号
     * @param oldTraceNo      原交易系统跟踪号
     * @param oldTransDate    原交易的交易日期
     * @param oldTransTime    原交易的交易时间
     * @return 返回交易响应的message
     */
    public IMessage preAuthCompleteRevoke(String cardNo, String amt, String validity, String posInputType,
                                          String sequenceNumber, String secondTrackData, String thirdTrackData, String pin,
                                          String oldAuthCode, String oldTraceNo, String oldTransDate, String oldTransTime) {
        return revoke(cardNo, amt, validity, posInputType, sequenceNumber, secondTrackData,
                thirdTrackData, pin, oldAuthCode, oldTraceNo, oldTransDate, oldTransTime);
    }

    /**
     * 预授权完成通知
     * 1.	主机将会为预授权完成通知交易检查原始的预授权交易，如果查不到原交易则主机将拒绝
     * 2.	预授权完成通知交易不能冲正，也不能被撤消
     * 3.	预授权完成通知交易不是操作员选择发出，而是由POS根据主机对预授权完成交易的响应报文的指令发出。
     * 如果IST用响应码Z9拒绝了预授权完成报文，表示此笔完成必须使用通知报文，此时POS需要根据原预授权完成报文的相关信息
     * ，组织预授权完成通知报文重新上送到IST，如果在第一次上送完成通知报文时，收到了IST的成功响应，就认为交易成功，如
     * 果交易被IST拒绝就认为交易失败。如果POS没有收到IST对完成通知的响应报文，就认为该通知交易成功（打印签购单）但需
     * 要在下笔交易前将此通知报文重新上送IST直到收到IST的应答报文。
     *
     * @param completeMessage 预授权完成的请求报文
     * @return 预授权完成通知返回的报文
     */
    public IMessage preAuthCompleteNotice(IMessage completeMessage, String transTime, String transDate) {

        String traceNo = getTraceNo();
        IMessage respMessage = null;
        IMessage reqMessage;
        try {
            reqMessage = createMessage();
            reqMessage.setFieldString(0, "0220");
            reqMessage.setFieldString(2, completeMessage.getFieldString(2));
            reqMessage.setFieldString(3, completeMessage.getFieldString(3));
            reqMessage.setFieldString(4, completeMessage.getFieldString(4));
            reqMessage.setFieldString(11, getTraceNo());
            reqMessage.setFieldString(12, transTime);
            reqMessage.setFieldString(13, transDate);
            if (completeMessage.getFieldString(14) != null) {
                reqMessage.setFieldString(14, completeMessage.getFieldString(14));
            }
            reqMessage.setFieldString(22, completeMessage.getFieldString(22));
            if (completeMessage.getFieldString(23) != null) {
                reqMessage.setFieldString(23, completeMessage.getFieldString(23));
            }
            reqMessage.setFieldString(24, completeMessage.getFieldString(24));
            reqMessage.setFieldString(25, completeMessage.getFieldString(25));
            if (completeMessage.getFieldString(35) != null) {
                reqMessage.setFieldString(35, completeMessage.getFieldString(35));
            }
            if (completeMessage.getFieldString(36) != null) {
                reqMessage.setFieldString(36, completeMessage.getFieldString(36));
            }
            if (completeMessage.getFieldString(38) != null) {
                reqMessage.setFieldString(38, completeMessage.getFieldString(38));
            }
            reqMessage.setFieldString(41, completeMessage.getFieldString(41));
            reqMessage.setFieldString(42, completeMessage.getFieldString(42));
            if (completeMessage.getFieldString(48) != null) {
                reqMessage.setFieldString(48, completeMessage.getFieldString(48));
            }
            reqMessage.setFieldString(49, completeMessage.getFieldString(49));
            if (completeMessage.getFieldString(52) != null) {
                reqMessage.setFieldString(52, completeMessage.getFieldString(52));
            }
            if (completeMessage.getFieldString(56) != null) {
                reqMessage.setFieldString(56, completeMessage.getFieldString(56));
            }
            reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());

            StringBuilder macData = new StringBuilder();
            macData.append(completeMessage.getFieldString(2).length() % 2 == 0 ? completeMessage.getFieldString(2) : "0" + completeMessage.getFieldString(2));
            macData.append(completeMessage.getFieldString(3));
            macData.append(completeMessage.getFieldString(4));
            macData.append(traceNo);
            macData.append("0").append(completeMessage.getFieldString(49));

            byte[] bcdMacData = HexUtil.parseHex(macData.toString());
            byte[] terminalByte = getTerminalId().getBytes();

            ByteBuffer buf = ByteBuffer.allocate(bcdMacData.length + terminalByte.length);
            buf.put(bcdMacData).put(terminalByte);

            reqMessage.setField(64, getMac(packMacData(reqMessage.getFieldString(2), reqMessage.getFieldString(3),
                    reqMessage.getFieldString(4), reqMessage.getFieldString(11), reqMessage.getFieldString(12),
                    reqMessage.getFieldString(13), reqMessage.getFieldString(49), reqMessage.getFieldString(38), null)));

            respMessage = sendTran(reqMessage);

            // TODO 通知未返回的处理，存储等待下笔交易上送
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 优惠商户
     *
     * @param cardNo          卡号
     * @param amt             消费金额
     * @param validity        效期
     * @param posInputType    POS输入方式901|051
     * @param sequenceNumber  卡片序列号
     * @param secondTrackData 二磁道数据
     * @param thirdTrackData  三磁道数据
     * @param pin             PIN码
     * @param icCardData      IC卡数据域
     * @return 返回交易响应的message
     */
    public IMessage specialPay(String cardNo, String amt, String validity, String posInputType,
                               String sequenceNumber, String secondTrackData, String thirdTrackData,
                               String pin, String icCardData) {
        String processCode = "840008";
        IMessage respMessage = null;
        IMessage specialMessage = specialPay(cardNo, amt);
        if (null != specialMessage && "00".equals(specialMessage.getFieldString(39))){
            String field54 = specialMessage.getFieldString(54);
            BigDecimal amount = new BigDecimal(amt);
            BigDecimal specialAmt = new BigDecimal(0);
            BigDecimal limitAmt = new BigDecimal(0);
            BigDecimal specialRate = new BigDecimal(1);
            String tag, value;
            int index = 0;
            while (index < field54.length()) {
                tag = field54.substring(index + 2, index + 4);
                value = field54.substring(index + 2 + 2 + 4, index + 2 + 2 + 4 + 12);
                index = index + 20;
                // 折让金额
                if (tag.equals("06")) {
                    specialAmt = new BigDecimal(value);
                }
                // 折扣上限
                if (tag.equals("07")) {
                    limitAmt = new BigDecimal(value);
                }
                // 折扣率 0000+小数点位数(1位)+折扣(7位)
                if (tag.equals("09")) {
                    int point = Integer.parseInt(value.substring(4, 5));
                    specialRate = new BigDecimal(value.substring(5)).movePointLeft(point);
                }
                // TODO 折扣上限、折扣率怎么用
            }
            respMessage = pay(cardNo, amount.subtract(specialAmt).toString(), validity, posInputType,
                    sequenceNumber, secondTrackData, thirdTrackData, pin, icCardData);
            if (null == respMessage || !"00".equals(respMessage.getFieldString(39))){
                // 普惠消费冲正
                try {
                    addElementToQueue(createSpecialMessage(cardNo, processCode, amt));
                } catch (PackagingException e) {
                    LOGGER.debug("when stagesPay happen PackagingException", e);
                }
            }
        }
        return respMessage;
    }

    /**
     * 普惠查询
     *
     * @param cardNo 卡号
     * @param amt    原始金额
     * @return
     */

    public IMessage specialQuery(String cardNo, String amt) {
        // TODO 普惠查询是否有用
        String processCode = "830008";
        IMessage respMessage = null;
        try {
            IMessage reqMessage = createSpecialMessage(cardNo, processCode, amt);
            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 普惠消费
     *
     * @param cardNo 卡号
     * @param amt    原始金额
     * @return
     */
    public IMessage specialPay(String cardNo, String amt) {
        String processCode = "840008";
        IMessage respMessage = null;
        IMessage reqMessage = null;
        try {
            reqMessage = createSpecialMessage(cardNo, processCode, amt);
            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
            addElementToQueue(reqMessage);
        }
        return respMessage;
    }

    private IMessage createSpecialMessage(String cardNo, String processCode, String amt) throws PackagingException {
        String formatAmt = String.format("%12s", amt).replace(' ', '0');
        String traceNo = getTraceNo();
        String currency = "156";

        IMessage reqMessage = createMessage();
        reqMessage.setFieldString(0, "0200");
        reqMessage.setFieldString(2, cardNo);
        reqMessage.setFieldString(3, processCode);
        reqMessage.setFieldString(4, formatAmt);
        reqMessage.setFieldString(11, traceNo);
        reqMessage.setFieldString(22, "012");
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(25, "14");
        reqMessage.setFieldString(41, getTerminalId());
        reqMessage.setFieldString(42, getMerchantId());
        reqMessage.setFieldString(48, "75002A4");
        reqMessage.setFieldString(49, currency);
        reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
        reqMessage.setField(64, getMac(packMacData(cardNo, processCode, formatAmt, traceNo, null, null, currency, null, null)));

        return reqMessage;
    }

    /**
     * 结算
     *
     * @param debitNum  借记交易总笔数
     * @param debitAmt  借记交易总金额
     * @param creditNum 贷记交易总笔数
     * @param creditAmt 贷记交易总金额
     * @return
     */
    public IMessage settlement(int debitNum, int debitAmt, int creditNum, int creditAmt) {
        String formatDebitNum = String.format("%3s", debitNum).replace(' ', '0');
        String formatDebitAmt = String.format("%12s", debitAmt).replace(' ', '0');
        String formatCreditNum = String.format("%3s", creditNum).replace(' ', '0');
        String formatCreditAmt = String.format("%12s", creditAmt).replace(' ', '0');
        String traceNo = getTraceNo();
        IMessage respMessage = null;
        try {
            IMessage reqMessage = createMessage();
            reqMessage.setFieldString(0, "0500");
            reqMessage.setFieldString(11, traceNo);
            reqMessage.setFieldString(24, "009");
            reqMessage.setFieldString(41, getTerminalId());
            reqMessage.setFieldString(42, getMerchantId());
            reqMessage.setFieldString(61, getBatchNo());
            reqMessage.setFieldString(63, formatDebitNum + formatDebitAmt + formatCreditNum + formatCreditAmt);

            respMessage = sendTran(reqMessage);

            //检查是否需要签到或参数下载
            cs.checkMessage(respMessage);
            field56Handle(respMessage);
        } catch (PackagingException e) {
            LOGGER.debug("when stagesPay happen PackagingException", e);
        } catch (IOException e) {
            LOGGER.debug("when stagesPay happen IOException", e);
        }
        return respMessage;
    }

    /**
     * 冲正交易
     *
     * @param orgMessage 要冲正的交易报文
     * @throws PackagingException
     * @throws IOException
     */
    public void reversal(IMessage orgMessage) throws PackagingException, IOException {
        IMessage reqMessage = createMessage();
        reqMessage.setFieldString(0, "0400");
        reqMessage.setFieldString(2, orgMessage.getFieldString(2));
        reqMessage.setFieldString(3, orgMessage.getFieldString(3));
        reqMessage.setFieldString(4, orgMessage.getFieldString(4));
        reqMessage.setFieldString(11, getTraceNo());
        if (orgMessage.getFieldString(14) != null) {
            reqMessage.setFieldString(14, orgMessage.getFieldString(14));
        }
        reqMessage.setFieldString(22, orgMessage.getFieldString(22));
        if (orgMessage.getFieldString(23) != null) {
            reqMessage.setFieldString(23, orgMessage.getFieldString(23));
        }
        reqMessage.setFieldString(24, "009");
        reqMessage.setFieldString(25, orgMessage.getFieldString(25));
        if (orgMessage.getFieldString(38) != null) {
            reqMessage.setFieldString(38, orgMessage.getFieldString(38));
        }
        reqMessage.setFieldString(41, orgMessage.getFieldString(41));
        reqMessage.setFieldString(42, orgMessage.getFieldString(42));
        if (orgMessage.getFieldString(44) != null) {
            reqMessage.setFieldString(44, orgMessage.getFieldString(44));
        }
        if (orgMessage.getFieldString(48) != null) {
            reqMessage.setFieldString(48, orgMessage.getFieldString(48));
        }
        reqMessage.setFieldString(49, orgMessage.getFieldString(49));
        reqMessage.setFieldString(61, getBatchNo() + getTellerNo() + getCerNo());
        reqMessage.setFieldString(62, orgMessage.getFieldString(0) + orgMessage.getFieldString(11) + "0000000000");

        sendTran(reqMessage);
    }

    /**
     * 组装算MAC的数据(2、3、4、11、12、13、49、38、39、41)
     *
     * @param cardNo      卡号-第2域
     * @param processCode 处理码-第3域
     * @param formatAmt   金额-第4域
     * @param traceNo     流水号-第11域
     * @param transTime   交易时间-第12域
     * @param transDate   交易日期-第13域
     * @param currency    货币-第49域
     * @param authCode    授权号-第38域
     * @param respCode    授权状态-第39域
     * @return
     */
    private ByteBuffer packMacData(String cardNo, String processCode, String formatAmt, String traceNo,
                                   String transTime, String transDate, String currency, String authCode, String respCode) {
        StringBuilder macData = new StringBuilder();
        if (null != cardNo) macData.append(cardNo.length() % 2 == 0 ? cardNo : "0" + cardNo);
        if (null != processCode) macData.append(processCode);
        if (null != formatAmt) macData.append(formatAmt);
        if (null != traceNo) macData.append(traceNo);
        if (null != transTime) macData.append(transTime);
        if (null != transDate) macData.append(transDate);
        if (null != currency) macData.append("0").append(currency);

        byte[] bcdMacData = HexUtil.parseHex(macData.toString());
        byte[] terminalByte = getTerminalId().getBytes();
        int macLength = bcdMacData.length + terminalByte.length;
        if (null != authCode) macLength = macLength + authCode.getBytes().length;
        if (null != respCode) macLength = macLength + respCode.getBytes().length;

        ByteBuffer buf = ByteBuffer.allocate(macLength);
        buf.put(bcdMacData);
        if (null != authCode) buf.put(authCode.getBytes());
        if (null != respCode) buf.put(respCode.getBytes());
        buf.put(terminalByte);

        return buf;
    }
}
