package com.pgg.pggpluginproject

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity", classLoader.toString())
        val dexClassLoader = DexClassLoader("", "", "", classLoader)
        Log.e("MainActivity", externalCacheDir.toString())

        resources.getString(R.string.app_name)

    }

    fun clickTv(view: View) {
        val clazz = classLoader.loadClass("com.pgg.plugin.Test")
        val method = clazz.getMethod("printPlugin")
        method.invoke(clazz.newInstance())

        // 找到一个容易替换Intent的地方
//                startActivity(new Intent(MainActivity.this,ProxyActivity.class));
        val intent = Intent()
        intent.component = ComponentName(
            "com.pgg.plugin",
            "com.pgg.plugin.MainActivity"
        )
        startActivity(intent)

    }
}