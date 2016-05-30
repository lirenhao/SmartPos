package com.yada.smartpos.device;

import android.os.Handler;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.DeviceControllerModule;
import com.yada.smartpos.module.impl.DeviceControllerModuleImpl;
import com.yada.smartpos.util.Const.MessageTag;

/**
 * 初始化设备控制器
 */
public class N900DeviceDriver {

	private MainActivity mainActivity;
	private DeviceControllerModule controller;

	public N900DeviceDriver(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public DeviceControllerModule initMe3xDeviceController(String driverPath, DeviceConnParams params) {
		controller = DeviceControllerModuleImpl.getInstance(driverPath);
		controller.init(mainActivity, driverPath, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {
					mainActivity.showMessage("设备被客户主动断开！", MessageTag.NORMAL);
				}
				if (event.isFailed()) {
					mainActivity.showMessage("设备链接异常断开！", MessageTag.ERROR);
				}
			}

			@Override
			public Handler getUIHandler() {
				return null;
			}
		});
		return controller;
	}

}
