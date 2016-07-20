package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.WordStockType;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.PrinterModule;
import com.yada.smartpos.module.impl.PrinterModuleImpl;

public class OrderFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;
    private String orderString;

    public OrderFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        TextView orderView = (TextView) view.findViewById(R.id.order);
        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        if (null == transResult) {
            orderView.setGravity(Gravity.CENTER);
            orderView.setText("交易失败\n交易结果为空");
        } else {
            if ("1".equals(transResult.getTransCode())) {
                switch (((App) mainActivity.getApplication()).getTransData().getTransType()) {
                    case QUERY:
                    case BILL:
                        orderView.setGravity(Gravity.CENTER);
                        orderView.setText(transResult.getResultText());
                        break;
                    default:
                        orderView.setText(transResult.getResultText());
                        orderString = transResult.getResultText();
                        // doPrinter();
                }
            } else {
                orderView.setGravity(Gravity.CENTER);
                orderView.setText(transResult.getResultText());
            }
        }
        Button finish = (Button) view.findViewById(R.id.finish);
        finish.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        handleListener.menuView();
    }

    private void doPrinter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrinterModule printer = new PrinterModuleImpl();
                //设置浓度
                printer.setDensity(10);
                //设置字体
                printer.setFontType(LiteralType.CHINESE, FontSettingScope.HEIGHT, FontType.NORMAL);
                //设置行间隔
                printer.setLineSpace(4);
                //设置打印字库
                printer.setWordStock(WordStockType.PIX_24);
                //步骤1： 打印机初始化该方法必须在打印前调用一次
                printer.init();
                //步骤2：开始打印，参数分别为打印信息，超时时间，超时时间单位
                printer.printString(orderString);
            }
        }).start();
    }
}
