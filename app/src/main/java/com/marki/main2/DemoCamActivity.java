/*
 * Copyright 2016 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevalpatel2106.sample.main2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.kevalpatel2106.sample.R;

import java.io.File;
import java.io.FileOutputStream;

import java.util.concurrent.TimeUnit;

public class DemoCamActivity extends HiddenCameraActivity  {
    private static final int REQ_CODE_CAMERA_PERMISSION = 1253;

    private CameraConfig mCameraConfig;
    private Sensor lightSensor;
    private SensorManager sensorManager;
    private SensorListener sensorListener;
    Object obj;
boolean locked,resume;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locked=true;
        resume=true;
        obj=new Object();
        setContentView(R.layout.activity_hidden_cam);
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
         sensorListener= new SensorListener();
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(sensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270)
                .setCameraFocus(CameraFocus.AUTO)
                .build();


        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            //Start camera preview
            startCamera(mCameraConfig);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQ_CODE_CAMERA_PERMISSION);
        }

        //Take a picture
        findViewById(R.id.capture_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take picture using the camera without preview.


            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("resume","glel");

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE_CAMERA_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(mCameraConfig);
            } else {
                Toast.makeText(this, R.string.error_camera_permission_denied, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {

        // Convert file to bitmap.
        // Do something.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //Display the image to the image view
        ((ImageView) findViewById(R.id.cam_prev)).setImageBitmap(bitmap);
    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void saveToDevice(Bitmap bitmap) {

        String filename = System.currentTimeMillis() + "fridgeCam.png";
        File sd = Environment.getExternalStorageDirectory();
        File dest = new File(sd + "aaUsbCamera", filename);
        Toast.makeText(this, sd + "aaUsbCamera", Toast.LENGTH_LONG).show();
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
public class  SensorListener implements SensorEventListener {
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float valueZ = event.values[0];
            Log.d("aaa",valueZ+"");

            if (valueZ ==0 && locked) {


                flashOn();

               takePicture();




               flashOff();


            } else {
                flashOff();


            }

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


}
}
