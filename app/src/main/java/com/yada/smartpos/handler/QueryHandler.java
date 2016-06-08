package com.yada.smartpos.handler;

import android.os.Message;
import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.EmvTransListener;
import com.yada.smartpos.event.SimpleTransferListener;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.SwiperModule;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.module.impl.SwiperModuleImpl;
import com.yada.smartpos.util.Client;
import com.yada.smartpos.util.Const;
import com.yada.smartpos.util.PackMessage;

import java.io.IOException;
import java.math.BigDecimal;

public class QueryHandler {

    private MainActivity mainActivity;
    private Message message;

    public QueryHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void query(){
        // 刷卡
        message = mainActivity.getFragmentHandler().obtainMessage(2);
        message.obj = "swipeCard";
        message.sendToTarget();
        mainActivity.getSwipeCardWaitThreat().waitForRslt();

        // 判断是IC卡还是磁条卡
        EmvTransListener transListener = new SimpleTransferListener(mainActivity);
        EmvModule emvModule = new EmvModuleImpl();
        emvModule.initEmvModule(mainActivity);
        EmvTransController controller = emvModule.getEmvTransController(transListener);
        switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
            case MSCARD:
                SwiperModule swiper = new SwiperModuleImpl();
                try {
                    SwipResult swipResult = swiper.readPlainResult(new SwiperReadModel[]{
                            SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK});

                    ((App) mainActivity.getApplication()).getTransData().setAccount(swipResult.getAccount().getAcctNo());
                    ((App) mainActivity.getApplication()).getTransData().setSecondTrackData(
                            ISOUtils.hexString(ISOUtils.str2bcd(new String(swipResult.getSecondTrackData()), false)));
                    ((App) mainActivity.getApplication()).getTransData().setThirdTrackData(
                            ISOUtils.hexString(swipResult.getThirdTrackData()));
                    // 磁条卡输入密码
                    message = mainActivity.getFragmentHandler().obtainMessage(3);
                    message.obj = "inputPin";
                    message.sendToTarget();
                    mainActivity.getInputPinWaitThreat().waitForRslt();
                    // 联机交易
                    onLineQuery(((App) mainActivity.getApplication()).getTransData());
                } catch (Exception e) {
                    // 读卡异常重新读卡
                    mainActivity.showMessage(e.getMessage(), Const.MessageTag.ERROR);
                    message = mainActivity.getFragmentHandler().obtainMessage(2);
                    message.obj = "swipeCard";
                    message.sendToTarget();
                    mainActivity.getSwipeCardWaitThreat().waitForRslt();
                }
                break;
            case ICCARD:
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                        ((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2),
                        new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            case RFCARD:
                controller.startEmv(ProcessingCode.GOODS_AND_SERVICE, InnerProcessingCode.EC_CONSUMPTION,
                        ((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2),
                        new BigDecimal("0"), true);
                mainActivity.getWaitThreat().waitForRslt();
                break;
            default:
                break;
        }

        // 输入密码

        // 展示余额
        message = mainActivity.getFragmentHandler().obtainMessage(100);
        message.obj = "success";
        message.sendToTarget();
    }

    private ISO8583 onLineQuery(TransData transData) throws ISO8583Exception, IOException {
        ISO8583 iso8583 = PackMessage.query(mainActivity, transData);
        String pack = iso8583.pack();
        TcpClient client = mainActivity.getClient();
        String unpack = Client.send(mainActivity, client, pack, null);
        iso8583.initPack();
        iso8583.unpack(unpack);
        return iso8583;
    }

}
