package com.ybmgtest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class WaterCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Button cancelBtn, cameraBtn, sureBtn;
    private ImageView pictureIv;
    private LinearLayout pictureLinear;
    private SurfaceView mSv;
    private SurfaceHolder mSurfaceHolder;
    private android.hardware.Camera mCamera;
    private Bitmap bitmap;
    private android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
    private TextView loadingTv,wordTv,dateTv;
    private float times = 0f;
    private String date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.water_camera_layout);
        initView();
        initData();
    }

    private void initView() {
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        sureBtn = (Button) findViewById(R.id.sureBtn);
        pictureIv = (ImageView) findViewById(R.id.pictureIv);
        pictureLinear = (LinearLayout) findViewById(R.id.pictureLinear);
        mSv = (SurfaceView) findViewById(R.id.mySv);
        loadingTv = (TextView) findViewById(R.id.loadingTv);
        wordTv= (TextView) findViewById(R.id.wordTv);
        dateTv= (TextView) findViewById(R.id.dateTv);

        mSv.setFocusable(true);
        mSurfaceHolder = mSv.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);
        // 为了实现照片预览功能，需要将SurfaceHolder的类型设置为PUSH,这样画图缓存就由Camera类来管理，画图缓存是独立于Surface的
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCameraBtnClick();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCancelBtnClick();
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSureBtnClick();
            }
        });
    }
    private void initData(){
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 E ");
        date = myFmt.format(new Date().getTime());
        dateTv.setText(date);
    }

    private void gotoCancelBtnClick() {
        pictureLinear.setVisibility(View.INVISIBLE);
        cancelBtn.setVisibility(View.INVISIBLE);
        cameraBtn.setVisibility(View.VISIBLE);
        wordTv.setVisibility(View.VISIBLE);
        dateTv.setVisibility(View.VISIBLE);
        sureBtn.setVisibility(View.INVISIBLE);
        mSv.setVisibility(View.VISIBLE);
    }

    private void gotoCameraBtnClick() {
        loadingTv.setVisibility(View.VISIBLE);

        mCamera.takePicture(null, null, new android.hardware.Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {//data 将会返回图片的字节数组
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap != null) {
                    Matrix m = new Matrix();
                    m.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    bitmap = compressImage(bitmap);
                    loadingTv.setVisibility(View.GONE);
                    cameraBtn.setVisibility(View.INVISIBLE);
                    cancelBtn.setVisibility(View.VISIBLE);
                    sureBtn.setVisibility(View.VISIBLE);
                    wordTv.setVisibility(View.INVISIBLE);
                    dateTv.setVisibility(View.INVISIBLE);
                    bitmap = addWater(bitmap);
                    pictureLinear.setVisibility(View.VISIBLE);
                    mSv.setVisibility(View.INVISIBLE);
                    pictureIv.setImageBitmap(bitmap);
                } else {
                    releaseCamera();
                }
            }
        });
    }

    private Bitmap addWater(Bitmap mBitmap) {
        android.graphics.Bitmap.Config bitmapConfig =
                mBitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        //获取原始图片与水印图片的宽与高
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels;//1080
        float mBitmapWidthF = mBitmapWidth;
        times = mBitmapWidthF / screenWidth;

        Bitmap mNewBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, bitmapConfig);
        Canvas canvas = new Canvas(mNewBitmap);
        //向位图中开始画入MBitmap原始图片
        canvas.drawBitmap(mBitmap, 0, 0, null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setDither(true); //获取跟清晰的图像采样
        paint.setFilterBitmap(true);//过滤一些
        paint.setTextSize(sp2px(this, 22) * times);
        String text = "装逼水印";
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float textW = paint.measureText(text);
        float x = (mBitmapWidth / 2) - (textW / 2);
        float textH = -paint.ascent() + paint.descent();
        canvas.drawText(text, x, (mBitmapHeight * 3 / 4), paint);//mBitmapWidth=3024

        paint.setTextSize(sp2px(this, 20) * times);
        paint.getTextBounds(date, 0, date.length(), bounds);
        textW = paint.measureText(date);
        x = (mBitmapWidth / 2) - (textW / 2);
        canvas.drawText(date, x, (mBitmapHeight * 3 / 4) + textH, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        return mNewBitmap;
    }

    private void gotoSureBtnClick() {
        try {
            FileOutputStream outStream = null;
            String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "testPhoto";
            String fileName = filePath + File.separator + String.valueOf(System.currentTimeMillis()) + ".jpg";
            File file = new File(fileName);
            if (!file.exists()) file.getParentFile().mkdirs();
            outStream = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            if (outStream != null) outStream.close();
            // 最后通知图库更新
            WaterCameraActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
            Toast.makeText(this, "文件已保存至:" + fileName, Toast.LENGTH_LONG).show();
            gotoCancelBtnClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 98;
        while (baos.toByteArray().length / 1024 > 3072) { // 循环判断如果压缩后图片是否大于 3Mb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 2;// 每次都减少2
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public int setCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void startCamera() {
        try {
            mCamera = Camera.open(0);
            Camera.getCameraInfo(0, cameraInfo);
            Camera.Parameters parameters = mCamera.getParameters();
            // 设置图片格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            // 设置照片质量
            parameters.setJpegQuality(100);
            // 首先获取系统设备支持的所有颜色特效，如果设备不支持颜色特性将返回一个null， 如果有符合我们的则设置
            List<String> colorEffects = parameters.getSupportedColorEffects();
            Iterator<String> colorItor = colorEffects.iterator();
            while (colorItor.hasNext()) {
                String currColor = colorItor.next();
                if (currColor.equals(Camera.Parameters.EFFECT_SOLARIZE)) {
                    parameters.setColorEffect(Camera.Parameters.EFFECT_AQUA);
                    break;
                }
            }
            // 获取对焦模式
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // 设置自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            // 设置闪光灯自动开启
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                // 自动闪光
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            mCamera.setDisplayOrientation(setCameraDisplayOrientation());
            // 设置显示
            mCamera.setPreviewDisplay(mSurfaceHolder);

            List<Camera.Size> photoSizes = parameters.getSupportedPictureSizes();//获取系统可支持的图片尺寸
            int width = 0, height = 0;
            for (Camera.Size size : photoSizes) {
                if (size.width > width) width = size.width;
                if (size.height > height) height = size.height;
            }
            parameters.setPictureSize(width, height);
            // 设置完成需要再次调用setParameter方法才能生效
            mCamera.setParameters(parameters);
            // 开始预览
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * dip转pix
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
