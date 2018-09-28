package com.crearo.recordapplication.controlers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.crearo.recordapplication.CameraView;
import com.crearo.recordapplication.MyApplication;
import com.crearo.recordapplication.R;
import com.crearo.recordapplication.filters.BaseFilter;
import com.crearo.recordapplication.filters.DrawFilter;
import com.crearo.recordapplication.filters.EffectFilter;
import com.crearo.recordapplication.filters.OesFilter;
import com.crearo.recordapplication.filters.WatermarkFilter;
import com.crearo.recordapplication.records.MediaVideoEncoder;
import com.crearo.recordapplication.utils.EasyGlUtils;
import com.crearo.recordapplication.utils.Gl2Utils;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aa on 2018/4/27.
 */

public class CameraDrawer implements GLSurfaceView.Renderer{

    private static final String TAG = "CameraDrawer";
    private WeakReference<CameraView> mCameraViewReference;
    private BaseFilter mBufferFilter;
    private EffectFilter mEffectFilter;
    private BaseFilter mShowFilter;
    private WatermarkFilter mWatermarkFilter;
    public int mOutTexture;
    private SurfaceTexture mSurfaceTexture;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int mTexWidth, mTexHeight;
    private int mViewWidth, mViewHeight;
    private float[] mMatrix = new float[16];
    private float[] mTexMatrix = new float[16];
    public MediaVideoEncoder mVideoEncoder;

    public CameraDrawer(CameraView cameraView) {
        mBufferFilter = new OesFilter();
        mEffectFilter = new EffectFilter();
        mShowFilter = new DrawFilter();
        mWatermarkFilter = new WatermarkFilter(BitmapFactory.decodeResource(
                MyApplication.getAppResource(),
                R.mipmap.watermark));
        mWatermarkFilter.setPosition(0,0,0,0);
        mEffectFilter.addFilter(mWatermarkFilter);
        mCameraViewReference = new WeakReference<CameraView>(cameraView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // API >= 8
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (!extensions.contains("OES_EGL_image_external")) {
            throw new RuntimeException("This system does not support OES_EGL_image_external.");
        }

        int texture = createTextureID();
        mSurfaceTexture = new SurfaceTexture(texture);
        mBufferFilter.onCreated();
        mEffectFilter.onCreated();
        mShowFilter.onCreated();
        mBufferFilter.setTextureId(texture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if ((width == 0) || (height == 0)) {
            return;
        }
        mViewWidth = width;
        mViewHeight = height;
        updateViewport();
        updateFrameBuffer();

        mBufferFilter.onChanged(mTexWidth, mTexHeight);
        mEffectFilter.onChanged(mTexWidth, mTexHeight);
        mShowFilter.onChanged(mTexWidth, mTexHeight);

//        Gl2Utils.getShowMatrix2(
//                mMatrix,
//                mTexWidth, mTexHeight,
//                width, height);
//        if(CameraControler.mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT){
//            Gl2Utils.flip(mMatrix,true,false);
//            Gl2Utils.rotate(mMatrix,90);
//        } else {
//            Gl2Utils.rotate(mMatrix,270);
//        }
        Matrix.setIdentityM(mMatrix, 0);
        mShowFilter.setMatrix(mMatrix);
//        mBufferFilter.setMatrix(mMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if(mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTexMatrix);
        }
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0]);
        GLES20.glViewport(0,0, mTexWidth, mTexHeight);
        mBufferFilter.setCoordMatrix(mTexMatrix);
        mBufferFilter.onDraw();
        EasyGlUtils.unBindFrameBuffer();

        mEffectFilter.setTextureId(fTexture[0]);
        mEffectFilter.onDraw();
        mOutTexture = mEffectFilter.getOutputTexture();

//        mOutTexture = fTexture[0];

        GLES20.glViewport(0,0, mViewWidth, mViewHeight);
        mShowFilter.setTextureId(mOutTexture);
        mShowFilter.onDraw();

        synchronized (this) {
            if (mVideoEncoder != null) {
                mVideoEncoder.frameAvailableSoon(mTexMatrix, mMatrix);
            }
        }
    }

    private int createTextureID(){
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public final void updateViewport() {
        final CameraView cameraView = mCameraViewReference.get();
        if (cameraView != null) {
            int viewWidth = cameraView.getWidth();
            int viewHeight = cameraView.getHeight();
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            int videoWidth = cameraView.mVideoWidth;
            int videoHeight = cameraView.mVideoHeight;
            mTexWidth = cameraView.mVideoWidth;
            mTexHeight = cameraView.mVideoHeight;

            if (videoWidth == 0 || videoHeight == 0) {
                return;
            }
            if (MyApplication.DEBUG) {
                Log.i(TAG, String.format("view(%d,%d),video(%d,%d)",
                        viewWidth, viewHeight,
                        videoWidth, videoHeight));
            }
        }
    }

    private void updateFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
        GLES20.glGenFramebuffers(1, fFrame,0);
        GLES20.glGenTextures(1, fTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mTexWidth, mTexHeight,
                0,  GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void changeBitmap(Bitmap bitmap) {
        mWatermarkFilter.setBitmap(bitmap);
    }
}
