package com.crearo.recordapplication.filters;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.crearo.recordapplication.controlers.CameraControler;

/**
 * Created by aa on 2018/4/28.
 */

public class DrawFilter extends BaseFilter {

    public DrawFilter() {
        super("shaders/base_vertex.sh", "shaders/base_fragment.sh");
        setTexOrientation();
    }

    private void setTexOrientation() {
        float[] coord = new float[] {
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f };
        mCoordBuffer.clear();
        mCoordBuffer.put(coord);
        mCoordBuffer.position(0);
    }

    @Override
    public void onSizeChanged(int viewWidth, int viewHeight) {
    }

    @Override
    public void bindTexture() {
        textureBindType = GLES20.GL_TEXTURE_2D;
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mTexHandle, textureType);
    }

    @Override
    public void setMatrixData() {
        GLES20.glUniformMatrix4fv(mMatrixHandle,1,false, mPositionMatrix,0);
    }
}
