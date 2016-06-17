package com.yada.smartpos.spos;

import com.yada.sdk.device.pos.ISequenceGenerator;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.util.SharedPreferencesUtil;

public class SequenceGenerator implements ISequenceGenerator {

    private MainActivity mainActivity;
    private String name;

    public SequenceGenerator(MainActivity mainActivity, String name) {
        this.mainActivity = mainActivity;
        this.name = name;
    }

    @Override
    public int getSequence() {
        int ret = SharedPreferencesUtil.getIntParam(mainActivity, name);
        ret = ret > 999999 ? 0 : ret;
        SharedPreferencesUtil.setIntParam(mainActivity, name, ret + 1);
        return ret;
    }
}
