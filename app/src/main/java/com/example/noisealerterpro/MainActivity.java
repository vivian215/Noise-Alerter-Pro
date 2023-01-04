package com.example.noisealerterpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private MainView mainView;
    private final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    private final int PERMISSION_REQUEST_CODE = 168;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("main", "onCreate");
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // create mainview
        mainView = new MainView(this, getResources(),  displayMetrics.heightPixels, displayMetrics.widthPixels);
        setContentView(mainView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        boolean success = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case PERMISSION_REQUEST_CODE:
                Log.d("main", "onRequestPermissionsResult" + grantResults[0]);
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("main", "onRequestPermissionsResult: " + grantResults[0]);
                    success = true;
                }
                break;
        }

        if (!success) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        Log.d("stuck", "onstart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d("stuck", "onpause");
        super.onPause();
        mainView.pause();
    }

    @Override
    protected void onStop() {
        Log.d("stuck", "onstop");
        super.onStop();
        mainView.pause();
    }

    @Override
    protected void onResume() {
        Log.d("stuck", "onresume");
        super.onResume();
        mainView.resume();
    }

    @Override
    protected void onDestroy() {
        Log.d("stuck", "ondestroy");
        super.onDestroy();
    }
}