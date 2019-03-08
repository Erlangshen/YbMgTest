package com.ybmgtest

import android.support.v7.app.AppCompatActivity
import com.umeng.analytics.MobclickAgent

abstract class BaseActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        MobclickAgent.onKillProcess(this)
//    }
}