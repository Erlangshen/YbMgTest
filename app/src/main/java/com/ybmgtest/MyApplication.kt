package com.ybmgtest

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.support.annotation.RequiresApi
import com.umeng.commonsdk.UMConfigure

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        UMConfigure.init(this,UMConfigure.DEVICE_TYPE_PHONE,"")
    }
}