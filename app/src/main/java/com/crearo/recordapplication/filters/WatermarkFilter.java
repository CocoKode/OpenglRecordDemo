package com.crearo.recordapplication.filters;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.crearo.recordapplication.MainActivity;
import com.crearo.recordapplication.utils.Gl2Utils;

/**
 * Created by aa on 2018/4/27.
 */

public class WatermarkFilter extends DrawFilter{

    private int x, y, w, h;
    private int mWidth, mHeight;
    private Bitmap mBitmap;
    private DrawFilter mFilter;

    private int[] textures = new int[1];

    public WatermarkFilter(Bitmap bitmap) {
        super();
        mBitmap = bitmap;
        mFilter = new DrawFilter();
        createTexture();
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setPosition(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }

    private void createTexture() {
        if(mBitmap != null){
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            Gl2Utils.flip(mFilter.getMatrix(), false, true);
            mFilter.setTextureId(textures[0]);
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        mFilter.onChanged(width,height);
    }

    @Override
    public void onDraw() {
        super.onDraw();
        int reSize[] = reSize(mBitmap);
        GLES20.glViewport(x, y,
                w == 0 ? reSize[0] : w,
                h == 0 ? reSize[1] : h);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_DST_ALPHA);
        mFilter.onDraw(mBitmap);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glViewport(0, 0, mWidth, mHeight);
    }

    public int[] reSize(Bitmap bitmap) {
        int density = bitmap.getDensity();
        float rate = (float) (480f / density);
        float widthRate = (float) (MainActivity.PREVIEW_WIDTH / 1280f);
        float heightRate = (float) (MainActivity.PREVIEW_HEIGHT / 720f);
        int[] size = new int[2];
        size[0] = (int) (bitmap.getWidth() * rate * widthRate);
        size[1] = (int) (bitmap.getHeight() * rate * heightRate);
        return size;
    }
}
