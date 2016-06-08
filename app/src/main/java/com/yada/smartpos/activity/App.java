package com.yada.smartpos.activity;

import android.app.Application;
import com.yada.smartpos.model.TransData;
import com.yada.smartpos.model.TransResult;

public class App extends Application {

    private TransData transData;

    private TransResult transResult;

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
