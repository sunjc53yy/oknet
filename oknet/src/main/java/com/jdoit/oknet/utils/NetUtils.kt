/*
 * Copyright 2022 sunjichang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jdoit.oknet.utils

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import com.jdoit.oknet.OkNet
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.ThreadFactory
import android.net.NetworkInfo
import android.os.Environment
import android.provider.MediaStore
import java.io.FileOutputStream

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/6 9:41 下午
 * @Description:
 */
class NetUtils {

    companion object {
        fun checkUrl(url : String?) : String {
            url?.let {
                try {
                    val u = URL(it)
                    return it
                } catch (e: Exception) {
                    OkNet.instance.getBaseUrl()?.let { baseUrl->
                        return baseUrl.plus("/").plus(it)
                    }
                }
            }
            return ""
        }

        fun threadFactory(name: String?, daemon: Boolean): ThreadFactory? {
            return ThreadFactory { runnable: Runnable? ->
                val result = Thread(runnable, name)
                result.isDaemon = daemon
                result
            }
        }

        private fun writeFileOnR(context : Context, file : File?, data : ByteArray?) : Boolean {
            try {
                if (null == file || null == data || null == context) {
                    return false
                }
                val resolver = context.contentResolver
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                val uri = Uri.fromFile(file)
                val fd = context.contentResolver.openFileDescriptor(uri, "w")
                fd?.let {
                    val out = FileOutputStream(file)
                    out.write(data)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun isConnected() : Boolean{
            val context = OkNet.instance.getContext()
            context?.let {
                val conn = it.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = conn.activeNetworkInfo
                val connected: Boolean = networkInfo?.isConnected() ?: false
                if (networkInfo != null && connected) {
                    return networkInfo.getState() === NetworkInfo.State.CONNECTED
                }
            }
            return false
        }
    }
}