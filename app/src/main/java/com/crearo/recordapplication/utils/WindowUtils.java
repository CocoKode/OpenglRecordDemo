package com.crearo.recordapplication.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.crearo.recordapplication.CameraView;

/**
 * Created by aa on 2018/5/28.
 */

public class WindowUtils {

    private Context mContext;
    private static View mView;
    private static WindowManager mWindowManager;

    public static View createWindow(Context context, int viewID) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mView = LayoutInflater.from(context).inflate(viewID, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.format = PixelFormat.TRANSLUCENT;
        mWindowManager.addView(mView, params);
        return mView;
    }

    public static void removeWindow() {
        if (mWindowManager != null && mView != null) {
            mWindowManager.removeView(mView);
        }
    }

    public static void hideWindow(View v) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mView.getLayoutParams();
        params.width = 1;
        params.height = 1;
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mWindowManager.updateViewLayout(mView, params);
    }

    public static void showWindow(View v) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mView.getLayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mWindowManager.updateViewLayout(mView, params);
    }
}
