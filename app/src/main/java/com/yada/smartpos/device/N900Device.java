package com.yada.smartpos.device;

import com.newland.mtypex.nseries.NSConnV100ConnParams;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.module.DeviceControllerModule;
import com.yada.smartpos.util.Const.MessageTag;

public class N900Device extends AbstractDevice {

	private String K21_DRIVER_NAME = "com.newland.me.K21Driver";
	private MainActivity mainActivity;
	private DeviceControllerModule controller = null;

	public N900Device(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public void initController() {
		N900DeviceDriver me3xDeviceController = new N900DeviceDriver(mainActivity);
		controller = me3xDeviceController.initMe3xDeviceController(K21_DRIVER_NAME, new NSConnV100ConnParams());
		mainActivity.showMessage("N900设备控制器已初始化!", MessageTag.TIP);

//		if(!((MyApplication) mainActivity.getApplication()).isDukpt()){
//			mainActivity.showMessage("秘钥体系是MK/SK，如若修改请点击秘钥体系进行选择!", MessageTag.ERROR);
//
//		}else{
//			mainActivity.showMessage("秘钥体系是DUKPT，如若修改请点击秘钥体系进行选择", MessageTag.ERROR);
//		}		mainActivity.btnStateToWaitingConn();
	}

	@Override
	public void connectDevice() {
		mainActivity.showMessage("设备连接中..", MessageTag.TIP);
		try {
			controller.connect();
			mainActivity.showMessage("设备连接成功.", MessageTag.TIP);
		} catch (Exception e1) {
			e1.printStackTrace();
			mainActivity.showMessage("链接异常,请检查设备或重新连接...", MessageTag.ERROR);
		}
	}

	@Override
	public void disconnect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (controller != null) {
						controller.disConnect();
						controller = null;
						mainActivity.showMessage("控制器断开成功...", MessageTag.TIP);
					}
				} catch (Exception e) {
					mainActivity.showMessage("控制器断开异常:" + e, MessageTag.TIP);
				}
			}
		}).start();
	}

	@Override
	public DeviceControllerModule getController() {
		return controller;
	}

	@Override
	public boolean isControllerAlive() {
		return controller != null;
	}
}
