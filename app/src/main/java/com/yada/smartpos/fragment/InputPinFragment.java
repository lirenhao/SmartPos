package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.*;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.module.impl.PinInputModuleImpl;
import com.yada.smartpos.util.Const;

import java.util.concurrent.TimeUnit;

public class InputPinFragment extends Fragment {

    private MainActivity mainActivity;
    private FragmentManager manager;
    private Fragment fragment;
    private Handler handler;

    private ImageView iv0;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private ImageView iv6;
    private ImageView iv7;
    private ImageView iv8;
    private ImageView iv9;

    private PinInputModule pinInput;
    private TextView inputMsg;
    private TextView inputTxt;
    private int inputLen = 0;

    public InputPinFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        fragment = this;
        handler = new Handler(mainActivity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2: // 键盘输入
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < inputLen; i++) {
                            buffer.append(" * ");
                        }
                        inputTxt.setText(buffer.toString());
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = getFragmentManager();
        pinInput = new PinInputModuleImpl();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keybord, container, false);

        iv0 = (ImageView) view.findViewById(R.id.iv_0);
        iv1 = (ImageView) view.findViewById(R.id.iv_1);
        iv2 = (ImageView) view.findViewById(R.id.iv_2);
        iv3 = (ImageView) view.findViewById(R.id.iv_3);
        iv4 = (ImageView) view.findViewById(R.id.iv_4);
        iv5 = (ImageView) view.findViewById(R.id.iv_5);
        iv6 = (ImageView) view.findViewById(R.id.iv_6);
        iv7 = (ImageView) view.findViewById(R.id.iv_7);
        iv8 = (ImageView) view.findViewById(R.id.iv_8);
        iv9 = (ImageView) view.findViewById(R.id.iv_9);

        inputMsg = (TextView) view.findViewById(R.id.input_tip_msg);
        inputMsg.setText(R.string.pin_input);
        inputTxt = (TextView) view.findViewById(R.id.input_pin_txt);

        getRandomKeyBoardNumber();

        pinInput.startK21StandPininput(null, new WorkingKey(Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX),
                KeyManageType.MKSK, AccountInputType.USE_ACCOUNT, ((App) mainActivity.getApplication()).getCardNo(),
                6, null, new byte[]{'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F'},
                PinConfirmType.ENABLE_ENTER_COMMANG, 60, TimeUnit.SECONDS, null, null, pinInputListener);
        return view;
    }

    private void getRandomKeyBoardNumber() {
        int[] resImages = {R.drawable.keyboard_0, R.drawable.keyboard_1,
                R.drawable.keyboard_2, R.drawable.keyboard_3, R.drawable.keyboard_4, R.drawable.keyboard_5,
                R.drawable.keyboard_6, R.drawable.keyboard_7, R.drawable.keyboard_8, R.drawable.keyboard_9};

        int x0 = 0, x1 = 136, x2 = 270, x3 = 405, x4 = 535;
        int y0 = 494, y1 = 592, y2 = 689, y3 = 795, y4 = 887;

        int[] coordinateInt = new int[]{x0, y0, x1, y1, x1, y0, x2, y1, x2, y0, x3, y1, x3, y0, x4, y1,
                x0, y1, x1, y2, x1, y1, x2, y2, x2, y1, x3, y2, x3, y1, x4, y2, x0, y2, x1, y3, x1, y2,
                x2, y3, x2, y2, x3, y3, x0, y3, x1, y4, x1, y3, x2, y4, x2, y3, x3, y4, x3, y2, x4, y4,};
        // 初始坐标集合
        byte[] initCoordinate = new byte[coordinateInt.length * 2];
        for (int i = 0, j = 0; i < coordinateInt.length; i++, j++) {
            initCoordinate[j] = (byte) ((coordinateInt[i] >> 8) & 0xff);
            j++;
            initCoordinate[j] = (byte) (coordinateInt[i] & 0xff);
        }
        byte[] randomCoordinate = pinInput.loadRandomKeyboard(new KeyboardRandom(initCoordinate));
        StringBuilder sb = new StringBuilder();
        byte[] numserial = new byte[10];// 获取数字键
        int d = 0;
        for (int i = 0; i < randomCoordinate.length; i++) {
            if (i == 3 || i == 7 || i == 11 || i == 13 || i == 14) {
                continue;
            }
            numserial[d] = (byte) (randomCoordinate[i] & 0x0f);
            sb.append(numserial[d]);
            d++;
        }
        ImageView[] buttons = new ImageView[]{iv1, iv2, iv3, iv4, iv5, iv6, iv7, iv8, iv9, iv0};
        for (int i = 0; i < buttons.length; i++) {
            int number = Integer.valueOf(sb.charAt(i) + "");
            buttons[i].setImageResource(resImages[number]);
        }
    }

    private DeviceEventListener<K21PininutEvent> pinInputListener = new DeviceEventListener<K21PininutEvent>() {
        @Override
        public Handler getUIHandler() {
            return null;
        }

        @Override
        public void onEvent(K21PininutEvent event, Handler h) {
            if (event.isProcessing()) {
                // 正在输入
                PinInputEvent.NotifyStep notifyStep = event.getNotifyStep();
                if (notifyStep == PinInputEvent.NotifyStep.ENTER) {
                    inputLen = inputLen + 1;
                } else if (notifyStep == PinInputEvent.NotifyStep.BACKSPACE) {
                    inputLen = (inputLen <= 0 ? 0 : inputLen - 1);
                }
                Message msg = handler.obtainMessage(2);
                msg.sendToTarget();
            } else if (event.isUserCanceled()) {
                // 取消
                manager.popBackStack();
            } else if (event.isSuccess()) {
                // 确定
                if (event.getInputLen() == 0) {
                    ((App) (mainActivity).getApplication()).setPin(new byte[6]);
                } else {
                    // 输入成功
                    ((App) (mainActivity).getApplication()).setPin(event.getEncrypPin());
                }
                manager.beginTransaction().hide(fragment);
            } else {
                Message pinFinishMsg = new Message();
                pinFinishMsg.what = Const.PIN_FINISH;
                pinFinishMsg.obj = new byte[6];
            }
        }
    };
}
