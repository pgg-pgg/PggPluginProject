package com.pgg.pggpluginproject

import android.annotation.SuppressLint
import android.content.Context
import dalvik.system.DexClassLoader

object LoadApkUtils {
    const val apkName = "/plugin-debug.apk"

    @SuppressLint("DiscouragedPrivateApi")
    fun loadClass(context: Context) {
        /**
         * 宿主dexElements = 宿主dexElements + 插件dexElements
         *
         * 1.获取宿主dexElements
         * 2.获取插件dexElements
         * 3.合并两个dexElements
         * 4.将新的dexElements 赋值到 宿主dexElements
         *
         * 目标：dexElements  -- DexPathList类的对象 -- BaseDexClassLoader的对象，类加载器
         *
         * 获取的是宿主的类加载器  --- 反射 dexElements  宿主
         *
         * 获取的是插件的类加载器  --- 反射 dexElements  插件
         */
        try {
            //获取BaseDexClassLoader中的pathList变量
            val baseClassLoaderClazz = Class.forName("dalvik.system.BaseDexClassLoader")
            val pathList = baseClassLoaderClazz.getDeclaredField("pathList")
            pathList.isAccessible = true
            //获取DexPathList的dexElements变量
            val dexPathListClazz = Class.forName("dalvik.system.DexPathList")
            val dexElements = dexPathListClazz.getDeclaredField("dexElements")
            dexElements.isAccessible = true

            //获取宿主类加载器
            val mainClassLoader = context.classLoader
            val hostPathList = pathList.get(mainClassLoader)
            val hostElements = dexElements.get(hostPathList) as Array<*>

            val dexClassLoader = DexClassLoader(
                context.externalCacheDir?.absolutePath + apkName,
                context.externalCacheDir?.absolutePath,
                null,
                mainClassLoader
            )
            val pluginPathList = pathList.get(dexClassLoader)
            val pluginElements = dexElements.get(pluginPathList) as Array<*>
            // 创建一个新数组
            val newDexElements = java.lang.reflect.Array.newInstance(
                hostElements::class.java.componentType!!,
                hostElements.size + pluginElements.size
            ) as Array<*>
            System.arraycopy(
                hostElements, 0, newDexElements,
                0, hostElements.size
            )
            System.arraycopy(
                pluginElements, 0, newDexElements,
                hostElements.size, pluginElements.size
            )
            dexElements.set(hostPathList, newDexElements)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}