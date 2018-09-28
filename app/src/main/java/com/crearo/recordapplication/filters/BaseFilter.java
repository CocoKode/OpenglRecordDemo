package com.crearo.recordapplication.filters;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.crearo.recordapplication.MyApplication;
import com.crearo.recordapplication.utils.Gl2Utils;
import com.crearo.recordapplication.utils.MatrixUtils;

import java.nio.FloatBuffer;

/**
 * Created by aa on 2018/4/27.
 */

public abstract class BaseFilter {

    private int mProgram;
    private int mPositionHandle;
    private int mCoordHandle;
    public int mTexHandle;
    public int mMatrixHandle;
    private int mCoordMatrixHandle;

    private float[] mPositionArray = {1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f};
//    public float[] mCoordArray = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};

//    private float[] mPositionArray = {-1.0f,  1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,  -1.0f};
//    private float[] mCoordArray = {0.0f, 0.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f, 1.0f};

//    private float[] mCoordArray = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private float[] mCoordArray = {1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};

    private FloatBuffer mPositionBuffer;
    public FloatBuffer mCoordBuffer;
    //
    public float[] mPositionMatrix = new float[16];
    private float[] mCoordMatrix = new float[16];
    public int textureType = 0;
    public int textureId = 0;
    public int textureBindType = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    public BaseFilter(String vertexPath, String fragmentPath) {
        if (vertexPath != null && fragmentPath != null) {
            mProgram = Gl2Utils.createGlProgramByRes(
                    MyApplication.getAppResource(),
                    vertexPath,
                    fragmentPath);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord");
            mTexHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMatrix");
            mCoordMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uCoordMatrix");

            mPositionBuffer = Gl2Utils.array2Buffer(mPositionArray);
            mCoordBuffer = Gl2Utils.array2Buffer(mCoordArray);

            Matrix.setIdentityM(mPositionMatrix, 0);
            Matrix.setIdentityM(mCoordMatrix, 0);
        }
    }

    public void onCreated() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void onChanged(int viewWidth, int viewHeight) {
        onSizeChanged(viewWidth, viewHeight);
    }

    public void onDraw() {
        clearColor();
        useProgram();
        setMatrixData();
        bindTexture();
        draw();
    }

    public void onDraw(Bitmap bitmap) {
        useProgram();
        setMatrixData();
        bindImgTexture(bitmap);
        draw();
    }

    public void clearColor() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }
    public void useProgram() {
        GLES20.glUseProgram(mProgram);
    }
    public void setMatrixData() {
        GLES20.glUniformMatrix4fv(mMatrixHandle,1,false, mPositionMatrix,0);
        GLES20.glUniformMatrix4fv(mCoordMatrixHandle, 1, false, mCoordMatrix,0);
    }
    public void bindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(textureBindType, textureId);
        GLES20.glUniform1i(mTexHandle, textureType);
    }
    public void bindImgTexture(Bitmap bitmap) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glUniform1i(mTexHandle, textureType);
    }
    public void draw() {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,2, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(mCoordHandle);
        GLES20.glVertexAttribPointer(mCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mCoordBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mCoordHandle);
        GLES20.glBindTexture(textureBindType, 0);
        GLES20.glUseProgram(0);
    }

    public void setMatrix(float[] matrix) {
        if ((matrix != null) && (matrix.length > 16)) {
            System.arraycopy(matrix, 16, mPositionMatrix, 0, 16);
        } else {
            mPositionMatrix = matrix;
        }
    }

    public void setCoordMatrix(float[] matrix) {
        mCoordMatrix = matrix;
    }

    public float[] getMatrix() {
        return mPositionMatrix;
    }

    public void setTextureId(int texture) {
        this.textureId = texture;
    }

    public int getTextureId() {
        return textureId;
    }

    public void release() {
        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

    public abstract void onSizeChanged(int viewWidth, int viewHeight);
}
