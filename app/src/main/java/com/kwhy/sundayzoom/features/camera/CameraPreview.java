package com.kwhy.sundayzoom.features.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    // SurfaceHolder는 SurfaceView를 관리
    private SurfaceHolder holder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        this.holder = getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // 미리보기를 holder가 관리
            this.camera.setPreviewDisplay(this.holder);
            this.camera.setDisplayOrientation(this.getDegree());
            this.camera.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview", "Fail to create Preview : " + e.getMessage());
        }
    }


    // 화면이 90도, 180도 등 다양한 방향으로 움직이는 경우 화면을 다시 출력하도록
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (this.holder.getSurface() == null) {
            return;
        }

        try {
            this.camera.stopPreview();
        } catch (Exception e) {

        }

        try {
            this.camera.setPreviewDisplay(this.holder);
            this.camera.setDisplayOrientation(this.getDegree());
            this.camera.startPreview();
        } catch (Exception e) {
            Log.d("CameraPreview", "Fail to create Preview : " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            // 화면을 다시 그릴때, camera의 release가 제대로 이루어지지 않는 경우
            // camera의 release를 강제로 실행
            this.camera.stopPreview();
            this.camera.setPreviewCallback(null);
            this.camera.release();
            this.camera = null;
        } catch (Exception ex) {

        }
    }

    // 카메라의 각도를 설정
    private int getDegree() {
        Activity currentActivity = (Activity) (this.getContext());
        int rotation = currentActivity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default:
                return 90;
        }
    }

    // 전면 후면 카메라가 변경이 되었을 경우 내 쪽 미리보기 화면 다시 설정
    public void changeCamera(Camera newCamera) {
        try {
            this.camera.stopPreview();
            this.camera.setPreviewCallback(null);
            this.camera.release();
            this.camera = null;
        } catch (Exception e) {

        }

        try {
            newCamera.setPreviewDisplay(this.holder);
            newCamera.setDisplayOrientation(this.getDegree());
            newCamera.startPreview();
            this.camera = newCamera;
        } catch (Exception e) {
            Log.d("CameraPreview", "Fail to create Preview : " + e.getMessage());
        }
    }

}
