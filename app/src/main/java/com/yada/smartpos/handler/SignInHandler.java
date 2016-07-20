package com.yada.smartpos.handler;

import com.yada.sdk.packages.PackagingException;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.TransHandleListener;

import java.io.IOException;

public class SignInHandler {

    private MainActivity mainActivity;
    private TransHandleListener handleListener;

    public SignInHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handleListener = new TransHandleListener(mainActivity);
    }

    public void signIn() throws IOException, PackagingException {
        handleListener.loadingView();
        mainActivity.getVirtualPos().resetSingIn();
        mainActivity.getVirtualPos().resetParamDownload();
        mainActivity.getVirtualPos().createTraner();
    }

    public void paramDownload() throws IOException, PackagingException {
        mainActivity.getVirtualPos().resetSingIn();
        mainActivity.getVirtualPos().resetParamDownload();
        mainActivity.getVirtualPos().createTraner();
    }
}
