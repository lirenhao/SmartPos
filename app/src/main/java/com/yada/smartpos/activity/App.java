package com.yada.smartpos.activity;

import android.app.Application;
import com.yada.sdk.device.pos.posp.params.Block01;
import com.yada.sdk.device.pos.posp.params.Block02;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;

public class App extends Application {

    private Block01 paramBlock01;

    private Block02 paramBlock02;

    private TransData transData;

    private TransResult transResult;

    public Block01 getParamBlock01() {
        return paramBlock01;
    }

    public void setParamBlock01(Block01 paramBlock01) {
        this.paramBlock01 = paramBlock01;
    }

    public Block02 getParamBlock02() {
        return paramBlock02;
    }

    public void setParamBlock02(Block02 paramBlock02) {
        this.paramBlock02 = paramBlock02;
    }

    public TransData getTransData() {
        return transData;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    public TransResult getTransResult() {
        return transResult;
    }

    public void setTransResult(TransResult transResult) {
        this.transResult = transResult;
    }
}
