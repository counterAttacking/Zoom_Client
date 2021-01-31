package com.kwhy.sundayzoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kwhy.sundayzoom.features.camera.CameraManager;
import com.kwhy.sundayzoom.features.camera.CameraPreview;
import com.kwhy.sundayzoom.features.camera.CameraStreamView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 100001;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 100002;

    private static Camera camera;
    private static CameraPreview cameraPreview;

    private List<CameraStreamView> streamViewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Android Version이 marshmallow이상인 경우
        앱 권한 요청을 진행을 하여 사용자가 런타임에 권한을 승인해야 합니다.
        현재 SundayZoom이 요청하는 권한 : CAMERA, WRITE_EXTERNAL_STORAGE
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                return;
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }

        CameraManager manager = CameraManager.getCameraManager();
        if (!manager.checkCameraUsable(this)) {
            new AlertDialog.Builder(this)
                    .setMessage("Unable to use Camera")
                    .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    })
                    .show();
        }

        Camera camera = manager.getCamera();

        this.cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(this.cameraPreview);

        this.addStreamView(null);

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                MainActivity.this.updateStreamView(data, camera);
            }

        });

        this.camera = camera;
    }

    // 앱 권한 요청에 대한 사용자의 결과를 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 승인이 처리가 된 경우 다시 그리기
                    recreate();
                } else {
                    // 권한 승인이 처리가 되지 않은 경우 종료
                    finish();
                }
                break;
            default:
                break;
        }
    }

    // 전면 후면 카메라 선택
    public void changeCamera(View view) {
        CameraManager manager = CameraManager.getCameraManager();
        Camera camera = manager.getNextCamera();
        this.cameraPreview.changeCamera(camera);
        this.camera = camera;

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                MainActivity.this.updateStreamView(data, camera);
            }
        });
    }

    // 사진 촬영
    public void takePicture(View view) {
        CameraManager cameraManager = CameraManager.getCameraManager();
        cameraManager.takeAndSaveImage(this.camera);
        Toast.makeText(this, "Save Completed", Toast.LENGTH_LONG).show();
    }

    // "Add screen"으로 표시되는 Button을 누르면 다른 화면 추가
    public void addStreamView(View view) {
        final CameraStreamView streamView = new CameraStreamView(this);
        this.streamViewList.add(streamView);

        /*
        화면을 추가하면 stream_list에
        다른 사용자를 보여주는 streamView와 나가기 기능을 추가한 Button이 동시에 담겨있는
        userView라는 LinearLayout을 화면에 출력
         */
        LinearLayout streamLayout = findViewById(R.id.stream_list);
        final LinearLayout userView = new LinearLayout(this);
        userView.setOrientation(LinearLayout.VERTICAL);
        Button closeButton = new Button(this);
        userView.addView(streamView);
        userView.addView(closeButton);
        streamLayout.addView(userView);
        closeButton.setText("Exit");
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.removeStreamView(userView, streamView);
            }
        });
    }

    public void updateStreamView(byte[] data, Camera camera) {
        CameraManager manager = CameraManager.getCameraManager();
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        for (CameraStreamView stream : this.streamViewList) {
            stream.drawStream(bytes, parameters.getJpegThumbnailSize(), manager.isFrontCamera());
        }
    }

    public void removeStreamView(LinearLayout view, CameraStreamView streamView) {
        LinearLayout streamLayout = findViewById(R.id.stream_list);
        streamLayout.removeViewInLayout(view);
        this.streamViewList.remove(streamView);
    }
}
