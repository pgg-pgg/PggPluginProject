package com.pgg.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources

object LoadUtils {
    const val apkName = "/plugin-debug.apk"

    //context.externalCacheDir?.absolutePath + apkName

    @SuppressLint("DiscouragedPrivateApi")
    fun loadResource(context: Context): Resources {
        try {
            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPathMethod =
                AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            addAssetPathMethod.isAccessible = true
            addAssetPathMethod.invoke(assetManager, context.externalCacheDir?.absolutePath + apkName)
            // 如果传入的是Activity的 context 会不断循环，导致崩溃
            val resources = context.resources
            // 加载插件的资源的 resources

            // 加载插件的资源的 resources
            return Resources(assetManager, resources.displayMetrics, resources.configuration)
        }catch (e: Exception) {
            e.printStackTrace()
        }
        throw ExceptionInInitializerError("Resources 创建失败")
    }
}