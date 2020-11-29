package com.kwhy.sundayzoom.features.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;


public class CameraManager {
    private static CameraManager cameraManager;

    private CameraManager() {
    }

    // CameraManager Class는 Singleton Class
    public static CameraManager getCameraManager() {
        if (cameraManager == null) {
            cameraManager = new CameraManager();
        }
        return cameraManager;
    }


    // Device에 존재하는 카메라를 사용할 수 있는지 파악
    public boolean checkCameraUsable(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public Camera getCamera() {
        Camera camera = null;

        try {
            // device의 camera 사용할 수 있도록
            camera = Camera.open();
            // camera에 대한 기능들을 가져옴
            Camera.Parameters cameraParameters = camera.getParameters();
            // camera가 Focus를 자동으로 조절 및 사용
            if (cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(cameraParameters);
            }
        } catch (Exception ex) {
            Log.e("CameraManager", ex.toString());
            System.exit(1);
        }

        return camera;
    }
}
