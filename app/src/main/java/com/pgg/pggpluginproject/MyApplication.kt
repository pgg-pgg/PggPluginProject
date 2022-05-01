package com.pgg.pggpluginproject

import android.app.Application

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        LoadApkUtils.loadClass(this)
//        HookStartActivityHelper.hookStartActivity(applicationContext)
        HookUtils.hookAMS()
        HookUtils.hookHandler()
    }
}