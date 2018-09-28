package com.crearo.recordapplication.filters;

import android.hardware.Camera;

import com.crearo.recordapplication.controlers.CameraControler;
import com.crearo.recordapplication.utils.Gl2Utils;

/**
 * Created by aa on 2018/4/27.
 */

public class OesFilter extends BaseFilter {
    public OesFilter() {
        super("shaders/oes_vertex.sh", "shaders/oes_fragment.sh");
    }

    @Override
    public void onSizeChanged(int viewWidth, int viewHeight) {

    }
}
