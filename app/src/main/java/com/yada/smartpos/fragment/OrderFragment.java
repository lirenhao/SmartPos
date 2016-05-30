package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.WordStockType;
import com.newland.mtype.util.ISOUtils;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.PrinterModule;
import com.yada.smartpos.module.impl.PrinterModuleImpl;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private FragmentManager manager;
    private StringBuffer order;

    public OrderFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        TextView orderView = (TextView) view.findViewById(R.id.order);
        String pin = ((App) mainActivity.getApplication()).getPin() != null ?
                ISOUtils.hexString(((App) mainActivity.getApplication()).getPin()) : "无密钥";
        order = new StringBuffer();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        order.append("\n\n").append("           签购单").append("\n\n").
                append("商户名称：TEST\n").
                append("商户编号：104110070110814\n").
                append("终端编号：11000897\n").
                append("操作员号：001\n").
                append("卡号：").append(((App) mainActivity.getApplication()).getCardNo()).append("\n").
                append("密码：").append(pin).append("\n").
                append("消费类型：消费\n").
                append("授权号：000001\n").
                append("参考号：000000000000001\n").
                append("日期时间：").append(format.format(new Date())).append("\n").
                append("交易金额：").append(((App) mainActivity.getApplication()).getAmt().movePointLeft(2)).append("RMB\n").
                append("--------------------------------\n").
                append("\n\n\n\n\n");
        new Date();
        orderView.setText(order.toString());
        Button finish = (Button) view.findViewById(R.id.finish);
        finish.setOnClickListener(this);

//        doPrinter();

        return view;
    }

    @Override
    public void onClick(View v) {
        manager.beginTransaction().replace(R.id.main, new MenuFragment(mainActivity)).commit();
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
                printer.printString(order.toString());
            }
        }).start();
    }
}
