package com.mz.cameraapi1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private SurfaceView surfaceView;
    private ImageView imageView;
    private Button take;
    private Button back;
    private Camera mCamera;
    private Bitmap mBitmap;
    private BitmapDrawable mBitmapDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surface_view);
        imageView = findViewById(R.id.image_view);
        take = findViewById(R.id.take);
        back = findViewById(R.id.back);

        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.INVISIBLE);
                surfaceView.setVisibility(View.VISIBLE);
                startCamera();
            }
        });
    }

    private final class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            startCamera();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setPictureSize(1920, 1080);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            stopCamera();
        }
    }

    private void startCamera() {
        try {
            if (null == mCamera) {
                mCamera = Camera.open(1);
                if (mCamera != null) {
                    SurfaceHolder mSurfaceHolder = surfaceView.getHolder();
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.setDisplayOrientation(90);
                    mCamera.startPreview();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动相机失败", Toast.LENGTH_SHORT).show();

            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            finish();
        }
    }

    private void stopCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void takePicture() {
        Toast.makeText(this, "开始拍照", Toast.LENGTH_SHORT).show();
        if (mCamera == null) {
            startCamera();
        } else {
            mCamera.takePicture(null, null, new MyPictureCallback());
        }
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            if (data != null) {
                if (data.length / 1024 > 350 && data.length / 1024 < 450) {
                    options.inSampleSize = 3;
                } else if (data.length / 1024 > 450) {
                    options.inSampleSize = 4;
                }
            }
            options.inPurgeable = true;
            options.inInputShareable = true;

            mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            Matrix m = new Matrix();
            m.postScale(1, -1);
            m.postScale(-1, 1);
            m.postRotate(-90);
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, true);

            mBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            imageView.setBackgroundDrawable(mBitmapDrawable);
            //imageView.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.INVISIBLE);

            //图片存到相册
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "myphoto", "description");

        }
    }

    //把图片保存到本地
    public static void saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}