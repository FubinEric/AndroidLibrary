package com.eric.camera.recognition.thread;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;

import com.eric.camera.recognition.utils.RecognitionUtils;

/**
 * 处理预览数据
 */
public class ProcessPreviewDataThread extends Thread {

    public static final int ProcessDataWhat = 0x01;
    private Context context;
    private byte[] mData;
    private Camera camera;
    private Rect previewRect;
    private Handler mHandler;
    private ProcessPreviewDataListener processPreviewDataListener;


    public ProcessPreviewDataThread(Context context, byte[] data, Camera camera, Rect previewRect, Handler mHandler) {
        this.context = context;
        this.mData = data;
        this.camera = camera;
        this.previewRect = previewRect;
        this.mHandler = mHandler;
    }

    public void setProcessPreviewDataListener(ProcessPreviewDataListener processPreviewDataListener) {
        this.processPreviewDataListener = processPreviewDataListener;
    }

    @Override
    public void run() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        byte[] data = mData;

        if (RecognitionUtils.getOrientation(context) == RecognitionUtils.ORIENTATION_PORTRAIT) {
            data = new byte[mData.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    data[x * height + height - y - 1] = mData[x + y * width];
                }
            }
            int tmp = width;
            width = height;
            height = tmp;
        }
        if (processPreviewDataListener != null) {
            processData(data, width, height);
        } else {
            sendResult("");
        }
    }

    public void processData(byte[] data, int width, int height) {
        try {
            String result = processPreviewDataListener.processData(data, width, height, previewRect);
            sendResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            sendResult("");
        }
    }

    private void sendResult(String result) {
        if (result == null) {
            result = "";
        }
        mHandler.obtainMessage(ProcessDataWhat, result).sendToTarget();
    }
}
