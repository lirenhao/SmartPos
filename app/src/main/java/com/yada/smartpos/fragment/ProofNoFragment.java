package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;

/**
 * 输入原凭证号
 */
public class ProofNoFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private FragmentManager manager;
    private Handler handler;

    private TextView inputTxt;
    private String inputValue = "";

    public ProofNoFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        handler = new Handler(mainActivity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2: // 键盘输入
                        inputTxt.setText(setInputTxt());
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keybord, container, false);

        ImageView iv0 = (ImageView) view.findViewById(R.id.iv_0);
        iv0.setOnClickListener(this);
        ImageView iv1 = (ImageView) view.findViewById(R.id.iv_1);
        iv1.setOnClickListener(this);
        ImageView iv2 = (ImageView) view.findViewById(R.id.iv_2);
        iv2.setOnClickListener(this);
        ImageView iv3 = (ImageView) view.findViewById(R.id.iv_3);
        iv3.setOnClickListener(this);
        ImageView iv4 = (ImageView) view.findViewById(R.id.iv_4);
        iv4.setOnClickListener(this);
        ImageView iv5 = (ImageView) view.findViewById(R.id.iv_5);
        iv5.setOnClickListener(this);
        ImageView iv6 = (ImageView) view.findViewById(R.id.iv_6);
        iv6.setOnClickListener(this);
        ImageView iv7 = (ImageView) view.findViewById(R.id.iv_7);
        iv7.setOnClickListener(this);
        ImageView iv8 = (ImageView) view.findViewById(R.id.iv_8);
        iv8.setOnClickListener(this);
        ImageView iv9 = (ImageView) view.findViewById(R.id.iv_9);
        iv9.setOnClickListener(this);
        ImageView ivCancel = (ImageView) view.findViewById(R.id.iv_cancel);
        ivCancel.setOnClickListener(this);
        ImageView ivBackspeace = (ImageView) view.findViewById(R.id.iv_backspeace);
        ivBackspeace.setOnClickListener(this);
        ImageView ivEnter = (ImageView) view.findViewById(R.id.iv_enter);
        ivEnter.setOnClickListener(this);

        TextView inputMsg = (TextView) view.findViewById(R.id.input_tip_msg);
        inputMsg.setText(R.string.proofNo_input);
        inputTxt = (TextView) view.findViewById(R.id.input_pin_txt);
        inputTxt.setGravity(Gravity.LEFT);
        inputTxt.setText(setInputTxt());

        return view;
    }

    @Override
    public void onClick(View view) {
        Message msg = handler.obtainMessage(2);
        switch (view.getId()) {
            case R.id.iv_0:
                inputValue = inputValue + "0";
                msg.sendToTarget();
                break;
            case R.id.iv_1:
                inputValue = inputValue + "1";
                msg.sendToTarget();
                break;
            case R.id.iv_2:
                inputValue = inputValue + "2";
                msg.sendToTarget();
                break;
            case R.id.iv_3:
                inputValue = inputValue + "3";
                msg.sendToTarget();
                break;
            case R.id.iv_4:
                inputValue = inputValue + "4";
                msg.sendToTarget();
                break;
            case R.id.iv_5:
                inputValue = inputValue + "5";
                msg.sendToTarget();
                break;
            case R.id.iv_6:
                inputValue = inputValue + "6";
                msg.sendToTarget();
                break;
            case R.id.iv_7:
                inputValue = inputValue + "7";
                msg.sendToTarget();
                break;
            case R.id.iv_8:
                inputValue = inputValue + "8";
                msg.sendToTarget();
                break;
            case R.id.iv_9:
                inputValue = inputValue + "9";
                msg.sendToTarget();
                break;
            case R.id.iv_cancel:
                manager.popBackStack();
                break;
            case R.id.iv_backspeace:
                if (inputValue.length() > 1) {
                    inputValue = inputValue.substring(0, inputValue.length() - 1);
                } else {
                    inputValue = "";
                }
                msg.sendToTarget();
                break;
            case R.id.iv_enter:
                if (inputValue.length() > 0) {
                    ((App) (mainActivity).getApplication()).getTransData().setOldProofNo(inputValue);
                    ((App) mainActivity.getApplication()).getTransData().setOldTraceNo(inputValue);
                    mainActivity.getProofNoWaitThreat().notifyThread();
                } else {
                    Toast.makeText(mainActivity, "请输入原凭证号！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
    }

    private String setInputTxt() {
        return "原凭证号:  " + inputValue;
    }
}
