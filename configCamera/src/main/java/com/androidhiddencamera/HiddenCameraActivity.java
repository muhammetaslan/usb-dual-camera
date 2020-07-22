package com.androidhiddencamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.androidhiddencamera.config.CameraFacing;
import com.serenegiant.common.BaseActivity;

/**
 * Created by Keval on 27-Oct-16.
 * This abstract class provides ability to handle background camera to the activity in which it is
 * extended.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public abstract class HiddenCameraActivity extends BaseActivity implements CameraCallbacks {

    private CameraPreview mCameraPreview;
    private CameraConfig mCachedCameraConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add the camera preview surface to the root of the activity view.
        mCameraPreview = addPreView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //stop preview and release the camera.
        stopCamera();
    }

    /**
     * Start the hidden camera. Make sure that you check for the runtime permissions before you start
     * the camera.
     *
     * @param cameraConfig camera configuration {@link CameraConfig}
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    protected void startCamera(CameraConfig cameraConfig) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { //check if the camera permission is available

            onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE);
        } else if (cameraConfig.getFacing() == CameraFacing.REAR_FACING_CAMERA
                && !HiddenCameraUtils.isFrontCameraAvailable(this)) {   //Check if for the front camera

            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA);
        } else {
            mCachedCameraConfig = cameraConfig;
            mCameraPreview.startCameraInternal(cameraConfig);
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using {@link #startCamera(CameraConfig)} before using this function.
     */
    protected void takePicture() {
        Log.d("zxcv","takepic");

        if (mCameraPreview != null) {
            Log.d("zxcv","preview null değşil");

            if (mCameraPreview.isSafeToTakePictureInternal()) {
                Log.d("zxcv","safe control" +
                        "");

                mCameraPreview.takePictureInternal();
                Log.d("zxcv","girdi");
            }
        } else {
            throw new RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.");
        }
    }
    protected synchronized  void flashOff(){
        mCameraPreview.lightOff();
    }
    protected synchronized  void flashOn(){
        mCameraPreview.lightOn();
    }

    /**
     * Stop and release the camera forcefully.
     */
    protected void stopCamera() {
        mCachedCameraConfig = null;    //Remove config.
        if (mCameraPreview != null) mCameraPreview.stopPreviewAndFreeCamera();
    }

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return {@link CameraPreview} that was added to the view.
     */
    private CameraPreview addPreView() {
        //create fake camera view
        CameraPreview cameraSourceCameraPreview = new CameraPreview(this, this);
        cameraSourceCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = ((ViewGroup) getWindow().getDecorView().getRootView()).getChildAt(0);

        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 1);
            linearLayout.addView(cameraSourceCameraPreview, params);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) view;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            relativeLayout.addView(cameraSourceCameraPreview, params);
        } else if (view instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) view;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
            frameLayout.addView(cameraSourceCameraPreview, params);
        } else {
            throw new RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout");
        }

        return cameraSourceCameraPreview;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCachedCameraConfig != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            startCamera(mCachedCameraConfig);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraPreview != null) mCameraPreview.stopPreviewAndFreeCamera();
    }
}