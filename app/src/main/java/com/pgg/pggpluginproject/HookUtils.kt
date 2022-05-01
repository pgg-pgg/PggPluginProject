package com.pgg.pggpluginproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Handler
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object HookUtils {
    const val TARGET_INTENT = "target_intent"

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun hookAMS() {
        try {
            //获取 singleton 对象
            //23 26不同，需要差异化处理
            val singletonField = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //如果小于26的版本
                val clazz = Class.forName("android.app.ActivityManagerNative")
                clazz.getDeclaredField("gDefault")
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                //小于29的版本
                val clazz = Class.forName("android.app.ActivityManager")
                clazz.getDeclaredField("IActivityManagerSingleton")
            } else {
                //大于等于29的版本
                val clazz = Class.forName("android.app.ActivityTaskManager")
                clazz.getDeclaredField("IActivityTaskManagerSingleton")
            }
            singletonField.isAccessible = true
            val singleton = singletonField.get(null)
            // 获取 系统的 IActivityManager 对象
            val singletonClass = Class.forName("android.util.Singleton")
            val mInstanceField = singletonClass.getDeclaredField("mInstance")
            mInstanceField.isAccessible = true
            val getMethod = singletonClass.getDeclaredMethod("get")
            getMethod.isAccessible = true
            val mInstance = getMethod.invoke(singleton)
            //android.app.ActivityTaskManager
            val iActivityManagerClass = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Class.forName("android.app.IActivityManager")
            } else {
                Class.forName("android.app.IActivityTaskManager")
            }
            val proxyInstance = Proxy.newProxyInstance(Thread.currentThread().contextClassLoader,
                arrayOf<Class<*>>(iActivityManagerClass),
                InvocationHandler { proxy, method, args ->
                    // Intent的修改 -- 过滤
                    /**
                     * IActivityManager类的方法
                     * startActivity(whoThread, who.getBasePackageName(), intent,
                     *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                     *                         token, target != null ? target.mEmbeddedID : null,
                     *                         requestCode, 0, null, options)
                     */
                    // 过滤 只Hook startActivity方法
                    try {
                        if ("startActivity" == method.name) {
                            //找到intent参数
                            var intentIndex = 0
                            args.forEachIndexed { index, any ->
                                if (any is Intent) {
                                    intentIndex = index
                                    return@forEachIndexed
                                }
                            }
                            val intent = args[intentIndex] as Intent
                            val proxyIntent = Intent()
                            //修改intent为启动代理的activity
                            proxyIntent.setClassName(
                                "com.pgg.pggpluginproject",
                                "com.pgg.pggpluginproject.ProxyActivity"
                            )
                            //保存原有的插件的intent信息，用于后面的恢复
                            proxyIntent.putExtra(TARGET_INTENT, intent)
                            args[intentIndex] = proxyIntent
                        }

                        //最后还需要正常调用IActivityManager.startActivity
                        method.invoke(mInstance, *args)
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                })

            // ActivityManager.getService() 替换成 proxyInstance
            mInstanceField.set(singleton, proxyInstance)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun hookHandler() {
        try {
            //获取ActivityThread对象
            val activityThreadClazz = Class.forName("android.app.ActivityThread")
            val activityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread")
            activityThreadField.isAccessible = true
            val activityThread = activityThreadField[null]

            val mHField = activityThreadClazz.getDeclaredField("mH")
            mHField.isAccessible = true
            val mH = mHField.get(activityThread) as Handler
            val mCallbackField = Handler::class.java.getDeclaredField("mCallback")
            mCallbackField.isAccessible = true

            val callback = Handler.Callback {
                try {
                    when (it.what) {
                        100 -> {
                            // LAUNCH_ACTIVITY msg 的code就是100
                            //it.obj -> ActivityClientRecord
                            val intentField = it.obj::class.java.getDeclaredField("intent")
                            intentField.isAccessible = true
                            val proxyIntent = intentField.get(it.obj) as Intent
                            val pluginIntent = proxyIntent.getParcelableExtra<Intent>(TARGET_INTENT)
                            if (pluginIntent != null) {
                                intentField.set(it.obj, pluginIntent)
                            }
                        }

                        159 -> {
                            //EXECUTE_TRANSACTION msg 的code就是159
                            //it.obj -> ClientTransaction
                            val mActivityCallbacksField =
                                it.obj::class.java.getDeclaredField("mActivityCallbacks")
                            mActivityCallbacksField.isAccessible = true
                            val mActivityCallbacks = mActivityCallbacksField.get(it.obj) as List<*>
                            mActivityCallbacks.forEach { clientTransactionItem ->
                                if (clientTransactionItem != null && clientTransactionItem::class.java.name
                                    == "android.app.servertransaction.LaunchActivityItem"
                                ) {
                                    val intentField =
                                        clientTransactionItem::class.java.getDeclaredField("mIntent")
                                    intentField.isAccessible = true
                                    val proxyIntent =
                                        intentField.get(clientTransactionItem) as Intent
                                    val pluginIntent = proxyIntent.getParcelableExtra<Intent>(TARGET_INTENT)
                                    if (pluginIntent != null) {
                                        intentField.set(clientTransactionItem, pluginIntent)
                                    }
                                }
                            }

                        }
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }
                false
            }

            mCallbackField.set(mH, callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}