package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.BytesUtils;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.net.FixLenPackageSplitterFactory;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.handler.ConsumeHandler;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.module.impl.PinInputModuleImpl;
import com.yada.smartpos.util.Const;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class MenuFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private FragmentManager manager;

    private String[] arrText = new String[]{
            "消费", "消费撤销", "消费退货",
            "预授权", "打印", "签到",
            "电子现金", "分期", "余额查询"
    };

    private int[] arrImages = new int[]{
            R.drawable.main_icon_1, R.drawable.main_icon_2, R.drawable.main_icon_3,
            R.drawable.main_icon_4, R.drawable.main_icon_5, R.drawable.main_icon_6,
            R.drawable.main_icon_7, R.drawable.main_icon_8, R.drawable.main_icon_9
    };

    public MenuFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = getFragmentManager();
        // 清空堆栈
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ((App) mainActivity.getApplication()).setSwipResult(null);
        ((App) mainActivity.getApplication()).setEmvTransInfo(null);
        ((App) mainActivity.getApplication()).setAmt(null);
        ((App) mainActivity.getApplication()).setCardNo(null);
        ((App) mainActivity.getApplication()).setPin(null);
        ((App) mainActivity.getApplication()).setTlvPackage(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        GridLayout layout = (GridLayout) view.findViewById(R.id.fl_item);
        for (int i = 0; i < arrText.length; i++) {
            View item = inflater.inflate(R.layout.fragment_menu_item, container, false);
            item.setTag(i);
            ImageView imageView = (ImageView) item.findViewById(R.id.iv_icon);
            imageView.setImageResource(arrImages[i]);
            TextView textView = (TextView) item.findViewById(R.id.txt_name);
            textView.setText(arrText[i]);
            layout.addView(item);
            item.setOnClickListener(this);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        // 根据元素位置获取对应的值
        int i = (int) view.getTag();
        switch (i) {
            case 0:
                ConsumeHandler consumeHandler = new ConsumeHandler(mainActivity);
                consumeHandler.pay();
                break;
            case 1:
                doTcp();
                break;
            case 5:
                doSignIn();
            default:

        }
    }

    private void doTcp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client = null;
                try {
                    String pack = "6000120000130602007020058020c09009190456351010089268584731000000000000000000168309010012143704563510100892685847d4912520100008690038333938313030303130343131303038333938313030300156771ecbb5b635a94d0015303930353131303031303030303832483edbc6335f2119";
                    ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes(pack));
                    client = new TcpClient(new InetSocketAddress("10.2.56.70", 1000),
                            new FixLenPackageSplitterFactory(2, false), 20000);
                    client.open();
                    ByteBuffer respBuffer = client.send(reqBuffer);
                    System.out.println(BytesUtils.bytesToHex(respBuffer.array()));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    client.close();
                }
            }
        }).start();
    }

    private void doSignIn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ISO8583 iso8583 = mainActivity.getIso8583();
                iso8583.setField(0, "0800");
                iso8583.setField(3, "000008");// 处理码
                iso8583.setField(11, "000008");// 系统跟踪号incTsc()
                iso8583.setField(24, "009");// NII
                iso8583.setField(41, "11000897");// 终端号
                iso8583.setField(42, "104110070110814");// 商户号
                iso8583.setField(61, "000008001");// 自定义域 交易批次号+操作员号+票据号+卡类型+发卡银行简称
                TcpClient client = mainActivity.getClient();
                try {
                    ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes("60001200001306" + iso8583.pack()));
                    System.out.println("签到8583包：60001200001306" + iso8583.pack());
                    client.open();
                    ByteBuffer respBuffer = client.send(reqBuffer);
                    String unpack = BytesUtils.bytesToHex(respBuffer.array());
                    System.out.println(unpack.substring(14));
                    iso8583.initPack();
                    iso8583.unpack(unpack.substring(14));

                    doInitKey(iso8583.getField(48));
                } catch (IOException e) {
                    mainActivity.showMessage("签到异常异常：" + e.getMessage(), Const.MessageTag.ERROR);
                } catch (ISO8583Exception e) {
                    mainActivity.showMessage("签到异常异常：" + e.getMessage(), Const.MessageTag.ERROR);
                } finally {
                    client.close();
                }
            }
        }).start();
    }

    private void doInitKey(String field48) {
        String tpk = field48.substring(5, 37);
        PinInputModule pinInput = new PinInputModuleImpl();
        byte[] kcv = pinInput.loadWorkingKey(WorkingKeyType.PININPUT, 2,
                Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX, ISOUtils.hex2byte(tpk), null);
        System.out.println(field48.substring(39, 55));
        System.out.println(BytesUtils.bytesToHex(kcv));
    }
}
