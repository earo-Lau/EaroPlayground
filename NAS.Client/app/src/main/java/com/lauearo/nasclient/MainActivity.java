package com.lauearo.nasclient;

import NAS.Model.UploadModelOuterClass;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mUploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUploadBtn = findViewById(R.id.uploadBtn);
        mUploadBtn.setOnClickListener((View v) ->{

        });
    }
}
