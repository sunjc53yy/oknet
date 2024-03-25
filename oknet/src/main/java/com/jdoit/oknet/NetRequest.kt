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

import android.content.Context
import androidx.annotation.StringDef
import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.cache.NetCache
import com.jdoit.oknet.utils.NetUtils
import java.io.File
import java.lang.reflect.Type

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 12:26 下午
 * @Description:
 */
class NetRequest<T> constructor(var net: IOkNet) {
    /**
     * HTTP 方法类
     */
    @StringDef(Headers.Method.GET, Headers.Method.POST, Headers.Method.DELETE
        , Headers.Method.PUT, Headers.Method.HEAD, Headers.Method.PATCH)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Method

    private lateinit var url : String
    private var method : String = Headers.Method.POST
    private var headerMap : MutableMap<String, String> = mutableMapOf()
    private val canceled : Boolean = false
    private var body : NetRequestBody? = null
    var connectTimeout = 1500
    var readTimeout = 1500
    private var parseClass : Type? = null
    private var target : Context? = null
    private var worker : INetWorker<*>? = null
    private var cache : NetCache = NetCache.NONE_CACHE
    internal var finishCallback : OnFinishCallback? = null
    private var useBaseParser : Boolean? = null
    private var downloadPath : File? = null
    var downloadRequest = false
        internal set
    private var finalUrl : String? = null

    fun getUrl() : String {
        finalUrl?.let {
            return it
        }
        if (method == Headers.Method.GET) { //get请求，需要拼接参数
            if (null != body && body!!.getQuery().isNotEmpty()) {
                finalUrl = NetUtils.formatGetUrl(url, body!!.getQuery())
                return finalUrl!!
            }
        }
        finalUrl = url
        return url
    }

    fun setUrl(url : String) : NetRequest<T> {
        this.url = NetUtils.checkUrl(url)
        return this
    }

    fun getMethod() : String {
        return method
    }

    fun setMethod(@Method method : String = Headers.Method.POST) : NetRequest<T> {
        this.method = method
        return this
    }

    fun addHeader(key : String, value : String) : NetRequest<T> {
        headerMap[key] = value
        return this
    }

    fun getHeaders() : MutableMap<String, String> {
        return headerMap
    }

    fun getHeader(key : String) : String? {
        return headerMap[key]
    }

    fun removeHeader(key : String) {
        headerMap.remove(key)
    }

    fun setBody(body : NetRequestBody) : NetRequest<T> {
        this.body = body
        return this;
    }

    fun getBody() : NetRequestBody? {
        return body
    }

    internal fun setParserClass(clazz: Type) {
        parseClass = clazz
    }

    fun getParserClass() : Type? {
        return parseClass
    }

    fun setTarget(context: Context) : NetRequest<T> {
        target = context
        return this
    }

    fun getTarget() : Context? {
        return target
    }

    internal fun setWorker(worker: INetWorker<*>) {
        this.worker = worker
    }

    fun setCache(cache: NetCache) : NetRequest<T> {
        this.cache = cache
        return this
    }

    fun getCache() : NetCache {
        return cache
    }

    fun setUseBaseParser(use : Boolean) : NetRequest<T> {
        this.useBaseParser = use
        return this
    }

    fun isUseBaseParser() : Boolean {
        useBaseParser?.let {
            return it
        }
        return OkNet.instance.useBaseParser
    }

    fun setDownloadPath(path : File) : NetRequest<T> {
        this.downloadPath = path
        return this
    }

    fun getDownloadPath() : File? {
        return downloadPath
    }

    fun cancel() {
        worker?.cancel()
    }

    fun execute() : NetResponse<T> {
        return net.execute(this)
    }

    fun enqueue(callback: INetCallback<T>?) {
        net.enqueue(this, callback)
    }

    fun <R> adapter(adapter: INetWorkerAdapter<T, R>) : R {
        return net.adapter(this, adapter)
    }

    fun onFinish() {
        finishCallback?.onFinish(this)
    }

    interface OnFinishCallback {
        fun <T> onFinish(request: NetRequest<T>)
    }
}