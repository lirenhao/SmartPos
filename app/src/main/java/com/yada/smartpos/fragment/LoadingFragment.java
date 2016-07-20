package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.MainActivity;

public class LoadingFragment extends Fragment {

    private MainActivity mainActivity;

    public LoadingFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        return view;
    }
}
