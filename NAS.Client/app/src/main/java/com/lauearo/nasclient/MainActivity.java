package com.lauearo.nasclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Service.UploadService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mUploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUploadBtn = findViewById(R.id.uploadBtn);
        mUploadBtn.setOnClickListener((View v) -> {

            Intent chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooseIntent.setType("*/*");

            startActivityForResult(Intent.createChooser(chooseIntent, "Select File"),
                    Constants.ACTION_REQUEST_PICK_FILE);

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == Constants.ACTION_REQUEST_PICK_FILE) {
            final Uri fileUri = data.getData();
            assert fileUri != null;

            try {
                UploadService.getInstance().beginUpload(this.getBaseContext(), fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
