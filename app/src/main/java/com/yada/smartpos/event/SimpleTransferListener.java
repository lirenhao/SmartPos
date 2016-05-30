package com.yada.smartpos.event;

import android.os.Message;
import com.newland.me.SupportMSDAlgorithm;
import com.newland.mtype.common.MESeriesConst;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.emv.level2.EmvCardholderCertType;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.BytesUtils;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.impl.SwiperModuleImpl;
import com.yada.smartpos.util.Const;
import com.yada.smartpos.util.Const.MessageTag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Emv流程控制监听和QPBOC流程控制监听
 */
public class SimpleTransferListener implements EmvTransListener {

    private MainActivity mainActivity;
    private int index;
    private String encryptAlgorithm;
    private List L_55TAGS = new ArrayList();
    private int isECSwitch = 0;

    public SimpleTransferListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        // 判断是否使用DUKPT密钥管理技术
        if (((App) (mainActivity).getApplication()).isDukpt()) {
            index = Const.DUKPTIndexConst.DEFAULT_DUKPT_INDEX;
            encryptAlgorithm = MESeriesConst.TrackEncryptAlgorithm.BY_DUKPT_MODEL;
        } else {
            index = Const.DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX;
            encryptAlgorithm = MESeriesConst.TrackEncryptAlgorithm.BY_UNIONPAY_MODEL;
        }
        L_55TAGS.add(0x9f26);
        L_55TAGS.add(0x9F27);
        L_55TAGS.add(0x9F10);
        L_55TAGS.add(0x9F37);
        L_55TAGS.add(0x9F36);
        L_55TAGS.add(0x95);
        L_55TAGS.add(0x9A);
        L_55TAGS.add(0x9C);
        L_55TAGS.add(0x9F02);
        L_55TAGS.add(0x5F2A);
        L_55TAGS.add(0x82);
        L_55TAGS.add(0x9F1A);
        L_55TAGS.add(0x9F03);
        L_55TAGS.add(0x9F33);
        L_55TAGS.add(0x9F74);
        L_55TAGS.add(0x9F34);
        L_55TAGS.add(0x9F35);
        L_55TAGS.add(0x9F1E);
        L_55TAGS.add(0x84);
        L_55TAGS.add(0x9F09);
        L_55TAGS.add(0x9F41);
        L_55TAGS.add(0x91);
        L_55TAGS.add(0x71);
        L_55TAGS.add(0x72);
        L_55TAGS.add(0xDF31);
        L_55TAGS.add(0x9F63);
        L_55TAGS.add(0x8A);
        L_55TAGS.add(0xDF32);
        L_55TAGS.add(0xDF33);
        L_55TAGS.add(0xDF34);
        L_55TAGS.add(0xDF75);
    }

    // ExecuteRslt结果集：
    // 0x00/*成功,可能是脱机余额查询、明细成功或简易流程成功*/
    // 0x01/*交易授受*/
    // 0x02/*交易拒绝*/
    // 0x03/*联机*/
    // 0x0c/*成功获取EC余额*/
    // 0x0d/*非接触QPBOC交易接受*/
    // 0x0e/*非接触QPBOC交易拒绝*/
    // 0x0f/*非接触QPBOC交易联机*/
    // 0x10/*非接触MSD交易联机*/
    // 0x11/*成功获取QPBOC余额*/
    // 0xff/*交易失败*/
    // 0xfd/*FDDA失败*/
    // 0xfe/*FALLBACK*/
    // 0xfc/*取消*/
    // 0xfb/*交易金额大于终端限额*/
    // 0xfa/*卡片不支持电子现金*/
    @Override
    public void onQpbocFinished(EmvTransInfo transInfo) {
        // qpboc交易结束
        if (transInfo.getExecuteRslt() == 0x02) {
            mainActivity.showMessage("交易失败：【交易拒绝】！" + "\r\n", MessageTag.TIP);
        } else if (transInfo.getExecuteRslt() == 0x03) {
            mainActivity.showMessage("联机：【电子现金余额不足，请发起联机交易】！" + "\r\n", MessageTag.TIP);
            // todo 联机交易操作
        } else if (transInfo.getExecuteRslt() == 0x00 || transInfo.getExecuteRslt() == 0x01) {
            transInfo.getOnLinePin();
            // 交易成功、交易授受
            mainActivity.showMessage(">>>>交易完成，卡号:" + transInfo.getCardNo() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>交易完成，Serial number:" + transInfo.getCardSequenceNumber() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage("----8583 IC卡55域数据---表16　基本信息子域列表----" + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>应用密文(9f26):" + transInfo.getAppCryptogram() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>密文信息数据(9F27):" + transInfo.getCryptogramInformationData() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>发卡行应用数据(9F10):" + transInfo.getIssuerApplicationData() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>不可预知数(9F37):" + transInfo.getUnpredictableNumber() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>应用交易计数器(9F36):" + transInfo.getAppTransactionCounter() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>终端验证结果(95):" + transInfo.getTerminalVerificationResults() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>交易日期(9A):" + transInfo.getTransactionDate() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>交易类型(9C):" + transInfo.getTransactionType() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>授权金额(9F02):" + transInfo.getAmountAuthorisedNumeric() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>交易货币代码(5F2A):" + transInfo.getTransactionCurrencyCode() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>应用交互特征(82):" + transInfo.getApplicationInterchangeProfile() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>终端国家代码(9F1A):" + transInfo.getTerminalCountryCode() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>其它金额(9F03):" + transInfo.getAmountOtherNumeric() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>终端性能(9F33):" + transInfo.getTerminal_capabilities() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>电子现金发卡行授权码(9F74):" + transInfo.getEcIssuerAuthorizationCode() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage("----8583 IC卡55域数据---可选信息子域列表----" + "\r\n", MessageTag.TIP);
            mainActivity.showMessage(">>>>持卡人验证方法结果(9F34):" + transInfo.getCvmRslt() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>终端类型(9F35):" + transInfo.getTerminalType() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>接口设备序列号(9F1E):" + transInfo.getInterface_device_serial_number() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>专用文件名称(84):" + transInfo.getDedicatedFileName() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>软件版本号(9F09):" + transInfo.getAppVersionNumberTerminal() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>交易序列计数器(9F41):" + transInfo.getTransactionSequenceCounter() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>发卡行认证数据(91):" + transInfo.getIssuerAuthenticationData() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>发卡行脚本1(71):" + transInfo.getIssuerScriptTemplate1() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>发卡行脚本2(72):" + transInfo.getIssuerScriptTemplate2() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>发卡方脚本结果(DF31):" + transInfo.getScriptExecuteRslt() + "\r\n", MessageTag.DATA);
            mainActivity.showMessage(">>>>卡产品标识信息(9F63):" + transInfo.getCardProductIdatification() + "\r\n", MessageTag.DATA);
            TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
            mainActivity.showMessage(">>>>55域打包集合:" + ISOUtils.hexString(tlvPackage.pack()) + "\r\n", MessageTag.DATA);
        } else {
            mainActivity.showMessage("错误的qpboc状态返回！" + transInfo.getExecuteRslt() + "\r\n", MessageTag.DATA);
        }
    }

    @Override
    public void onSwipeFinished(SwipResult swipResult) throws IOException, ISO8583Exception {
        ((App) (mainActivity).getApplication()).setSwipResult(swipResult);
        ((App) (mainActivity).getApplication()).setCardNo(swipResult.getAccount().getAcctNo());
        Message message;
        if (swipResult != null) {
            ((App) mainActivity.getApplication()).setSwipResult(swipResult);
            message = mainActivity.getFragmentHandler().obtainMessage(3);
            message.obj = "inputPin";
            message.sendToTarget();
            while (((App) mainActivity.getApplication()).getPin() == null) {
                // TODO 等待输入密码结果暂且这样处理后续会修改
            }
            ISO8583 iso8583 = mainActivity.getIso8583();
            iso8583.setField(0, "0200");
            iso8583.setField(2, swipResult.getAccount().getAcctNo());// 主帐号
            iso8583.setField(3, "000008");// 处理码
            iso8583.setField(4, ((App) (mainActivity).getApplication()).getAmt().toString());// 交易金额
            iso8583.setField(11, "000008");// 系统跟踪号incTsc()
            iso8583.setField(14, swipResult.getValidDate());// 卡有效期
            iso8583.setField(22, "901");// POS输入方式
            iso8583.setField(24, "009");// NII
            iso8583.setField(25, "14");// 服务点条件码 14代表POS
            System.out.println(new String(swipResult.getSecondTrackData()));
            if (swipResult.getSecondTrackData() != null)
                iso8583.setField(35, ISOUtils.hexString(ISOUtils.str2bcd(new String(swipResult.getSecondTrackData()), false)));// 二磁道数据
            if (swipResult.getThirdTrackData() != null)
                iso8583.setField(36, ISOUtils.hexString(swipResult.getThirdTrackData()));// 三磁道数据
            iso8583.setField(41, "11000897");// 终端号
            iso8583.setField(42, "104110070110814");// 商户号
            iso8583.setField(49, "156");// 货币代码
            iso8583.setField(52, ISOUtils.hexString(((App) (mainActivity).getApplication()).getPin()));// 个人识别码
            iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号
            TcpClient client = mainActivity.getClient();
            try {
                String pack = iso8583.pack();
                ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes("60001200001306" + pack));
                System.out.println("刷卡8583包：60001200001306" + pack);
                client.open();
                ByteBuffer respBuffer = client.send(reqBuffer);
                String unpack = BytesUtils.bytesToHex(respBuffer.array());
                System.out.println(unpack);
                iso8583.initPack();
                iso8583.unpack(unpack.substring(14));
            } catch (Exception e) {
                throw e;
            } finally {
                client.close();
            }
            message = mainActivity.getFragmentHandler().obtainMessage(4);
            message.obj = iso8583.getField(39);
            message.sendToTarget();
        } else {
            //  swipResult为空
            message = mainActivity.getFragmentHandler().obtainMessage(4);
            message.obj = "fail";
            message.sendToTarget();
        }
    }

    @Override
    public void onEmvFinished(boolean isSuccess, EmvTransInfo transInfo) throws Exception {

        mainActivity.showMessage(">>>>交易完成，交易结果(DF75):" + transInfo.getExecuteRslt() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易完成，卡号:" + transInfo.getCardNo() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易完成，Serial number:" + transInfo.getCardSequenceNumber() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage("----8583 IC卡55域数据---表16　基本信息子域列表----" + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>应用密文(9f26):" + (transInfo.getAppCryptogram() == null ? "无返回" : ISOUtils.hexString(transInfo.getAppCryptogram())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>密文信息数据(9F27):" + transInfo.getCryptogramInformationData() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>发卡行应用数据(9F10):" + (transInfo.getIssuerApplicationData() == null ? "无返回" : ISOUtils.hexString(transInfo.getIssuerApplicationData())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>不可预知数(9F37):" + (transInfo.getUnpredictableNumber() == null ? "无返回" : ISOUtils.hexString(transInfo.getUnpredictableNumber())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>应用交易计数器(9F36):" + (transInfo.getAppTransactionCounter() == null ? "无返回" : ISOUtils.hexString(transInfo.getAppTransactionCounter())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>终端验证结果(95):" + (transInfo.getTerminalVerificationResults() == null ? "无返回" : ISOUtils.hexString(transInfo.getTerminalVerificationResults())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易日期(9A):" + transInfo.getTransactionDate() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易类型(9C):" + transInfo.getTransactionType() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>授权金额(9F02):" + transInfo.getAmountAuthorisedNumeric() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易货币代码(5F2A):" + transInfo.getTransactionCurrencyCode() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>应用交互特征(82):" + (transInfo.getApplicationInterchangeProfile() == null ? "无返回" : ISOUtils.hexString(transInfo.getApplicationInterchangeProfile())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>终端国家代码(9F1A):" + transInfo.getTerminalCountryCode() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>其它金额(9F03):" + transInfo.getAmountOtherNumeric() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>终端性能(9F33):" + (transInfo.getTerminal_capabilities() == null ? "无返回" : ISOUtils.hexString(transInfo.getTerminal_capabilities())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>电子现金发卡行授权码(9F74):" + transInfo.getEcIssuerAuthorizationCode() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage("----8583 IC卡55域数据---可选信息子域列表----" + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>持卡人验证方法结果(9F34):" + (transInfo.getCvmRslt() == null ? "无返回" : ISOUtils.hexString(transInfo.getCvmRslt())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>终端类型(9F35):" + transInfo.getTerminalType() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>接口设备序列号(9F1E):" + transInfo.getInterface_device_serial_number() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>专用文件名称(84):" + (transInfo.getDedicatedFileName() == null ? "无返回" : ISOUtils.hexString(transInfo.getDedicatedFileName())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>软件版本号(9F09):" + (transInfo.getAppVersionNumberTerminal() == null ? "无返回" : ISOUtils.hexString(transInfo.getAppVersionNumberTerminal())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>交易序列计数器(9F41):" + (transInfo.getTransactionSequenceCounter() == null ? "无返回" : ISOUtils.hexString(transInfo.getTransactionSequenceCounter())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>发卡行认证数据(91):" + transInfo.getIssuerAuthenticationData() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>发卡行脚本1(71):" + transInfo.getIssuerScriptTemplate1() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>发卡行脚本2(72):" + transInfo.getIssuerScriptTemplate2() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>发卡方脚本结果(DF31):" + transInfo.getScriptExecuteRslt() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>卡产品标识信息(9F63):" + transInfo.getCardProductIdatification() + "\r\n", MessageTag.DATA);
        if (isSuccess) {
            TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
            ((App) mainActivity.getApplication()).setTlvPackage(tlvPackage);
            mainActivity.showMessage(">>>>55域打包集合:" + ISOUtils.hexString(tlvPackage.pack()) + "\r\n", MessageTag.DATA);
        }

        ((App) mainActivity.getApplication()).setEmvTransInfo(transInfo);
        ((App) mainActivity.getApplication()).setCardNo(transInfo.getCardNo());

        Message message = mainActivity.getFragmentHandler().obtainMessage(4);
        message.obj = "order";
        message.sendToTarget();
    }

    @Override
    public void onError(EmvTransController controller, Exception e) {
        e.printStackTrace();
        mainActivity.showMessage("emv交易失败" + "\r\n", MessageTag.ERROR);
        mainActivity.showMessage(e.getMessage() + "\r\n", MessageTag.ERROR);
        Message message = mainActivity.getFragmentHandler().obtainMessage(4);
        message.obj = "fail";
        message.sendToTarget();
    }

    @Override
    public void onFallback(EmvTransInfo transInfo) throws Exception {
        mainActivity.showMessage("ic卡交易环境不满足:交易降级..." + "\r\n", MessageTag.ERROR);
        Message message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
    }

    @Override
    public void onRequestOnline(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        mainActivity.showMessage(">>>>请求联机交易onRequestOnline，交易结果(DF75):" + transInfo.getExecuteRslt() + "\r\n", MessageTag.DATA);
        TLVPackage tlvPackage = transInfo.setExternalInfoPackage(L_55TAGS);
        mainActivity.showMessage(">>>>55域打包集合:" + ISOUtils.hexString(tlvPackage.pack()) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>请求在线交易处理" + "\r\n", MessageTag.DATA);
        mainActivity.showMessage("终端验证结果(95):" + (transInfo.getTerminalVerificationResults() == null ? "无返回" : ISOUtils.hexString(transInfo.getTerminalVerificationResults())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage("应用密文(9f26):" + (transInfo.getAppCryptogram() == null ? "无返回" : ISOUtils.hexString(transInfo.getAppCryptogram())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage("持卡人验证方法结果(9f34):" + (transInfo.getCvmRslt() == null ? "无返回" : ISOUtils.hexString(transInfo.getCvmRslt())) + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>卡号:" + transInfo.getCardNo() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>卡序列号:" + transInfo.getCardSequenceNumber() + "\r\n", MessageTag.DATA);
        mainActivity.showMessage(">>>>卡有效期:" + transInfo.getCardExpirationDate() + "\r\n", MessageTag.DATA);

        if (null != transInfo.getTrack_2_eqv_data()) {
            mainActivity.showMessage(">>>>二磁道明文:" + ISOUtils.hexString(transInfo.getTrack_2_eqv_data()) + "\r\n", MessageTag.DATA);
            SwiperModuleImpl swiper = new SwiperModuleImpl();
            SwipResult swipResult = swiper.k21CalculateTrackData(ISOUtils.hexString(transInfo.getTrack_2_eqv_data()), null, new WorkingKey(index), SupportMSDAlgorithm.getMSDAlgorithm(encryptAlgorithm));
            ((App) mainActivity.getApplication()).setSwipResult(swipResult);
            mainActivity.showMessage(">>>>二磁道密文:" + (swipResult.getSecondTrackData() == null ? null : ISOUtils.hexString(swipResult.getSecondTrackData())) + "\r\n", MessageTag.DATA);
        }
        // [步骤1]：从该处transInfo中获取ic卡卡片信息后，发送银联8583交易

        ISO8583 iso8583 = mainActivity.getIso8583();
        iso8583.setField(0, "0200");
        iso8583.setField(2, transInfo.getCardNo());// 主帐号
        iso8583.setField(3, "000008");// 处理码
        iso8583.setField(4, transInfo.getAmountAuthorisedNumeric());// 交易金额
        iso8583.setField(11, "000008");// 系统跟踪号incTsc()
        iso8583.setField(14, transInfo.getCardExpirationDate().substring(0, 4));// 卡有效期
        iso8583.setField(22, "051");// POS输入方式
        if (null != transInfo.getCardSequenceNumber() && !transInfo.getCardSequenceNumber().equals(""))
            iso8583.setField(23, transInfo.getCardSequenceNumber());// 卡片序列号
        iso8583.setField(24, "009");// NII
        iso8583.setField(25, "14");// 服务点条件码 14代表POS
        if (null != transInfo.getTrack_2_eqv_data()) // TODO 二磁道放明文还是密文
            iso8583.setField(35, ISOUtils.hexString(transInfo.getTrack_2_eqv_data()).substring(0, 37));// 二磁道数据
        System.out.println(ISOUtils.hexString(transInfo.getTrack_2_eqv_data()));
        iso8583.setField(41, "11000897");// 终端号
        iso8583.setField(42, "104110070110814");// 商户号
        iso8583.setField(49, "156");// 货币代码
        iso8583.setField(52, BytesUtils.bytesToHex(((App) (mainActivity).getApplication()).getPin()));// 个人识别码
        iso8583.setField(55, ISOUtils.hexString(tlvPackage.pack()));// IC卡交易数据域
        iso8583.setField(61, "000008001000009");// 自定义域 交易批次号+操作员号+票据号+卡类型+发卡银行简称

        TcpClient client = mainActivity.getClient();
        String pack = iso8583.pack();
        ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes("60001200001306" + pack));
        System.out.println("IC卡8583包：60001200001306" + pack);
        client.open();
        ByteBuffer respBuffer = client.send(reqBuffer);
        iso8583.initPack();
        String unpack = BytesUtils.bytesToHex(respBuffer.array());
        System.out.println(unpack);
        iso8583.initPack();
        iso8583.unpack(unpack.substring(14));

        if ("00".equals(iso8583.getField(39))) {
            tlvPackage.unpack(BytesUtils.hexStringToBytes(iso8583.getField(55)));
            SecondIssuanceRequest request = new SecondIssuanceRequest();
            request.setAuthorisationResponseCode(iso8583.getField(39));// 取自银联8583规范39域值,该参数按交易实际值填充
            request.setIssuerAuthenticationData(tlvPackage.getValue(0x91));//取自银联8583规范 55域0x91值,该参数按交易实际值填充
            request.setIssuerScriptTemplate1(tlvPackage.getValue(0x71));//取自银联8583规范 55域0x71值,该参数按交易实际值填充
            request.setIssuerScriptTemplate2(tlvPackage.getValue(0x72));//取自银联8583规范 55域0x72值,该参数按交易实际值填充
            request.setAuthorisationCode(iso8583.getField(38));//取自银联8583规范 38域值,该参数按交易实际值填充

            // [步骤2].ic卡联机交易成功或者非接圈存交易，调用二次授权接口，等回调onemvfinished结束流程。
            controller.secondIssuance(request);
        } else {
            // [并列步骤2].联机交易失败或者非接交易(除圈存外)调用emv结束方法，结束流程。
            controller.doEmvFinish(false);
        }

    }

    @Override
    public void onRequestSelectApplication(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        mainActivity.showMessage("错误的事件返回，不可能要求应用选择！" + "\r\n", MessageTag.DATA);
        controller.cancelEmv();
    }

    @Override
    public void onRequestTransferConfirm(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        mainActivity.showMessage("交易确认完成" + "\r\n", MessageTag.DATA);
        controller.transferConfirm(true);
    }

    @Override
    public void onRequestAmountEntry(final EmvTransController controller, EmvTransInfo context) {
        // 调用输入金额界面
        Message message = mainActivity.getFragmentHandler().obtainMessage(1);
        message.obj = "amount";
        message.sendToTarget();
        while (((App) mainActivity.getApplication()).getPin() == null) {
            // TODO 等待金额结果暂且这样处理后续会修改
        }
    }

    // IM81和N900会触发，ME30、ME31不会触发
    @Override
    public void onRequestPinEntry(EmvTransController controller, EmvTransInfo transInfo) throws Exception {
        // EMV调用密码键盘
        SwiperModuleImpl swiper = new SwiperModuleImpl();
        SwipResult swipResult = swiper.k21CalculateTrackData(ISOUtils.hexString(transInfo.getTrack_2_eqv_data()),
                null, new WorkingKey(index), SupportMSDAlgorithm.getMSDAlgorithm(encryptAlgorithm));
        ((App) mainActivity.getApplication()).setCardNo(transInfo.getCardNo());
        if (swipResult != null) {
            ((App) mainActivity.getApplication()).setSwipResult(swipResult);
            Message message = mainActivity.getFragmentHandler().obtainMessage(3);
            message.obj = "inputPin";
            message.sendToTarget();
            while (((App) mainActivity.getApplication()).getPin() == null) {
                // TODO 等待输入密码结果暂且这样处理后续会修改
            }
            controller.sendPinInputResult(((App) mainActivity.getApplication()).getPin());
        } else {
            //  swipResult为空
        }
    }

    /**
     * 是否拦截acctType select事件
     */
    @Override
    public boolean isAccountTypeSelectInterceptor() {
        return true;
    }

    /**
     * 是否拦截持卡人证件确认事件
     */
    @Override
    public boolean isCardHolderCertConfirmInterceptor() {
        return true;
    }

    /**
     * 是否拦截电子现金确认事件
     */
    @Override
    public boolean isEcSwitchInterceptor() {
        return true;
    }

    /**
     * 是否拦截使用外部的序列号处理器
     */
    @Override
    public boolean isTransferSequenceGenerateInterceptor() {
        return true;
    }

    /**
     * 是否拦截消息显示事件
     */
    @Override
    public boolean isLCDMsgInterceptor() {
        return true;
    }

    /**
     * 账号类型选择
     * <p>
     * 返回int范围
     * <p>
     * <ol>
     * <li>default</li>
     * <li>savings</li>
     * <li>Cheque/debit</li>
     * <li>Credit</li>
     * </ol>
     *
     * @return 1-4：选择范围， －1：失败
     */
    @Override
    public int accTypeSelect() {
        return 1;
    }

    /**
     * 持卡人证件确认
     *
     * @return true:确认正确， false:确认失败
     */
    @Override
    public boolean cardHolderCertConfirm(EmvCardholderCertType certType, String certno) {
        return true;
    }

    /**
     * 电子现金/emv选择
     * 交易返回：
     * 1：继续电子现金交易
     * 0：不进行电子现金交易
     * －1:用户中止
     * －3:超时
     */
    @Override
    public int ecSwitch() {
        return 0;

    }

    /**
     * 流水号加1并返回
     */
    @Override
    public int incTsc() {
        return 0;
    }

    /**
     * 显示信息
     *
     * @param title        标题
     * @param msg          消息
     * @param yesnoShowed  是否出现yesno
     * @param waittingTime 等待时间
     * @return 如果yesnoShow == true, 返回1 表示确认，返回0表示取消 如果yesnoShow == false,
     * 返回值无意义
     */
    @Override
    public int lcdMsg(String title, String msg, boolean yesnoShowed, int waittingTime) {
        return 1;
    }

    // 线程等待、唤醒
    public class WaitThreat {
        Object syncObj = new Object();

        void waitForRslt() throws InterruptedException {
            synchronized (syncObj) {
                syncObj.wait();
            }
        }

        void notifyThread() {
            synchronized (syncObj) {
                syncObj.notify();
            }
        }
    }
}
