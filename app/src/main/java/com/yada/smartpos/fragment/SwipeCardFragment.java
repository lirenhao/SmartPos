package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.newland.mtype.ModuleType;
import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.light.LightType;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.EmvTransListener;
import com.yada.smartpos.event.SimpleTransferListener;
import com.yada.smartpos.module.CardReaderModule;
import com.yada.smartpos.module.EmvModule;
import com.yada.smartpos.module.IndicatorLightModule;
import com.yada.smartpos.module.SwiperModule;
import com.yada.smartpos.module.impl.CardReaderModuleImpl;
import com.yada.smartpos.module.impl.EmvModuleImpl;
import com.yada.smartpos.module.impl.IndicatorLightModuleImpl;
import com.yada.smartpos.module.impl.SwiperModuleImpl;
import com.yada.smartpos.util.Const;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class SwipeCardFragment extends Fragment {

    private MainActivity mainActivity;
    private CardReaderModule cardReader;
    private IndicatorLightModule indicatorLight;

    private EmvModule emvModule;
    private EmvTransController controller;

    public SwipeCardFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardReader = new CardReaderModuleImpl();
        indicatorLight = new IndicatorLightModuleImpl();
        emvModule = new EmvModuleImpl();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swipe_card, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        indicatorLight.turnOnLight(new LightType[]{LightType.BLUE_LIGHT});
        cardReader.openCardReader("请刷卡或者插入IC卡",
                new ModuleType[]{ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER},
                null, true, true, 60, TimeUnit.SECONDS, new DeviceEventListener<OpenCardReaderEvent>() {
                    @Override
                    public void onEvent(OpenCardReaderEvent openCardReaderEvent, Handler handler) {
                        if (openCardReaderEvent.isSuccess()) {
                            indicatorLight.turnOffLight(new LightType[]{LightType.BLUE_LIGHT});
                            EmvTransListener transListener = new SimpleTransferListener(mainActivity);
                            switch (openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]) {
                                case MSCARD:
                                    SwiperModule swiper = new SwiperModuleImpl();
                                    try {
                                        SwipResult swipResult = swiper.readPlainResult(new SwiperReadModel[] {
                                                SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK });
                                        transListener.onSwipeFinished(swipResult);
                                    } catch (Exception e) {
                                        mainActivity.showMessage(e.getMessage(), Const.MessageTag.ERROR);
                                        Message message = mainActivity.getFragmentHandler().obtainMessage(2);
                                        message.obj = "swipeCard";
                                        message.sendToTarget();
                                    }
                                    break;
                                case ICCARD:
                                    emvModule.initEmvModule(mainActivity);
                                    controller = emvModule.getEmvTransController(transListener);
                                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE,
                                            InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                                            ((App) mainActivity.getApplication()).getAmt().movePointLeft(2),
                                            new BigDecimal("0"), true);
                                    break;
                                case RFCARD:
                                    emvModule.initEmvModule(mainActivity);
                                    controller = emvModule.getEmvTransController(new SimpleTransferListener(mainActivity));
                                    controller.startEmv(ProcessingCode.GOODS_AND_SERVICE,
                                            InnerProcessingCode.EC_CONSUMPTION,
                                            ((App) mainActivity.getApplication()).getAmt().movePointLeft(2),
                                            new BigDecimal("0"), true);
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
