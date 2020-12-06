package com.kwhy.sundayzoom.features.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class CameraManager {
    private static CameraManager cameraManager;
    private static int maxCamera;
    // 현재 device가 사용하는 카메라
    private static int currentCamera = 0;

    private CameraManager() {
    }

    // CameraManager Class는 Singleton Class
    public static CameraManager getCameraManager() {
        if (cameraManager == null) {
            cameraManager = new CameraManager();
        }
        // Device에 존재하는 카메라의 개수가 몇 개인지 파악
        maxCamera = Camera.getNumberOfCameras();
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

    // 전면 후면 카메라 선택
    public Camera getNextCamera() {
        Camera camera = null;

        try {
            // 카메라를 선택
            currentCamera = (currentCamera + 1) % maxCamera;
            // 선택한 카메라를 사용할 수 있도록
            camera = Camera.open(currentCamera);
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

    // 촬영
    public void takeAndSaveImage(Camera camera) {
        camera.takePicture(null, null, getTakePictureCallback());
    }

    // 파일 생성 Handler
    private Camera.PictureCallback getTakePictureCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.startPreview();
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.e("CameraManager", "Fail to create file");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("CameraManager", "Fail to search file" + e.getMessage());
                } catch (IOException e) {
                    Log.e("CameraManager", "Fail to access file" + e.getMessage());
                } catch (Exception e) {
                    Log.e("CameraManager", "Fail to save image file" + e.getMessage());
                }
            }
        };
    }

    // 이미지가 저장될 디렉토리를 생성하고 이미지 파일 저장
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SundayZoomPictures"
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("CameraManager", "Fail to create file directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMDDHHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
