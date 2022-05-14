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
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/24 3:27 下午
 * @Description:
 */
object NetFileUtils {
    fun read(context : Context, uri : Uri?) : ByteArray? {
        try {
            if (null == uri) {
                return null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return readFileOnR(context, uri)
            }
            val input = FileInputStream(uri.toFile())
            val bytes = input.readBytes()
            input.close()
            return bytes
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun readFileOnR(context : Context, uri : Uri) : ByteArray? {
        try {
            val fd = context.contentResolver.openFileDescriptor(uri, "r")
            fd?.let {
                val input = FileInputStream(it.fileDescriptor)
                val bytes = input.readBytes()
                input.close()
                return bytes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun write(context : Context?, file : File?, data : ByteArray?, url : String) : Uri? {
        try {
            if (null == data || null == context) {
                return null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return writeOnR(context, data, url)
            }
            if (null == file) {
                return null
            }
            val out = FileOutputStream(file)
            out.use {
                out.write(data)
                return Uri.fromFile(file)
            }
        } catch (e: Exception) {
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeOnR(context : Context, data : ByteArray, url : String) : Uri? {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        var name = url.substring(url.lastIndexOf("/") + 1)
        if (null == name) {
            name = System.currentTimeMillis().toString()
        }
        val file = File(path, name)
        val out = FileOutputStream(file)
        out.use {
            it.write(data)
        }
        return Uri.fromFile(file)
    }
}