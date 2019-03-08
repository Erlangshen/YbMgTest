package com.ybmgtest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.yanzhenjie.permission.AndPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val c = MainActivity::class.java.classLoader
        val c1 = c.parent
        val c2 = c1.parent
        Log.e("abc", "c:$c\nc1:$c1\nc2:$c2")
        AndPermission.with(this)
                .permission(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .send()
        scanBtn.setOnClickListener {
            var integrator = IntentIntegrator(this)
            // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
            integrator.captureActivity = ScanActivity::class.java//设置打开摄像头的Activity
            integrator.setPrompt("请扫描ISBN")//底部的提示文字，设为""可以置空
            integrator.setCameraId(0) //前置或者后置摄像头
            integrator.setBeepEnabled(true)//扫描成功的「哔哔」声，默认开启
            integrator.setBarcodeImageEnabled(true)
            integrator.initiateScan()
        }
        takePhotoBtn.setOnClickListener {
            var tpIntent = Intent()
            tpIntent.setClass(this, TestActivity::class.java)
            startActivity(tpIntent)
        }
        waterCameraBtn.setOnClickListener {
            var wcIntent=Intent(this, WaterCameraActivity::class.java)
            startActivity(wcIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var scanResult: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        scanTv.text = scanResult.contents
    }
}
