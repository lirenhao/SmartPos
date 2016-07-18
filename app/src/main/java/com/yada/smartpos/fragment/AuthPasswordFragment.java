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
import android.widget.Toast;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.MainActivity;

public class AuthPasswordFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private FragmentManager manager;
    private Handler handler;

    private TextView inputTxt;
    private int inputLen = 0;

    public AuthPasswordFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        handler = new Handler(mainActivity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2: // 键盘输入
                        StringBuilder buffer = new StringBuilder();
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
        inputMsg.setText(R.string.pin_supervisor);
        inputTxt = (TextView) view.findViewById(R.id.input_pin_txt);

        return view;
    }

    @Override
    public void onClick(View view) {
        Message msg = handler.obtainMessage(2);
        switch (view.getId()) {
            case R.id.iv_cancel:
                manager.popBackStack();
                break;
            case R.id.iv_backspeace:
                if (inputLen > 1) {
                    inputLen = inputLen - 1;
                }
                msg.sendToTarget();
                break;
            case R.id.iv_enter:
                if (inputLen == 6) {
                    mainActivity.getAuthPasswordWaitThreat().notifyThread();
                } else {
                    Toast.makeText(mainActivity, "请输入6位主管密码！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                if (inputLen < 6) {
                    inputLen = inputLen + 1;
                }
                msg.sendToTarget();
        }
    }
}
