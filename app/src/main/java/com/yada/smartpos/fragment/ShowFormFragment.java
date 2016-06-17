package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;

public class ShowFormFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;

    public ShowFormFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_form, container, false);

        Button finish = (Button) view.findViewById(R.id.finish);
        finish.setOnClickListener(this);

        TextView transCardNo = (TextView) view.findViewById(R.id.transCardNo);
        transCardNo.setText(((App)mainActivity.getApplication()).getTransData().getAccount());

        TextView transProofNo = (TextView) view.findViewById(R.id.transProofNo);
        transProofNo.setText(((App)mainActivity.getApplication()).getTransData().getOldProofNo());

        TextView transAuthCode = (TextView) view.findViewById(R.id.transAuthCode);
        transAuthCode.setText(((App)mainActivity.getApplication()).getTransData().getOldAuthCode());

        TextView transReferenceNumber = (TextView) view.findViewById(R.id.transReferenceNumber);
        transReferenceNumber.setText(((App)mainActivity.getApplication()).getTransData().getOldTraceNo());

        TextView transDateTime = (TextView) view.findViewById(R.id.transDateTime);
        transDateTime.setText(((App)mainActivity.getApplication()).getTransData().getOldTransDate()+
                ((App)mainActivity.getApplication()).getTransData().getOldTransTime());

        TextView transAmount = (TextView) view.findViewById(R.id.transAmount);
        transAmount.setText(((App) mainActivity.getApplication()).getTransData().getAmount().movePointLeft(2).toString());

        return view;
    }

    @Override
    public void onClick(View v) {
        mainActivity.getShowFormWaitThreat().notifyThread();
    }
}
