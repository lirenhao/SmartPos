package com.yada.smartpos.module.impl;

import android.content.Context;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.*;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.ISOUtils;
import com.yada.smartpos.module.DeviceControllerModule;

import java.io.File;
import java.util.Date;

/**
 * 设备控制器接口实现
 */
public class DeviceControllerModuleImpl implements DeviceControllerModule {
	private static String DRIVER_NAME = "";


	private static DeviceManager deviceManager = ConnUtils.getDeviceManager();
	public static DeviceManager getDeviceManager() {
		return deviceManager;
	}

	public static void setDeviceManager(DeviceManager deviceManager) {
		DeviceControllerModuleImpl.deviceManager = deviceManager;
	}

	private DeviceConnParams connParams;
	private Context context;

	private DeviceControllerModuleImpl(String driverName) {
		this.DRIVER_NAME = driverName;
	}

	public void init(Context context, String driverName, DeviceConnParams params, DeviceEventListener<ConnectionCloseEvent> listener) {
		deviceManager.init(context, driverName, params, listener);
		this.context = context;
		this.connParams = params;

	}

	public DeviceConnParams getDeviceConnParams() {
		Device device = deviceManager.getDevice();
		if (device == null)
			return null;

		return (DeviceConnParams) device.getBundle();
	}

	public static DeviceControllerModule getInstance(String driverName) {
		return new DeviceControllerModuleImpl(driverName);
	}

	@Override
	public void connect() throws Exception {
		deviceManager.connect();
		deviceManager.getDevice().setBundle(connParams);
	}

	@Override
	public void disConnect() {
		deviceManager.disconnect();
	}

	@Override
	public void destroy() {
		deviceManager.destroy();
		deviceManager.getDevice().destroy();
	}

	@Override
	public void isConnected() {
		synchronized (DRIVER_NAME) {
			if (null == deviceManager || deviceManager.getDevice() == null) {
				throw new DeviceOutofLineException("无法连接设备!!");
			}
		}
	}

	public DeviceConnState getDeviceConnState() {
		return deviceManager.getDeviceConnState();
	}

	@Override
	public Date getDeviceDate() {
		return deviceManager.getDevice().getDeviceDate();
	}

	@Override
	public void setDeviceDate(Date date) {
		deviceManager.getDevice().setDeviceDate(date);

	}

	@Override
	public void setDeviceParams(int tag, byte[] value) {
		TLVPackage tlvpackage = ISOUtils.newTlvPackage();
		tlvpackage.append(tag, value);
		deviceManager.getDevice().setDeviceParams(tlvpackage);
	}

	@Override
	public byte[] getDeviceParams(int tag) {
		TLVPackage pack = deviceManager.getDevice().getDeviceParams(tag);
		return pack.getValue(getOrginTag(tag));
	}

	private int getOrginTag(int tag) {
		if ((tag & 0xFF0000) == 0xFF0000) {
			return tag & 0xFFFF;
		} else if ((tag & 0xFF00) == 0xFF00) {
			return tag & 0xFF;
		}
		return tag;
	}

	//更新
	@Override
	public void updateApp(String filePath, UpdateAppListener listener, int deviant) {
		deviceManager.getDevice().updateApp(new File(filePath), listener, deviant);

	}

	//获取电池电量
	@Override
	public BatteryInfoResult getBatteryInfo() {
		return deviceManager.getDevice().getBatteryInfo();
	}

	@Override
	public void reset() {
		deviceManager.getDevice().reset();
	}

	//获取设备信息
	@Override
	public DeviceInfo getDeviceInfo() {
		return deviceManager.getDevice().getDeviceInfo();
	}
}
