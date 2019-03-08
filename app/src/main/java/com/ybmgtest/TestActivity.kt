package com.ybmgtest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import kotlinx.android.synthetic.main.test_layout.*
import java.io.File
import java.io.FileOutputStream

class TestActivity : BaseActivity() {
    val PHOTO_INTENT_CODE = 101
    var filePath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
        photoBtn.setOnClickListener {
            var photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var fileName = System.currentTimeMillis().toString() + ".jpg"
            filePath = Environment.getExternalStorageDirectory().path + File.separator + "testPhoto" + File.separator + fileName
            var file = File(filePath)
            if (!file.exists()) file.parentFile.mkdirs()
            var authorities = "com.ybmgtest"
            var uri = FileProvider.getUriForFile(this, authorities, file)
            photoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(photoIntent, PHOTO_INTENT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PHOTO_INTENT_CODE) {
            var rotatePhotoPath=amendRotatePhoto(filePath)
            var bm = BitmapFactory.decodeFile(rotatePhotoPath)
            photoIv.setImageBitmap(bm)
        }
    }

    /**
     * 处理旋转后的图片
     *
     * @param originpath    原图路径
     * @return 返回修复完毕后的图片路径
     */
    fun amendRotatePhoto(originpath: String): String {
        if (TextUtils.isEmpty(originpath)) return originpath
        var angle = readPictureDegree(originpath)
        var bm: Bitmap = BitmapFactory.decodeFile(originpath)
        if (angle != 0) bm = rotaingImageView(angle, bm)
        return savePhotoToSD(bm,originpath)
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    fun readPictureDegree(path: String): Int {
        var degree = 0
        var exifInterface = ExifInterface(path)
        var orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
        return degree
    }

    /**
     *  旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    fun rotaingImageView(angle: Int, bm: Bitmap): Bitmap {
        var returnBm: Bitmap? = null
        var matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        if (bm != returnBm) bm.recycle()
        return returnBm
    }

    /**
     * 保存Bitmap图片在SD卡中
     * 如果没有SD卡则存在手机中
     *
     * @param mbitmap       需要保存的Bitmap图片
     * @return 保存成功时返回图片的路径，失败时返回null
     */
    fun savePhotoToSD(bm: Bitmap, originpath: String): String {
        var outStream: FileOutputStream? = null
        var fileName = originpath
        if (bm == null) return originpath
        outStream = FileOutputStream(fileName)
        bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        if (outStream != null) outStream.close()
        if (bm != null) bm.recycle()
        return fileName
    }
}