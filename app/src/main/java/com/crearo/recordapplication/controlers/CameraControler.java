package com.crearo.recordapplication.controlers;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.crearo.recordapplication.CameraView;
import com.crearo.recordapplication.MyApplication;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aa on 2018/4/27.
 */

public class CameraControler implements Camera.PreviewCallback{

    private static final String TAG = "CameraControler";
    private Camera mCamera;
    private int mPreviewWidth, mPreviewHeight;
    public static int mCameraID = 0;

    private WeakReference<CameraView> mCameraViewReference;

    public CameraControler(CameraView mCameraView) {
        mCameraViewReference = new WeakReference(mCameraView);
    }

    public boolean openCamera() {
        try {
            if (mCamera == null) {
                mCamera = Camera.open(mCameraID);
                Camera.Parameters params = mCamera.getParameters();
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
                int[] maxFps = supportedFpsRanges.get(supportedFpsRanges.size() - 1);
                params.setPreviewFpsRange(maxFps[0], maxFps[1]);
                params.setRecordingHint(true);
                Camera.Size previewSize = getCloestSize(
                        params.getSupportedPreviewSizes(),
                        mPreviewWidth, mPreviewHeight);
                params.setPreviewSize(previewSize.width, previewSize.height);
                Camera.Size pictureSize = getCloestSize(
                        params.getSupportedPictureSizes(),
                        mPreviewWidth, mPreviewHeight);
                params.setPictureSize(pictureSize.width, pictureSize.height);
                setCameraRotation(previewSize.width, previewSize.height);
                mCamera.setParameters(params);
//            mCamera.setPreviewCallback(this);
                return true;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private Camera.Size getCloestSize(List<Camera.Size> supportedSizes, final int mWidth, final int mHeight) {
        return Collections.min(supportedSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size s1, Camera.Size s2) {
                return diff(s1) - diff(s2);
            }

            private int diff(Camera.Size s1) {
                return Math.abs(s1.width - mWidth) + Math.abs(s1.height - mHeight);
            }
        });
    }
    private void setCameraRotation(final int width, final int height) {
        final CameraView cameraView =  mCameraViewReference.get();
        if (cameraView != null) {
            WindowManager windowManager = (WindowManager) cameraView.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            int rotation = windowManager.getDefaultDisplay().getRotation();
            int angle = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    angle = 0;
                    break;
                case Surface.ROTATION_90:
                    angle = 90;
                    break;
                case Surface.ROTATION_180:
                    angle = 180;
                    break;
                case Surface.ROTATION_270:
                    angle = 270;
                    break;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraID, info);
            if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                angle = (info.orientation + angle) % 360;
                angle = (360 - angle) % 360;
            } else {
                angle = (info.orientation - angle + 360) % 360;
            }
            mCamera.setDisplayOrientation(angle);
            cameraView.mRotation = angle;
            cameraView.post(new Runnable() {
                @Override
                public void run() {
                    cameraView.resetVideoSize(width, height);
                }
            });
        }
    }

    public void preview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setDisplay(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDisplay(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CameraControler setPreviewSize(int width, int height) {
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
        return this;
    }

    public CameraControler setCameraID(int id) {
        this.mCameraID = id;
        return this;
    }

    private int frameCount = 0;
    private long startTime = 0l;
    private long endTime = 0l;

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        frameCount++;
		if (startTime == 0l) {
			startTime = System.currentTimeMillis();
		} else {
			endTime = System.currentTimeMillis();
			int seconds = (int) ((endTime - startTime) / 1000);
			double frameRate = (double) frameCount / seconds;
			Log.i("preview", "seconds:" + seconds + "," + "frameRate:" + frameRate);
		}
    }
}
