package com.crearo.recordapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crearo.recordapplication.records.MediaAudioEncoder;
import com.crearo.recordapplication.records.MediaEncoder;
import com.crearo.recordapplication.records.MediaMuxerWrapper;
import com.crearo.recordapplication.records.MediaVideoEncoder;
import com.crearo.recordapplication.utils.WindowUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FrameLayout mFramelayout;
    private CameraView mCameraView;
    private TextView mTimeText;
    private Button mRecordBtn;
    private MediaMuxerWrapper mMuxer;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    private boolean isRecordAudio = false;
    private boolean isRecordVideo = true;
    private MediaVideoEncoder mVideoEncoder = null;
    private Handler mHandler = new Handler();

    public static final int PREVIEW_WIDTH = 1920;
    public static final int PREVIEW_HEIGHT = 1080;

    private int mState;
    private static final int STATE_READY = 0;
    private static final int STATE_PRERECORDING = 1;
    private static final int STATE_RECORDING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
//
//        mCameraView = findViewById(R.id.view_camera);
//        mTimeText = findViewById(R.id.tv_time);
//        mRecordBtn = findViewById(R.id.btn_record);
//        mRecordBtn.setOnClickListener(this);
//        mCameraView.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
//        mTimeText.setVisibility(View.INVISIBLE);

        View view = WindowUtils.createWindow(this, R.layout.activity_window);
        mFramelayout = view.findViewById(R.id.framelayout);
        mCameraView = view.findViewById(R.id.view_camera);
        mTimeText = view.findViewById(R.id.tv_time);
        mRecordBtn = view.findViewById(R.id.btn_record);
        mRecordBtn.setOnClickListener(this);
        mCameraView.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        mTimeText.setVisibility(View.INVISIBLE);

        mState = STATE_READY;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTiming();
        WindowUtils.showWindow(mFramelayout);

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (mMuxer == null && mState == STATE_READY) {
//                    Toast.makeText(MainActivity.this, "开始预录", Toast.LENGTH_SHORT).show();
//                    startRecording();
//                    mState = STATE_PRERECORDING;
//                }
//            }
//        }, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WindowUtils.hideWindow(mFramelayout);
    }


    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
//                if (mMuxer == null) {
                if (mState == STATE_READY) {
                    Toast.makeText(MainActivity.this, "开始录像", Toast.LENGTH_SHORT).show();
                    startRecording();
                    mVideoEncoder.getRecordHandler().obtainMessage(0).sendToTarget();
                    mState = STATE_RECORDING;
                    mRecordBtn.setText("停止录像");
                } else {
                    Toast.makeText(MainActivity.this, "录像结束", Toast.LENGTH_SHORT).show();
                    stopRecording();
                    mState = STATE_READY;
                }
                break;
            default:
                break;
        }
    }

    private void startRecording() {
        try {
            mRecordBtn.setText("停止");
            // if you record audio only, ".m4a" is also OK.
            mMuxer = new MediaMuxerWrapper(".mp4");
            if (isRecordVideo) {
                mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraView.getVideoWidth(), mCameraView.getVideoHeight());
            }
            if (isRecordAudio) {
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            mRecordBtn.setText("录像");
            Log.e("MainActivity", "startRecording:", e);
        }
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                mCameraView.setVideoEncoder((MediaVideoEncoder)encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                mCameraView.setVideoEncoder(null);
        }
    };

    private void stopRecording() {
        mRecordBtn.setText("录像");
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }
    }

    private void startTiming() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Date currentData = new Date(System.currentTimeMillis());
                        mTimeText.setText(timeFormat.format(currentData));

                        mTimeText.setDrawingCacheEnabled(true);
                        mTimeText.measure(
                                View.MeasureSpec.makeMeasureSpec(
                                        0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(
                                        0, View.MeasureSpec.UNSPECIFIED));
                        mTimeText.layout(0, 0,
                                mTimeText.getMeasuredWidth(), mTimeText.getMeasuredHeight());
                        Bitmap bitmap = Bitmap.createBitmap(mTimeText.getDrawingCache());
                        mTimeText.destroyDrawingCache();
                        mCameraView.changeBitmap(bitmap);
                    }
                });
            }
        }, 0, 1000);

    }
}
