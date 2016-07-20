package com.yada.smartpos.handler;

import com.yada.sdk.packages.PackagingException;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.event.TransHandleListener;
import com.yada.smartpos.util.Const;

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
        boolean result = mainActivity.getVirtualPos().signIn();
        handleListener.menuView();
        if (result) {
            mainActivity.showMessage("签到完成！", Const.MessageTag.NORMAL);
        } else {
            mainActivity.showMessage("签到失败！", Const.MessageTag.NORMAL);
        }

    }

    public void paramDownload() throws IOException, PackagingException {
        handleListener.loadingView();
        boolean result = mainActivity.getVirtualPos().paramDownload();
        handleListener.menuView();
        if (result) {
            mainActivity.showMessage("参数下载完成！", Const.MessageTag.NORMAL);
        } else {
            mainActivity.showMessage("参数下载失败！", Const.MessageTag.NORMAL);
        }
    }
}
