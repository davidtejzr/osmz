package com.example.osmzhttpserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Camera.PreviewCallback {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;

    public final String TAG = "MainActivity";
    public static byte[] pictureData;

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

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        Camera mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
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

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0);
            c.setPreviewCallback(this);
        }
        catch (Exception ignored){
        }
        return c;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        byte[] jpegArray = Utils.NV21toJPEG(data, previewSize.width, previewSize.height, 100);
        pictureData = jpegArray;
    }
}
