package com.kwhy.sundayzoom.features.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
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
            this.camera.setPreviewDisplay(holder);
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
            this.camera.startPreview();
        } catch (Exception e) {
            Log.d("CameraPreview", "Fail to create Preview : " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
