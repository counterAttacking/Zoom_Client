package com.kwhy.sundayzoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.kwhy.sundayzoom.features.camera.CameraManager;
import com.kwhy.sundayzoom.features.camera.CameraPreview;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 100001;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Android Version이 marshmallow이상인 경우
        앱 권한 요청을 진행을 하여 사용자가 런타임에 권한을 승인해야 합니다.
        현재 SundayZoom이 요청하는 권한 : CAMERA
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
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
    }


    // 앱 권한 요청에 대한 사용자의 결과를 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
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
}
