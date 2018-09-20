package com.eric.camera.recognition.view;

/**
 *
 */
public interface ProcessDataResultListener {
    /**
     * 处理扫描结果
     *
     * @param result
     */
    void onProcessDataSuccess(String result);

    /**
     * 处理打开相机出错
     */
    void onCameraError();
}
