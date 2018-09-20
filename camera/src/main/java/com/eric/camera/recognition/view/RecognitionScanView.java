package com.eric.camera.recognition.view;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.eric.camera.R;
import com.eric.camera.recognition.thread.ProcessPreviewDataListener;
import com.eric.camera.recognition.thread.ProcessPreviewDataThread;
import com.eric.camera.recognition.utils.RecognitionUtils;


/**
 * 摄像头扫描框Layout
 */
public abstract class RecognitionScanView extends RelativeLayout implements Camera.PreviewCallback, ProcessPreviewDataListener {
    protected Camera mCamera;
    protected RecognitionSurfaceView mPreview;
    protected RecognitionScanBoxView mCameraPreviewBoxView;
    protected ProcessDataResultListener listener;
    protected Handler mHandler;
    protected boolean isStartSpot = false;
    private ProcessPreviewDataThread thread;

    public RecognitionScanView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecognitionScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleMsg(msg);
                return true;
            }
        });
        initView(context, attrs);
    }

    private void handleMsg(Message msg) {
        if (msg.what == ProcessPreviewDataThread.ProcessDataWhat) {
            String result = (String) msg.obj;
            if (result != null && !result.isEmpty() && listener != null) {
                listener.onProcessDataSuccess(result);
            } else {
                startSpot();
            }
        }
    }

    protected void initView(Context context, AttributeSet attrs) {
        mPreview = new RecognitionSurfaceView(getContext());
        mCameraPreviewBoxView = new RecognitionScanBoxView(getContext(), attrs);
        mPreview.setId(R.id.camera_surface_view);
        addView(mPreview);
        LayoutParams layoutParams = new LayoutParams(context, attrs);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, mPreview.getId());
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mPreview.getId());
        addView(mCameraPreviewBoxView, layoutParams);
    }

    public RecognitionScanBoxView getScanBoxView() {
        return mCameraPreviewBoxView;
    }

    public void setListener(ProcessDataResultListener listener) {
        this.listener = listener;
    }

    /**
     * 打开后置摄像头开始预览，但是并未开始识别
     */
    public void startCamera() {
        startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * 打开指定摄像头开始预览，但是并未开始识别
     *
     * @param cameraFacing
     */
    private void startCamera(int cameraFacing) {
        if (mCamera != null) {
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                startCameraById(cameraId);
                break;
            }
        }
    }

    private void startCameraById(int cameraId) {
        try {
            mCamera = Camera.open(cameraId);
            mPreview.setCamera(mCamera);
        } catch (Exception e) {
            if (listener != null) {
                listener.onCameraError();
            }
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera() {
        try {
            if (mCamera != null) {
                mPreview.stopCameraPreview();
                mPreview.setCamera(null);
                mCamera.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera = null;
    }

    /**
     * 延迟0.5秒后开始识别
     */
    public void startSpot() {
        startSpotDelay(500);
    }

    /**
     * 延迟delay毫秒后开始识别
     *
     * @param delay
     */
    public void startSpotDelay(int delay) {
        isStartSpot = true;

        startCamera();
        if (delay == 0) {
            mCamera.setOneShotPreviewCallback(RecognitionScanView.this);
            return;
        }
        // 开始前先移除之前的任务
        if (mOneShotPreviewCallbackTask != null) {
            mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
            mHandler.postDelayed(mOneShotPreviewCallbackTask, delay);
        }
    }

    /**
     * 停止识别
     */
    public void stopSpot() {
        cancelProcessDataTask();

        isStartSpot = false;

        if (mCamera != null) {
            try {
                mCamera.setOneShotPreviewCallback(null);
            } catch (Exception e) {
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        }
    }


    public void onStart() {
        startCamera();
        startSpot();
    }

    public void onStop() {
        stopSpot();
        stopCamera();
    }

    /**
     * 打开闪光灯
     */
    public void openFlashlight() {
        mPreview.openFlashlight();
    }

    /**
     * 关闭散光灯
     */
    public void closeFlashlight() {
        mPreview.closeFlashlight();
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        mHandler = null;
        listener = null;
        mOneShotPreviewCallbackTask = null;
    }

    /**
     * 取消数据处理任务
     */
    protected void cancelProcessDataTask() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }


    /**
     * 判断是否是二维码扫描模式
     *
     * @return
     */
    public boolean isQrCodeStyle() {
        return mCameraPreviewBoxView.isQrCodeStyle();
    }

    /**
     * 设置二维码扫描模式
     *
     * @param isQrCodeStyle
     */
    public void setQrCodeStyle(boolean isQrCodeStyle) {
        mCameraPreviewBoxView.setQrCodeStyle(isQrCodeStyle);
    }

    @Override
    public void onPreviewFrame(byte[] data, final Camera camera) {
        if (isStartSpot) {
            onResultPreview(data, camera);
        }
    }

    protected void onResultPreview(byte[] data, Camera camera) {
        cancelProcessDataTask();
        thread = getProcessDataThread(getContext(), data, camera, getScanBoxView().getPreviewRect(), mHandler);
        thread.setProcessPreviewDataListener(this);
        thread.start();
    }

    public ProcessPreviewDataThread getProcessDataThread(Context context, byte[] data, Camera camera, Rect previewRect, Handler mHandler) {
        return new ProcessPreviewDataThread(getContext(), data, camera, getScanBoxView().getPreviewRect(), mHandler);
    }

    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && isStartSpot) {
                try {
                    mCamera.setOneShotPreviewCallback(RecognitionScanView.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public String processData(byte[] data, int width, int height, Rect previewRect) {
        return null;
    }
}