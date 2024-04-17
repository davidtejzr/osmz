package com.example.osmzhttpserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Camera.PreviewCallback {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;

    private static Camera mCamera;
    public static byte[] pictureData;
    public static int rotation = 0;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.append(msg.getData().getString("address") + " -  - [" + msg.getData().getString("timestamp") + "] \"" + msg.getData().getString("req") + "\"\n");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotation = Utils.getCurrentRotation(getWindowManager().getDefaultDisplay().getRotation());

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        Camera mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        mCamera.setDisplayOrientation(rotation);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    public void onClick(View v) {

        Context context = getApplicationContext();
        if (v.getId() == R.id.button1) {
                s = new SocketServer(context, handler);
                s.start();

        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Context context = getApplicationContext();
                s = new SocketServer(context, handler);
                s.start();
            }
        }
    }

    public static Camera getCameraInstance(){
        if (mCamera == null) {
            try {
                mCamera = Camera.open(0);
                mCamera.setPreviewCallback((data, camera) -> {
                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    pictureData = Utils.NV21toJPEGRotated(data, previewSize.width, previewSize.height, rotation);
                    Log.d("MJPEG Stream", "Picture taken");
                });
            }
            catch (Exception ignored){
            }
        }
        return mCamera;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        pictureData = Utils.NV21toJPEG(data, previewSize.width, previewSize.height);
    }
}
