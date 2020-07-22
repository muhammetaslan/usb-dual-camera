
package com.kevalpatel2106.sample.main;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usbcameratest7.R;

import java.io.File;

public final class MainActivity extends BaseActivity  {

    private String DIR_NAME = "aaUsbCamera";
    private Button button;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.baslat);
        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            //Start camera preview

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1253);
        }
        checkPermissionWriteExternalStorage();
        Toast.makeText(MainActivity.this, "Main", Toast.LENGTH_SHORT).show();
        createDir();
        startActivity(new Intent(MainActivity.this, Cameras.class));
        findViewById(R.id.baslat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take picture using the camera without preview.


            }
        });
    }



    public void createDir(){

        File f = new File(Environment.getExternalStorageDirectory(), DIR_NAME);
        if (!f.exists()) {
            f.mkdirs();
        }
    }
}