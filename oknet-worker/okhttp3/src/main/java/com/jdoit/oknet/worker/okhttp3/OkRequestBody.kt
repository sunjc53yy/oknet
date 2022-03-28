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
package com.jdoit.oknet.worker.okhttp3

import android.content.Context
import com.jdoit.oknet.NetRequest
import com.jdoit.oknet.OkNet
import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.utils.NetFileUtils
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject

import okhttp3.MultipartBody

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/9 10:55 上午
 * @Description:
 */
object OkRequestBody{

    fun <T> create(request : NetRequest<T>, body: NetRequestBody?) : RequestBody? {
        if (body == null) {
            return null
        }
        var intercept = false
        OkNet.instance.getNetInterceptor()?.let {
            val bytes = it.onConvertBody(request.getUrl(), body.getMediaType(), body.getParams())
            intercept = bytes != null
        }
        if (intercept) {
            return createBody(request, body)
        }
        return when (body.getType()) {
            NetRequestBody.JSON -> createJsonBody(body)
            NetRequestBody.FORM -> createFormBody(body)
            NetRequestBody.PROTOBUF -> createStreamBody(body)
            NetRequestBody.FILE -> createFileBody(request.getTarget(), body)
            else -> createBody(request, body)
        }
    }

    private fun createFormBody(body: NetRequestBody) : RequestBody {
        val paramMap = body.getParams()
        val okBody = FormBody.Builder()
        for ((index, key) in paramMap.keys.withIndex()) {
            okBody.addEncoded(key, paramMap[key].toString())
        }
        return okBody.build()
    }

    private fun createJsonBody(body: NetRequestBody) : RequestBody {
        return object : InnerRequestBody() {
            override fun contentType(): MediaType? {
                return body.getMediaType().toMediaType()
            }

            override fun writeTo(sink: BufferedSink) {
                val paramMap = body.getParams()
                val json = JSONObject(paramMap)
                if (null != json) {
                    sink.write(json.toString().toByteArray(body.getParamCharset()))
                }
            }
        }
    }

    private fun createStreamBody(body: NetRequestBody) : RequestBody {
        return object : InnerRequestBody() {
            override fun contentType(): MediaType? {
                return body.getMediaType().toMediaType()
            }

            override fun writeTo(sink: BufferedSink) {
                body.getBytes()?.let { sink.write(it)}
            }
        }
    }

    private fun createFileBody(context: Context?, body: NetRequestBody) : RequestBody {
        val okBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        val paramMap = body.getParams()
        for ((index, key) in paramMap.keys.withIndex()) {
            okBody.addFormDataPart(key, paramMap[key].toString())
        }
        body.getMultiple()?.let {
            for (entity in it) {
                okBody.addFormDataPart(entity.key, entity.fileName,
                    object : RequestBody() {
                        override fun contentType(): MediaType? {
                            return entity.mimeType?.toMediaTypeOrNull()
                        }

                        override fun writeTo(sink: BufferedSink) {
                            entity.fileBytes?.let { bytes->
                                sink.write(bytes)
                                return
                            }
                            if (null != context) {
                                val bytes = NetFileUtils.read(context, entity.fileUri)
                                bytes?.let { b->
                                    sink.write(b)
                                }
                            }
                        }
                    }) //添加文件
            }
        }
        return okBody.build()
    }

    private fun <T> createBody(request : NetRequest<T>, body: NetRequestBody) : RequestBody {
        return object : InnerRequestBody() {
            override fun contentType(): MediaType? {
                return body.getMediaType().toMediaType()
            }

            override fun writeTo(sink: BufferedSink) {
                OkNet.instance.getNetInterceptor()?.let {
                    val bytes = it.onConvertBody(request.getUrl(), body.getMediaType(), body.getParams())
                    bytes?.let { b->
                        sink.write(b)
                    }
                }
            }
        }
    }

    abstract class InnerRequestBody : RequestBody() {

    }
}