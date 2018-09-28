package com.crearo.recordapplication;

import android.media.MediaCodec;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by aa on 2018/5/31.
 */

public class Frame implements Serializable {

    public int trackIndex;
    public ByteBuffer encodeData;
    public MediaCodec.BufferInfo bufferInfo;

    public Frame(int trackIndex, ByteBuffer encodeData, MediaCodec.BufferInfo bufferInfo) {
        this.trackIndex = trackIndex;

        this.encodeData = ByteBuffer.allocateDirect(encodeData.limit())
                .order(ByteOrder.LITTLE_ENDIAN);
        this.encodeData.limit(encodeData.limit());
//        this.encodeData.reset();
        this.encodeData.put(encodeData);
//        this.encodeData = encodeData;

        this.bufferInfo = new MediaCodec.BufferInfo();
        this.bufferInfo.flags = bufferInfo.flags;
        this.bufferInfo.size = bufferInfo.size;
        this.bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs;
    }
}
