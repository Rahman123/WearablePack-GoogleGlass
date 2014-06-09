package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.R;
import com.salesforce.glassdemo.util.CameraUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraActivity extends Activity {
    public static final String EXTRA_BASE64_IMAGE = "base64Image";

    protected Camera mCamera;
    protected SurfaceView mSurfaceView;

    final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(Constants.TAG, "Surface changed.");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Log.i(Constants.TAG, "Surface created.");
                Camera.Parameters parameters = mCamera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }

                Camera.Size pictureSize = CameraUtils.getLargestPictureSize(parameters);
                Log.d(Constants.TAG, "Calc max size: " + pictureSize.width + " , height: " + pictureSize.height);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);

                Camera.Size previewSize = CameraUtils.getSmallestPictureSize(parameters);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureFormat(ImageFormat.JPEG);

                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(Constants.TAG, "Surface destroyed.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        if (holder != null) {
            holder.addCallback(surfaceHolderCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCamera.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            Log.d(Constants.TAG, "Camera key captured");
            takePicture();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void takePicture() {
        Log.i(Constants.TAG, "Taking photo...");

        Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                Log.d(Constants.TAG, "Picture data received");
                new Runnable() {
                    @Override
                    public void run() {
                        Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
                        source = CameraUtils.resizeImageToHalf(source);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        source.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                        byte[] byteArray = stream.toByteArray();

                        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_BASE64_IMAGE, base64Image);
                        setResult(RESULT_OK, intent);
                    }
                }.run();
            }
        };
        mCamera.takePicture(null, null, pictureCallback);
    }
}
