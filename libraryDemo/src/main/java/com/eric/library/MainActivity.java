package com.eric.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.eric.camera.recognition.view.ProcessDataResultListener;
import com.eric.camera.recognition.zxing.ZxingView;

public class MainActivity extends AppCompatActivity implements ProcessDataResultListener {

    private ZxingView zxingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zxingView = findViewById(R.id.zxingView);
        zxingView.setListener(this);
    }

    @Override
    protected void onStart() {
        zxingView.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        zxingView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zxingView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onProcessDataSuccess(String result) {
        Toast.makeText(this,"结果:"+result,Toast.LENGTH_SHORT).show();
        zxingView.startSpot();
    }

    @Override
    public void onCameraError() {

    }
}
