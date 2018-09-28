package com.crearo.recordapplication.filters;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by aa on 2018/4/27.
 */

public class EffectFilter extends BaseFilter {

    private Queue<BaseFilter> mFilterQueue;
    private List<BaseFilter> mFilterList;
    private int textureIndex = 0;
    private int mWidth, mHeight;
    private int mFilterNum = 0;
    private int fTextureSize = 2;
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[fTextureSize];

    public EffectFilter() {
        super(null, null);
        mFilterList = new ArrayList<>();
        mFilterQueue = new ConcurrentLinkedQueue<>();
    }

    public void addFilter(BaseFilter filter) {
        mFilterQueue.add(filter);
    }

    @Override
    public void onSizeChanged(int viewWidth, int viewHeight) {
        mWidth = viewWidth;
        mHeight = viewHeight;
        updateFilter();
        createFrameBuffer();
    }

    @Override
    public void onDraw(){
        updateFilter();
        textureIndex = 0;
        GLES20.glViewport(0,0, mWidth, mHeight);

        for (BaseFilter filter : mFilterList) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[textureIndex % 2], 0);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fRender[0]);
            if(textureIndex == 0) {
                filter.setTextureId(getTextureId());
            } else {
                filter.setTextureId(fTexture[(textureIndex - 1) % 2]);
            }
            filter.onDraw();
            unBindFrame();
            textureIndex++;
        }
    }

    private void updateFilter(){
        BaseFilter filter;
        while ((filter = mFilterQueue.poll()) != null) {
//            filter.create();
            filter.onChanged(mWidth, mHeight);
            mFilterList.add(filter);
            mFilterNum++;
        }
    }

    //创建FrameBuffer
    private boolean createFrameBuffer() {
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glGenRenderbuffers(1, fRender, 0);

        genTextures();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mWidth, mHeight);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fTexture[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]);
//        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
//        if(status==GLES20.GL_FRAMEBUFFER_COMPLETE){
//            return true;
//        }
        unBindFrame();
        return false;
    }

    private void genTextures() {
        GLES20.glGenTextures(fTextureSize, fTexture, 0);
        for (int i = 0; i < fTextureSize; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        }
    }

    private void unBindFrame() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getOutputTexture(){
        return mFilterNum == 0 ? getTextureId() : fTexture[(textureIndex - 1) % 2];
    }
}
