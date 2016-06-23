package com.yada.smartpos.handler;

import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.util.ISOUtils;
import com.newland.pos.sdk.util.BytesUtils;
import com.yada.sdk.packages.PackagingException;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.PinInputModule;
import com.yada.smartpos.module.impl.PinInputModuleImpl;
import com.yada.smartpos.util.Const;

import java.io.IOException;

public class SignInHandler {

    private MainActivity mainActivity;

    public SignInHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void signIn() throws IOException, PackagingException {
        mainActivity.getTraner().singIn();
    }

    public void paramDownload() throws IOException, PackagingException {
        mainActivity.getTraner().paramDownload();
    }

    private void loadWorkingKey(String field48) {
        String tpk = field48.substring(5, 37);
        PinInputModule pinInput = new PinInputModuleImpl();
        byte[] kcv = pinInput.loadWorkingKey(WorkingKeyType.PININPUT, 2,
                Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX, ISOUtils.hex2byte(tpk), null);
        System.out.println(field48.substring(39, 55));
        System.out.println(BytesUtils.bytesToHex(kcv));
    }
}
