package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.light.LightType;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mtype.util.ISOUtils;
import com.payneteasy.tlv.HexUtil;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.CardReaderModule;
import com.yada.smartpos.module.IndicatorLightModule;
import com.yada.smartpos.module.SwiperModule;
import com.yada.smartpos.module.impl.CardReaderModuleImpl;
import com.yada.smartpos.module.impl.IndicatorLightModuleImpl;
import com.yada.smartpos.module.impl.SwiperModuleImpl;
import com.yada.smartpos.util.Const;
import com.yada.smartpos.util.TransType;

import java.util.concurrent.TimeUnit;

public class SwipeCardFragment extends Fragment {

    private MainActivity mainActivity;
    private CardReaderModule cardReader;
    private IndicatorLightModule indicatorLight;

    public SwipeCardFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardReader = new CardReaderModuleImpl();
        indicatorLight = new IndicatorLightModuleImpl();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swipe_card, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        indicatorLight.turnOnLight(new LightType[]{LightType.BLUE_LIGHT});
        ModuleType[] moduleTypes;
        if (((App) mainActivity.getApplication()).getTransData().getTransType().equals(TransType.EC_CONSUMPTION)) {
            moduleTypes = new ModuleType[]{ModuleType.COMMON_RFCARDREADER};
        } else {
            moduleTypes = new ModuleType[]{ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER};
        }
        cardReader.openCardReader("请刷卡或者插入IC卡" ,moduleTypes, null, true, true, 60, TimeUnit.SECONDS,
                new DeviceEventListener<OpenCardReaderEvent>() {
                    @Override
                    public void onEvent(OpenCardReaderEvent openCardReaderEvent, Handler handler) {
                        if (openCardReaderEvent.isSuccess()) {
                            indicatorLight.turnOffLight(new LightType[]{LightType.BLUE_LIGHT});
                            ((App) mainActivity.getApplication()).getTransData().
                                    setCardType(openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]);
                            switch (((App) mainActivity.getApplication()).getTransData().getCardType()) {
                                case MSCARD:
                                    SwiperModule swiper = new SwiperModuleImpl();
                                    try {
                                        SwipResult swipResult = swiper.readPlainResult(new SwiperReadModel[]{
                                                SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK});
                                        ((App) mainActivity.getApplication()).getTransData().setAccount(swipResult.getAccount().getAcctNo());
                                        if (swipResult.getSecondTrackData() != null)
                                            ((App) mainActivity.getApplication()).getTransData().setSecondTrackData(
                                                    HexUtil.toHexString(ISOUtils.str2bcd(new String(swipResult.getSecondTrackData()), false)));
                                        if (swipResult.getThirdTrackData() != null)
                                            ((App) mainActivity.getApplication()).getTransData().setThirdTrackData(
                                                    HexUtil.toHexString(ISOUtils.str2bcd(new String(swipResult.getThirdTrackData()), false)));
                                        mainActivity.getSwipeCardWaitThreat().notifyThread();
                                    } catch (Exception e) {
                                        // 读卡异常重新读卡
                                        mainActivity.showMessage(e.getMessage(), Const.MessageTag.ERROR);
                                        Message message = mainActivity.getFragmentHandler().obtainMessage(2);
                                        message.obj = "swipeCard";
                                        message.sendToTarget();
                                    }
                                    break;
                                case ICCARD:
                                    mainActivity.getSwipeCardWaitThreat().notifyThread();
                                    break;
                                case RFCARD:
                                    mainActivity.getSwipeCardWaitThreat().notifyThread();
                                    break;
                                default:
                                    break;
                            }
                        } else if (openCardReaderEvent.isUserCanceled()) {
                            mainActivity.showMessage("取消开启读卡器" + "\r\n", Const.MessageTag.NORMAL);
                        } else if (openCardReaderEvent.isFailed()) {
                            mainActivity.showMessage("读卡器开启失败" + "\r\n", Const.MessageTag.NORMAL);
                        }
                    }

                    @Override
                    public Handler getUIHandler() {
                        return null;
                    }
                });

    }

    @Override
    public void onPause() {
        super.onStop();
        cardReader.cancelCardRead();
        indicatorLight.turnOffLight(new LightType[]{LightType.BLUE_LIGHT});
    }
}
