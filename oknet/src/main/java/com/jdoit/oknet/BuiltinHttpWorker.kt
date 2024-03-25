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
package com.jdoit.oknet

import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.cache.NetCacheManager
import com.jdoit.oknet.utils.NetFileUtils
import com.jdoit.oknet.utils.NetLogger
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 2:32 下午
 * @Description: 内置http请求
 */
class BuiltinHttpWorker<T>(request: NetRequest<T>, okNet: OkNet) :
        BaseHttpWorker<T>(request, okNet) {
    private val bufferSize  = 4096
    private var callback : INetCallback<T>? = null

    override fun execute(): NetResponse<T> {
        onRequestStart()
        val rawResponse = call()
        val result = convert(rawResponse)
        val response = NetResponse<T>()
        response.cache = rawResponse.cache
        response.rawResponse = rawResponse
        response.data = result.data
        response.exception = result.fail
        response.request = request
        result.base?.let {
            response.setExtData(it)
        }
        request.onFinish()
        if (rawResponse.success) {
            onRequestSuccess(rawResponse)
        } else {
            onRequestFail(result.fail)
        }
        return response
    }

    override fun enqueue(callback: INetCallback<T>?) {
        this.callback = callback
        NetSync.instance.runOnMain{
            callback?.onStart(request)
        }
        onRequestStart()
        NetSync.instance.execute(runnable = object : NetSync.NetRunnable() {
            override fun execute() {
                val rawResponse = call()
                val result = convert(rawResponse)
                NetSync.instance.runOnMain {
                    request.onFinish()
                    if (rawResponse.success) {
                        result.fail?.let {
                            onRequestFail(it)
                            callback?.onFailure(it)
                            return@runOnMain
                        }
                        val response = NetResponse<T>()
                        response.cache = rawResponse.cache
                        response.rawResponse = rawResponse
                        response.data = result.data
                        response.request = request
                        result.base?.let {
                            response.setExtData(it)
                        }
                        callback?.onResponse(response)
                        onRequestSuccess(rawResponse)
                    } else {
                        rawResponse.exception?.let {
                            val failResponse = NetFailResponse(it.message, it)
                            onRequestFail(failResponse)
                            callback?.onFailure(failResponse)
                        }
                    }
                    callback?.onFinish(request)
                }
            }
        })
    }

    private fun call() : RawResponse {
        val response = RawResponse()
        var callEnd = false
        try {
            request.getBody()?.let {
                OkNet.instance.getNetInterceptor()?.onInterceptParams(request.getUrl(), it.getParams())
            }
            if (request.getCache().isPriorityModel()) {
                val cacheBody = NetCacheManager.read(request)
                if (null != cacheBody) {
                    onRequestEnd()
                    callEnd = true
                    response.content = cacheBody.content
                    response.mimeType = cacheBody.mimeType
                    response.code = Headers.ResponseCode.USE_CACHE
                    response.success = true
                    response.cache = true
                    return response
                }
            }
            val conn = openConnection(request)
            val headers = request.getHeaders()
            OkNet.instance.getNetInterceptor()?.onInterceptHeader(request.getUrl(), headers)
            for (key in headers.keys) {
                conn.addRequestProperty(key, headers[key])
            }
            request.getBody()?.let {
                val type = it.getMediaType()
                conn.addRequestProperty(Headers.Key.ContentType, if (it.getType() == NetRequestBody.FILE) type.plus("; boundary=").plus(Headers.C.UPLOAD_BOUNDARY) else type)
            }
            NetLogger.printHttpRequest(request)
            conn.connect()
            if (request.getMethod() == Headers.Method.POST ||
                request.getMethod() == Headers.Method.DELETE ||
                request.getMethod() == Headers.Method.PATCH ||
                request.getMethod() == Headers.Method.PUT ||
                request.getMethod() == Headers.Method.GET) {
                write(conn, request)
            }
            onRequestEnd()
            callEnd = true
            val responseCode = conn.responseCode
            response.code = responseCode
            response.content = readData(conn.inputStream)

            val error = conn.errorStream
            response.error = error?.buffered(bufferSize)?.readBytes()
            error?.close()
            response.mimeType = conn.getHeaderField("Content-Type")
            response.success = true
            response.setHeaders(conn.headerFields)

            if (request.getCache().allowCache()) { //缓存到本地
                NetCacheManager.cache(response.content, response.mimeType.toString(), request)
            }
            NetLogger.printHttpResponse(request.getUrl(), response)
            OkNet.instance.getNetInterceptor()?.onInterceptHttpCode(request, responseCode)
            return response
        } catch (e: Exception) {
            if (!callEnd) {
                onRequestEnd()
            }
            if (request.getCache().isFailGetModel()) {
                val cacheBody = NetCacheManager.read(request)
                if (null != cacheBody) {
                    response.content = cacheBody.content
                    response.mimeType = cacheBody.mimeType
                    response.code = Headers.ResponseCode.USE_CACHE
                    response.success = true
                    response.cache = true
                    return response
                }
            }
            response.exception = e
            response.success = false
            NetLogger.printHttpResponse(request.getUrl(), response)
            return response
        }
    }

    private fun openConnection(request: NetRequest<T>) : HttpURLConnection {
        val url = URL(request.getUrl())
        val conn = url.openConnection() as HttpURLConnection
        if (conn is HttpsURLConnection) {
            conn.sslSocketFactory = NetHelper.instance.getHttpSSLSocketFactory()
            conn.setHostnameVerifier { hostname, _ ->
                val hostList = NetHelper.instance.getHttpsVerifierHostnameList()
                hostList?.let {
                    it.forEach { host->
                        if (host == hostname) {
                            return@setHostnameVerifier true
                        }
                    }
                    return@setHostnameVerifier false
                }
                return@setHostnameVerifier false
            }
        }
        conn.requestMethod = request.getMethod()
        if (Headers.Method.POST == request.getMethod()) {
            conn.doInput = true
            conn.doOutput = true
            conn.setChunkedStreamingMode(0)
            conn.useCaches = false
        }
        conn.connectTimeout = request.connectTimeout
        conn.readTimeout = request.readTimeout
        return conn
    }

    private fun write(conn : HttpURLConnection, request : NetRequest<T>) {
        val body = request.getBody()
        val url = request.getUrl()
        body?.let {
            okNet.getNetInterceptor()?.let { intercept->
                val data = intercept.onConvertBody(url, it.getMediaType(), it.getParams())
                data?.let { byte->
                    val outStream = conn.outputStream
                    outStream.write(byte)
                    outStream.flush()
                    outStream.close()
                    return
                }
            }
            when {
                it.getType() == NetRequestBody.FORM -> {
                    writeFormData(conn, it)
                }
                it.getType() == NetRequestBody.JSON -> {
                    writeJsonData(conn, it)
                }
                it.getType() == NetRequestBody.FILE -> {
                    writeFileData(request, conn, it)
                }
                it.getType() == NetRequestBody.PROTOBUF -> {
                    writeStreamData(conn, it)
                }
            }
        }
    }

    private fun writeFormData(conn : HttpURLConnection, body : NetRequestBody) {
        val outStream = conn.outputStream
        val paramMap = body.getParams()
        if (paramMap.isNotEmpty()) {
            val sb = StringBuilder()
            for ((index, key) in paramMap.keys.withIndex()) {
                sb.append(key).append("=").append(paramMap[key])
                if (index < paramMap.size - 1) {
                    sb.append("&")
                }
            }
            outStream.write(sb.toString().toByteArray(body.getParamCharset()))
        }
        outStream.flush()
        outStream.close()
    }

    private fun writeJsonData(conn : HttpURLConnection, body : NetRequestBody) {
        val outStream = conn.outputStream
        val paramMap = body.getParams()
        val json = JSONObject(paramMap)
        outStream.write(json.toString().toByteArray(body.getParamCharset()))
        outStream.flush()
        outStream.close()
    }

    /**
     * 文件上传
     */
    private fun writeFileData(request: NetRequest<T>, conn : HttpURLConnection, body : NetRequestBody) {
        //写参数
        val outStream = conn.outputStream
        val paramMap = body.getParams()
        val sb = StringBuilder()
        if (paramMap.isNotEmpty()) {
            for (key in paramMap.keys) {
                sb.clear()
                sb.append(Headers.C.UPLOAD_PREFIX).append(Headers.C.UPLOAD_BOUNDARY).append(Headers.C.UPLOAD_LINE_END)
                sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(Headers.C.UPLOAD_LINE_END)
                    .append(Headers.C.UPLOAD_LINE_END)
                    .append(paramMap[key]).append(Headers.C.UPLOAD_LINE_END)
                NetLogger.print(sb.toString())
                outStream.write(sb.toString().toByteArray(body.getParamCharset()))
            }
        }
        //写文件
        val multipleList = body.getMultiple()
        multipleList?.let {
            for (entity in it) {
                sb.clear()
                //写文件参数
                sb.append(Headers.C.UPLOAD_PREFIX).append(Headers.C.UPLOAD_BOUNDARY).append(Headers.C.UPLOAD_LINE_END)
                sb.append("Content-Disposition: form-data; name=\"").append(entity.key).append("\"")
                    .append("; filename=\"").append(entity.fileName).append("\"").append(Headers.C.UPLOAD_LINE_END)
                    .append(String.format("Content-Type: ${entity.mimeType}; charset=${body.getParamCharset().name()}")).append(Headers.C.UPLOAD_LINE_END)
                    .append(Headers.C.UPLOAD_LINE_END)
                NetLogger.print(sb.toString())
                outStream.write(sb.toString().toByteArray(body.getParamCharset()))
                //写文件内容
                if (entity.fileBytes != null) {
                    outStream.write(entity.fileBytes)
                } else {
                    request.getTarget()?.let { ctx->
                        val bytes = NetFileUtils.read(ctx, entity.fileUri)
                        bytes?.let { b->
                            outStream.write(b)
                        }
                    }
                }
                sb.clear()
                sb.append(Headers.C.UPLOAD_LINE_END)
                NetLogger.print(sb.toString())
                outStream.write(sb.toString().toByteArray(body.getParamCharset()))
            }
        }
        //结束标志
        sb.clear()
        sb.append(Headers.C.UPLOAD_PREFIX).append(Headers.C.UPLOAD_BOUNDARY).append(Headers.C.UPLOAD_LINE_END)
        NetLogger.print(sb.toString())
        outStream.write(sb.toString().toByteArray(body.getParamCharset()))

        outStream.flush()
        outStream.close()
    }

    private fun writeStreamData(conn : HttpURLConnection, body : NetRequestBody) {
        val outStream = conn.outputStream
        body.getBytes()?.let {
            outStream.write(it)
            outStream.flush()
            outStream.close()
        }
    }

    private fun readData(input : InputStream) : ByteArray? {
        var read: Int = -1
        val bos = ByteArrayOutputStream()
        val buff = ByteArray(bufferSize)
        val total = input.available()
        var download = 0
        input.use { ipt ->
            bos.use { out ->
                while (!isCancel.get() && ipt.read(buff).also { read = it } != -1) {
                    out.write(buff, 0, read)
                    download += read
                    callback?.onDownloadProgress(total.toLong(), download.toLong())
                }
            }
            if (request.downloadRequest) {
                NetSync.instance.execute(object : NetSync.NetRunnable() {
                    override fun execute() {
                        NetFileUtils.write(
                            okNet.getContext(),
                            request.getDownloadPath(),
                            bos.toByteArray(),
                            request.getUrl()
                        )
                    }
                })
                return "download success".toByteArray()
            }
        }
        return bos.toByteArray()
    }
}