package com.pgg.plugin

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {
    protected lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loadResource = LoadUtils.loadResource(applicationContext)
        context = ContextThemeWrapper(baseContext, 0)
        val clazz: Class<out Context?> = context.javaClass
        try {
            val mResourcesField = clazz.getDeclaredField("mResources")
            mResourcesField.isAccessible = true
            mResourcesField[context] = loadResource
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}