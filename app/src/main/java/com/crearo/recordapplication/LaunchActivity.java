package com.crearo.recordapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by aa on 2018/5/28.
 */

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    public void clickBtn(View view) {
        Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
