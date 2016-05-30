package com.yada.smartpos.event;

public interface PrinterListener {
	/**
     * 打印错误
     * @param errorCode 错误码
     * @param detail 错误详情
     */
    void onError(int errorCode, String detail);
    
/**
     * 打印完成
     */
    void onFinish();
/**
     * 开始打印
     */
    void onStart();
}
