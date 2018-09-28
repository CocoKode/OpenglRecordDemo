package com.crearo.recordapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.crearo.recordapplication.controlers.CameraControler;
import com.crearo.recordapplication.controlers.CameraDrawer;
import com.crearo.recordapplication.records.MediaVideoEncoder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aa on 2018/4/27.
 */

public class CameraView extends GLSurfaceView implements SurfaceHolder.Callback, GLSurfaceView.Renderer{

    private SurfaceHolder mSurfaceHolder;
    private CameraControler mCameraControler;
    private CameraDrawer mCameraDrawer;
    public int mVideoWidth, mVideoHeight;
    public int mRotation;
    private boolean isCameraOpen;
    private boolean isFirst;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mCameraControler = new CameraControler(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraDrawer = new CameraDrawer(this);
        mCameraDrawer.onSurfaceCreated(gl10, eglConfig);
        if (mCameraControler != null) {

            mCameraControler.setPreviewSize(mVideoWidth, mVideoHeight);
            if (mCameraControler.openCamera()) {
                isCameraOpen = true;
            } else {
                isCameraOpen = false;
                Looper.prepare();
                Toast.makeText(this.getContext(), "相机开启失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            mCameraControler.setDisplay(mCameraDrawer.getSurfaceTexture());
            mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    requestRender();
                }
            });
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if (!isFirst) {
            mCameraDrawer.onSurfaceChanged(gl10, width, height);
//        mCameraControler.setPreviewSize(getWidth(), getHeight());
//        mCameraControler.setPreviewSize(width, height);
            if (isCameraOpen) {
                mCameraControler.preview();
            }

            isFirst = true;
        }

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mCameraDrawer.onDrawFrame(gl10);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCameraControler.release();
    }


    public void setPreviewSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    public void resetVideoSize(final int width, final int height) {
        if ((mRotation % 180) == 0) {
            mVideoWidth = width;
            mVideoHeight = height;
        } else {
            mVideoWidth = height;
            mVideoHeight = width;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.updateViewport();
            }
        });
    }

    public void changeBitmap(Bitmap bitmap) {
        if (mCameraDrawer != null) {
            mCameraDrawer.changeBitmap(bitmap);
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (mCameraDrawer) {
                    if (encoder != null) {
                        encoder.setEglContext(EGL14.eglGetCurrentContext(), mCameraDrawer.mOutTexture);
                    }
                    mCameraDrawer.mVideoEncoder = encoder;
                }
            }
        });
    }
}
