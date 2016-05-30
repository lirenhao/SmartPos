package com.yada.smartpos.module;

import android.content.Context;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.UpdateAppListener;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;

import java.util.Date;

public interface DeviceControllerModule {
	/**
	 * 初始化设备控制器
	 * 
	 */
	public void init(Context context, String driverName, DeviceConnParams params, DeviceEventListener<ConnectionCloseEvent> listener);

	/**
	 * 销毁连接控制器，释放相关资源
	 * @since ver1.0
	 */
	public void destroy();

	/**
	 * 连接设备
	 * 
	 * @since ver1.0
	 * @throws Exception
	 */
	public void connect() throws Exception;

	/**
	 * 连接中断
	 * 
	 * @since ver1.0
	 */
	public void disConnect();

	/**
	 * 判断连接是否正常
	 * 
	 * @since ver1.0
	 */
	public void isConnected();

	/**
	 * 返回当前设备状态
	 * 
	 * @return 当前设备状态
	 * @throws UnsupportedOperationException
	 *             当不支持该方法时抛出该异常
	 */
	public DeviceConnState getDeviceConnState();

	/**
	 * 获得当前设备内时间
	 * <p>
	 * 
	 */
	public Date getDeviceDate();

	/**
	 * 设置设备内部时间
	 */
	public void setDeviceDate(Date date);

	/**
	 * 设置终端参数
	 * @param tag
	 * @param value
     */
	public void setDeviceParams(int tag, byte[] value);

	/**
	 * 获得一个终端参数列表
	 * 
	 * @return
	 */
	public byte[] getDeviceParams(int tag);

	/**
	 * 更新设备应用（支持断点续传）
	 * @param filePath 待更新文件
	 * @param listener 固件更新监听
	 * @param deviant 内容偏移值
     */
	public void updateApp(String filePath, UpdateAppListener listener, int deviant);

	/**
	 * 获得当前设备内电池信息
	 * 
	 * @return
	 * 
	 */
	public BatteryInfoResult getBatteryInfo();

	/**
	 * 设备软复位。
	 */
	public void reset();

	/**
	 * 获得设备信息
	 * 
	 * @return
	 */
	public DeviceInfo getDeviceInfo();

}
