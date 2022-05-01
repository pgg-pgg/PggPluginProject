package com.pgg.plugin

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.activity_main, null)
        setContentView(view)

        val plugin = context.getString(R.string.plugin)

        Log.e("Plugin", "插件的MainActivity被启动了")
        Log.e("Plugin", plugin)
    }
}