package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
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
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.model.TransResult;
import com.yada.smartpos.module.PrinterModule;
import com.yada.smartpos.module.impl.PrinterModuleImpl;

import java.io.UnsupportedEncodingException;

public class OrderFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private StringBuffer order;

    public OrderFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        TextView orderView = (TextView) view.findViewById(R.id.order);
        order = new StringBuffer();
        TransResult transResult = ((App) mainActivity.getApplication()).getTransResult();
        if (null == transResult) {
            orderView.setGravity(Gravity.CENTER);
            order.append("交易失败\n交易结果为空");
        } else {
            if ("1".equals(transResult.getTransCode())) {
                ISO8583 iso8583 = mainActivity.getIso8583();
                try {
                    iso8583.unpack(transResult.getTransResp());
                    order.append("\n\n").append("           ").
                            append("签购单").append("\n\n").
                            append("商户名称：TEST\n").
                            append("商户编号：104110070110814\n").
                            append("终端编号：11000897\n").
                            append("操作员号：001\n").
                            append("卡号：").append(iso8583.getField(2)).append("\n").
                            append("凭证号：").append(((App) mainActivity.getApplication()).getTransData().getOldProofNo()).append("\n").
                            append("授权码：").append(iso8583.getField(38)).append("\n").
                            append("参考号：").append(iso8583.getField(37)).append("\n").
                            append("日期时间：").append(iso8583.getField(13)).append(iso8583.getField(12)).append("\n").
                            append("金额：").append(iso8583.getField(4)).append("\n").
                            append("--------------------------------\n").append("\n\n\n\n\n");
                     doPrinter();
                } catch (UnsupportedEncodingException | ISO8583Exception e) {
                    e.printStackTrace();
                    orderView.setGravity(Gravity.CENTER);
                    order.append("交易失败\n").append(e.getMessage());
                }
            } else {
                orderView.setGravity(Gravity.CENTER);
                order.append("交易失败\n").append(transResult.getTransMsg());
            }
        }
        orderView.setText(order.toString());
        Button finish = (Button) view.findViewById(R.id.finish);
        finish.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Message message = mainActivity.getFragmentHandler().obtainMessage(0);
        message.obj = "menu";
        message.sendToTarget();
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
