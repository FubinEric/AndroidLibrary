package com.eric.camera.recognition.thread;

import android.graphics.Rect;

public interface ProcessPreviewDataListener {

    String processData(byte[] data, int width, int height, Rect previewRect);
}
