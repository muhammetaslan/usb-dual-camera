package com.serenegiant.usbCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usbcameratest7.R;

/*
* this class get the main_activity and start the camera action
* */
public final class MainActivity extends BaseActivity  {

    static final String TAG = "MainActivity";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1253);
        }
        Toast.makeText(MainActivity.this, "Main of Application", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "OnCreate");
        startActivity(new Intent(MainActivity.this, Cameras.class));
        Log.e(TAG, "OnCreate 1");
        findViewById(R.id.baslat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take picture using the camera without preview.
            }
        });
    }
}