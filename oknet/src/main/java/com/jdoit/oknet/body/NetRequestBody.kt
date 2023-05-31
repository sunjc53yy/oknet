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
package com.jdoit.oknet.body

import android.net.Uri
import android.os.Build
import androidx.annotation.StringDef
import com.jdoit.oknet.Headers
import com.jdoit.oknet.OkNet
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 1:37 下午
 * @Description:
 */
class NetRequestBody{

    companion object {
        /**
         * json请求
         */
        const val JSON = "json"

        /**
         * from表单请求
         */
        const val FORM = "form"

        /**
         * 文件上传
         */
        const val FILE = "file"
        const val PROTOBUF = "protobuf"
        const val XML = "xml"

        fun body() : NetRequestBody {
            val body = NetRequestBody()
            body.setType(FORM)
            return body
        }

        fun jsonBody() : NetRequestBody {
            val body = NetRequestBody()
            body.setType(JSON)
            return body
        }

        fun fileBody() : NetRequestBody {
            val body = NetRequestBody()
            body.setType(FILE)
            return body
        }

        fun pbBody() : NetRequestBody {
            val body = NetRequestBody()
            body.setType(PROTOBUF)
            return body
        }

        fun downloadBody() : NetRequestBody {
            val body = NetRequestBody()
            body.setType(FILE)
            return body
        }
    }

    @StringDef(JSON, FORM, FILE, PROTOBUF, XML)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class MediaType

    private var paramMap : MutableMap<String, Any?> = mutableMapOf()
    private var multipleEntities: MutableList<MultipleEntity>? = null
    private var type : String = FORM
    private var paramCharset : String = "UTF-8"
    private var bytes : ByteArray? = null

    fun param(key : String, value : Any?) : NetRequestBody {
        return param(key, value, true)
    }

    fun param(key : String, value : Any?, encoder : Boolean = true) : NetRequestBody {
        value?.let {
            if (it == String::class.java && encoder) {
                paramMap[key] = URLEncoder.encode(it as String)
                return this
            }
        }
        paramMap[key] = value
        return this
    }

    fun param(params : MutableMap<String, Any>?) : NetRequestBody {
        return param(params, true)
    }

    fun param(params : MutableMap<String, Any>?, encoder : Boolean = true) : NetRequestBody {
        params?.let {
            paramMap.clear()
            for ((index, key) in it.keys.withIndex()) {
                param(key, params[key], encoder)
            }
        }
        return this
    }

    fun getParams() : MutableMap<String, Any?>{
        return paramMap
    }

    fun multiple(multiple : MultipleEntity) : NetRequestBody {
        if (null == multipleEntities) {
            multipleEntities = mutableListOf()
        }
        multipleEntities!!.add(multiple)
        return this
    }

    fun getMultiple() : MutableList<MultipleEntity>? {
        return multipleEntities
    }

    fun setParamCharset(charset : String) : NetRequestBody {
        paramCharset = charset
        return this
    }

    fun getParamCharset() : Charset {
        return try {
            Charset.forName(paramCharset)
        } catch (e: Exception) {
            Charset.forName("UTF-8")
        }
    }

    fun setType(@MediaType type : String) : NetRequestBody {
        this.type = type
        return this
    }

    fun getType() : String {
        return type
    }

    fun setBytes(bytes: ByteArray?) : NetRequestBody {
        this.bytes = bytes
        return this
    }

    fun getBytes() : ByteArray? {
        return bytes
    }

    fun getMediaType() : String {
        return when(type) {
            FILE -> Headers.MediaType.FILE
            JSON -> Headers.MediaType.JSON
            PROTOBUF -> Headers.MediaType.STREAM
            FORM -> Headers.MediaType.FORM
            XML -> Headers.MediaType.XML
            else -> Headers.MediaType.FORM
        }
    }

    class MultipleEntity private constructor(
        val key: String,
        val fileName: String? = null,
        val fileUri: Uri? = null,
        val mimeType : String? = null,
        val fileBytes: ByteArray? = null){

        companion object {
            /**
             * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
             */
            inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
        }

        class Builder{
            //文件上传key
            lateinit var key: String
            //文件名
            var fileName: String? = null
                get() {
                    if (field == null && file != null) {
                        return file!!.name
                    }
                    return field
                }
            //进度回调函数
            //private val callback: UploadProgressCallback? = null
            //文件内容
            var file: File? = null
                set(value) {
                    //android 10 不支持file对象作为参数
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return
                    }
                    field = value
                }
            var fileUri : Uri? = null
                get() {
                    if (field != null) {
                        return field
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return null
                    }
                    if (null != file) {
                        return Uri.fromFile(file)
                    }
                    return null
                }
            var mimeType : String? = null
                get() {
                    if (field == null && file != null) {
                        return file!!.extension
                    }
                    if (field == null) {
                        return Headers.MediaType.STREAM
                    }
                    return field
                }
            var fileBytes: ByteArray? = null

            fun build() = MultipleEntity(key ,fileName, fileUri, mimeType, fileBytes)
        }
    }
}