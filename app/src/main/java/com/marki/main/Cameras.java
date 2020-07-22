/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.kevalpatel2106.sample.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandlerMultiSurface;
import com.serenegiant.usbcameratest7.R;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class Cameras extends HiddenCameraActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;    // FIXME set false when production
    private static final String TAG = "Cameras";
    private final Object mSync = new Object();
    private static final float[] BANDWIDTH_FACTORS = {0.6f, 0.6f};



    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;


    private static final int REQ_CODE_CAMERA_PERMISSION = 1253;


    private CameraConfig mCameraConfig;

    private UVCCameraHandlerMultiSurface mCameraHandler;
    private UVCCameraHandler mHandlerR;
    private CameraViewInterface mUVCCameraViewR;
    private ImageButton mCaptureButtonR;
    private Surface mRightPreviewSurface;

    private UVCCameraHandler mHandlerL;
    private CameraViewInterface mUVCCameraViewL;
    private ImageButton mCaptureButtonL;
    private Surface mLeftPreviewSurface;
    private String CAPTURE_PATH;
    private String DIR_NAME = "aaUsbCamera";

    //Button take2Photo;
    Thread threadL, threadDevice,threadR;
    private Sensor lightSensor;
    private SensorManager sensorManager;
    private SensorListener sensorListener;
    Button buttonUplo;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameras);

//device camera
/****************************************************/
        CAPTURE_PATH = Environment.getExternalStorageDirectory() + DIR_NAME;

        findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUVCCameraViewL = (CameraViewInterface) findViewById(R.id.camera_view_L);
        mUVCCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewL).setOnClickListener(mOnClickListener);
        mCaptureButtonL = (ImageButton) findViewById(R.id.capture_button_L);
        mCaptureButtonL.setOnClickListener(mOnClickListener);
        mCaptureButtonL.setVisibility(View.INVISIBLE);

        mUVCCameraViewR = (CameraViewInterface) findViewById(R.id.camera_view_R);
        mUVCCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewR).setOnClickListener(mOnClickListener);
        mCaptureButtonR = (ImageButton) findViewById(R.id.capture_button_R);
        mCaptureButtonR.setOnClickListener(mOnClickListener);
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        //mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270)
                .setCameraFocus(CameraFocus.AUTO)
                .build("/storage/emulated/0/DCIM/USBCameraTest/");

//Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            //Start camera preview
            startCamera(mCameraConfig);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQ_CODE_CAMERA_PERMISSION);
        }

        //take2Photo = findViewById(R.id.takeTwoPhoto);
        //take2Photo.setOnClickListener(mOnClickListener);
        synchronized (mSync) {
            mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

            mHandlerR = UVCCameraHandler.createHandler(this, mUVCCameraViewR, 0, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG, BANDWIDTH_FACTORS[1]);
            mHandlerL = UVCCameraHandler.createHandler(this, mUVCCameraViewL, 0, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG, BANDWIDTH_FACTORS[0]);
        }

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensorListener = new SensorListener();
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

         threadL = new Thread(new Runnable()  {

            @Override
            public void run() {

                mHandlerL.captureStill();
            }
        });

         threadR = new Thread(new Runnable()  {

            @Override
            public void run() {

                mHandlerR.captureStill();

            }
        });
         threadDevice = new Thread(new Runnable()  {

            @Override
            public void run() {

                takePicture();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onResume();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerR.close();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onPause();
        mHandlerL.close();
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onPause();
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerR != null) {
            mHandlerR = null;
        }
        if (mHandlerL != null) {
            mHandlerL = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewR = null;
        mCaptureButtonR = null;
        mUVCCameraViewL = null;
        mCaptureButtonL = null;
        super.onDestroy();
        sensorManager.unregisterListener(sensorListener);

    }

    public final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view_L:
                    if (mHandlerL != null) {
                        if (!mHandlerL.isOpened()) {
                            CameraDialog.showDialog(Cameras.this);
                        } else {
                            mHandlerL.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_L:
                    if (mHandlerL != null) {
                        if (mHandlerL.isOpened()) {
                            //TODO when app open getRequest will run

                            if (checkPermissionWriteExternalStorage()) {
                                mHandlerL.captureStill();
                            }
                            Toast.makeText(Cameras.this, "oldu", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.camera_view_R:
                    if (mHandlerR != null) {
                        if (!mHandlerR.isOpened()) {
                            CameraDialog.showDialog(Cameras.this);
                        } else {
                            mHandlerR.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_R:
                    if (mHandlerR != null) {
                        if (mHandlerR.isOpened()) {
                            if (checkPermissionWriteExternalStorage()) {
                                mHandlerR.captureStill();

                            }
                        }
                    }
                    break;

//                case R.id.takeTwoPhoto:
//
//                    takeTwoL();
//                    try {
//                        Thread.sleep(1000);
//                        takeTwoR();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    Toast.makeText(Cameras.this, "????oldu", Toast.LENGTH_SHORT).show();
//
//                    /*if (checkPermissionWriteExternalStorage()) {
//
//                        if (mHandlerR != null) {
//                            if (mHandlerR.isOpened()) {
//                                mHandlerR.captureStill();
//
//                            }
//                        }
//
//                        try {
//                            Thread.sleep(1100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//
//                        if (mHandlerL != null) {
//                            if (mHandlerL.isOpened()) {
//
//                                mHandlerL.captureStill();
//
//                            }
//                        }
//                    }*/
//                    Toast.makeText(Cameras.this, "take two photo", Toast.LENGTH_SHORT).show();
//
//                    break;
            }
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:" + device);
            Toast.makeText(Cameras.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:" + device);
            if (!mHandlerL.isOpened()) {
                mHandlerL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
                mHandlerL.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //mCaptureButtonL.setVisibility(View.VISIBLE);
                    }
                });
            } else if (!mHandlerR.isOpened()) {
                mHandlerR.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
                mHandlerR.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mCaptureButtonR.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);
            if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerL.close();
                        if (mLeftPreviewSurface != null) {
                            mLeftPreviewSurface.release();
                            mLeftPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerR != null) && !mHandlerR.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerR.close();
                        if (mRightPreviewSurface != null) {
                            mRightPreviewSurface.release();
                            mRightPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(Cameras.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
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
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraButton();
                }
            }, 0);
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mHandlerL != null) && !mHandlerL.isOpened() && (mCaptureButtonL != null)) {
                    mCaptureButtonL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerR != null) && !mHandlerR.isOpened() && (mCaptureButtonR != null)) {
                    mCaptureButtonR.setVisibility(View.INVISIBLE);
                }
            }
        }, 0);
    }

    private void takeTwoL() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mSync) {
                    if (checkPermissionWriteExternalStorage()) {
                        if (mHandlerL != null) {
                            if (mHandlerL.isOpened()) {
                                mHandlerL.captureStill();
                            }
                        }
                    }
                }
            }
        }, 0);
    }

    private void takeTwoR() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mSync) {
                    if (checkPermissionWriteExternalStorage()) {
                        if (mHandlerR != null) {
                            if (mHandlerR.isOpened()) {
                                mHandlerR.captureStill();

                            }
                        }

                    }
                }
            }
        }, 0);
    }








    @Override
    public void onImageCapture(@NonNull File imageFile) {
        // Convert file to bitmap.
        // Do something.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        Log.d("resimm",imageFile.getPath());
        //The captured photo is upload to the AWS S3.
        beginUpload(imageFile.getPath());
        //Display the image to the image view
       // ((ImageView) findViewById(R.id.cam_prev)).setImageBitmap(bitmap);
    }

    @Override
    public void onCameraError(int errorCode) {
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

    class SensorListener implements SensorEventListener {
        long second, lastSecond = 0;


        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float valueZ = event.values[0];
                second = System.currentTimeMillis() / 1000;
                Log.d("aaa", valueZ + "saat"+second);
                if (valueZ == 0 && 20 < ( second- lastSecond)) {

                    flashOn();
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    threadL.run();threadR.run();
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }




                    threadDevice.run();


                    new Handler().postDelayed (() -> {
                        flashOff();
                    }, 3000);
                    lastSecond = second;
                }
            }


        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }


    }
}
