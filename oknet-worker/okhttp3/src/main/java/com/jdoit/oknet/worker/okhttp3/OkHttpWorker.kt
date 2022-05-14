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

import com.jdoit.oknet.*
import com.jdoit.oknet.Headers
import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.cache.NetCacheManager
import com.jdoit.oknet.utils.NetFileUtils
import com.jdoit.oknet.utils.NetLogger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/8 9:25 下午
 * @Description:
 */
class OkHttpWorker<T>(request: NetRequest<T>, okNet: OkNet) :
    BaseHttpWorker<T>(request, okNet) {
    private val bufferSize  = 4096
    private var call : Call? = null
    private lateinit var client : OkHttpClient

    fun setClient(client : OkHttpClient) {
        this.client = client
    }

    override fun execute(): NetResponse<T> {
        val req = newRequest()
        onRequestStart()
        call = client.newCall(req)
        var callEnd = false
        try {
            val okResp = call!!.execute()
            onRequestEnd()
            callEnd = true
            val rawResponse = covertToRawResponse(okResp, null)
            NetLogger.printHttpResponse(request.getUrl(), rawResponse)
            val response = NetResponse<T>()
            response.rawResponse = rawResponse
            val result = convert(rawResponse)
            response.data = result.data
            response.exception = result.fail
            response.request = request
            onRequestSuccess(rawResponse)
            OkNet.instance.getNetInterceptor()?.onInterceptHttpCode(okResp.code)
            return response
        } catch (e: Exception) {
            if (!callEnd) {
                onRequestEnd()
            }
            val rawResponse = RawResponse()
            rawResponse.exception = e
            val response = NetResponse<T>()
            val failResponse = NetFailResponse(e.message, e)
            response.rawResponse = rawResponse
            response.exception = failResponse
            response.request = request
            onRequestFail(failResponse)
            return response
        } finally {
            request.onFinish()
        }
    }

    override fun enqueue(callback: INetCallback<T>?) {
        NetSync.instance.runOnMain{
            callback?.onStart(request)
            onRequestStart()
        }
        val req = newRequest()
        call = client.newCall(req)
        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onRequestEnd()
                request.onFinish()
                if (request.getCache().isFailGetModel()) { //读取缓存
                    val cacheBody = NetCacheManager.read(request)
                    if (cacheBody?.content != null) {
                        val body = cacheBody.content!!.toResponseBody(cacheBody.mimeType?.toMediaType())
                        val resp = Response.Builder()
                            .code(Headers.ResponseCode.USE_CACHE)
                            .request(call.request())
                            .protocol(Protocol.HTTP_1_1)
                            .message("use cache")
                            .body(body)
                            .build()
                        onSuccess(resp, callback, true)
                        return
                    }
                }
                val rawResponse = RawResponse()
                rawResponse.exception = e
                NetLogger.printHttpResponse(request.getUrl(), rawResponse)
                NetSync.instance.runOnMain {
                    val failResponse = NetFailResponse(e.message, e)
                    onRequestFail(failResponse)
                    callback?.onFailure(failResponse)
                }
                NetSync.instance.runOnMain{
                    callback?.onFinish(request)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                onRequestEnd()
                request.onFinish()
                onSuccess(response, callback, false)
            }
        })
    }

    private fun onSuccess(okResp: Response, callback: INetCallback<T>?, cache: Boolean) {
        val rawResponse = covertToRawResponse(okResp, callback)
        if (!cache) {
            NetLogger.printHttpResponse(request.getUrl(), rawResponse)
        }
        val result = convert(rawResponse)
        NetSync.instance.runOnMain {
            result.fail?.let {
                onRequestFail(it)
                callback?.let { cb->
                    cb.onFailure(it)
                    cb.onFinish(request)
                }
                return@runOnMain
            }
            val netResponse = NetResponse<T>()
            netResponse.rawResponse = rawResponse
            netResponse.cache = cache
            netResponse.data = result.data
            netResponse.request = request
            onRequestSuccess(rawResponse)
            callback?.let {
                it.onResponse(netResponse)
                it.onFinish(request)
            }
            OkNet.instance.getNetInterceptor()?.onInterceptHttpCode(okResp.code)
        }
    }

    private fun newRequest() : Request {
        val requestBuild = Request.Builder()
            .url(request.getUrl())
            .tag(request)
        val headers = request.getHeaders()
        OkNet.instance.getNetInterceptor()?.onInterceptHeader(request.getUrl(), headers)
        for (key in headers.keys) {
            headers[key]?.let { value -> requestBuild.header(key, value) }
        }
        request.getBody()?.let {
            OkNet.instance.getNetInterceptor()?.onInterceptParams(request.getUrl(), it.getParams())
            val type = it.getMediaType()
            requestBuild.header(Headers.Key.ContentType, if (it.getType() == NetRequestBody.FILE) type.plus("; boundary=").plus(Headers.C.UPLOAD_BOUNDARY) else type)
        }
        NetLogger.printHttpRequest(request)
        val body = OkRequestBody.create(request, request.getBody())
        when(request.getMethod()) {
            Headers.Method.GET-> requestBuild.get()
            Headers.Method.POST-> body?.let { requestBuild.post(it) }
            Headers.Method.DELETE-> body?.let { requestBuild.delete(it) }
            Headers.Method.HEAD-> requestBuild.head()
            Headers.Method.PUT-> body?.let { requestBuild.put(it) }
            Headers.Method.PATCH-> body?.let { requestBuild.patch(it) }
        }
        return requestBuild.build()
    }

    private fun covertToRawResponse(resp : Response, callback: INetCallback<T>?) : RawResponse {
        val rawResponse = RawResponse()
        resp.body?.let { body->
            rawResponse.mimeType = body.contentType()?.toString()
            rawResponse.contentLength = body.contentLength()
            body.byteStream().use { ipt->
                val bos = ByteArrayOutputStream()
                val buff = ByteArray(bufferSize)
                var read = 0
                val total = body.contentLength()
                var download = 0
                bos.also { out->
                    while (!isCancel.get() && ipt.read(buff).also { read = it } != -1) {
                        out.write(buff, 0, read)
                        download += read
                        callback?.onDownloadProgress(total, download.toLong())
                    }
                }
                if (request.downloadRequest) {
                    NetSync.instance.execute(object : NetSync.NetRunnable() {
                        override fun execute() {
                            NetFileUtils.write(okNet.getContext(), request.getDownloadPath(), bos.toByteArray(), request.getUrl())
                        }
                    })
                    rawResponse.content = "download success".toByteArray()
                } else {
                    rawResponse.content = bos.toByteArray()
                }
            }
        }
        rawResponse.code = resp.code
        rawResponse.success = resp.isSuccessful
        rawResponse.setHeaders(resp.headers.toMultimap())
        return rawResponse
    }

    override fun cancel() {
        super.cancel()
        call?.let {
            if (!it.isCanceled()) {
                it.cancel()
            }
        }
    }
}