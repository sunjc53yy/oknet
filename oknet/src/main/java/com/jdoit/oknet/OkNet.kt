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
import com.jdoit.oknet.utils.NetClzUtils

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 11:45 上午
 * @Description:
 */
class OkNet private constructor(){
    companion object {
        const val TAG = "OkNet"
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            OkNet()
        }

        fun <T> newRequest(cls : Class<T>) : NetRequest<T> {
            return instance.buildRequest(cls)
        }

        fun <T> newListRequest(cls : Class<T>) : NetRequest<List<T>> {
            return instance.buildListRequest(cls)
        }

        fun newDownloadRequest() : NetRequest<String> {
            val request : NetRequest<String> = instance.buildRequest()
            request.downloadRequest = true
            return request
        }
    }

    private var workerFactories : MutableList<INetWorker.Factory> = mutableListOf()
    private var converterFactories : MutableList<INetConverter.Factory> = mutableListOf()
    private var baseUrl : String? = null
    private var httpsTls : HttpsTls?  = null
    private var netInterceptor : INetInterceptor? = null
    private var okNetProxy : IOkNet
    private var coreThreadCount = 4
    private var context : Context? = null
    private var baseParserModelCls : Class<*>? = null
    private var debug = true

    init {
        //添加内置的网络请求框架和转化器
        workerFactories.add(BuiltinHttpWorkerFactory.create())
        converterFactories.add(BuiltinConvertFactory.create())
        okNetProxy = MethodProcessor(this)
    }

    fun init(context : Context) : OkNet {
        this.context = context
        return this
    }

    /**
     * 设置拦截器
     */
    fun setNetInterceptor(interceptor: INetInterceptor?) : OkNet {
        netInterceptor = interceptor
        return this
    }

    fun getNetInterceptor() : INetInterceptor?{
        return netInterceptor
    }

    /**
     * 添加网络请求框架
     */
    fun addWorkerFactory(factory: INetWorker.Factory) : OkNet {
        workerFactories.add(0, factory)
        return this
    }

    fun getWorkerFactories() : MutableList<INetWorker.Factory>{
        return workerFactories
    }

    /**
     * 添加结果解析转化器
     */
    fun addConverterFactory(factory: INetConverter.Factory) : OkNet {
        converterFactories.add(0, factory)
        return this
    }

    fun getConverterFactories() : MutableList<INetConverter.Factory> {
        return converterFactories
    }

    /**
     * 设置解析基类model
     */
    fun setBaseParserModel(cls : Class<*>) : OkNet{
        baseParserModelCls = cls
        return this
    }

    fun getBaseParserModel() : Class<*> {
        if (null != baseParserModelCls) {
            return baseParserModelCls!!
        }
        return BuiltinBaseModel::class.java
    }

    /**
     * 设置base url
     */
    fun setBaseUrl(url : String) : OkNet {
        this.baseUrl = url
        return this
    }

    fun getBaseUrl() : String? {
        return baseUrl
    }

    /**
     * 设置OKNet中，线程池的核心线程数，默认为4
     */
    fun setCoreThreadCount(threadCount : Int) : OkNet {
        this.coreThreadCount = threadCount
        return this
    }

    fun getCoreThreadCount() : Int {
        return coreThreadCount
    }

    /**
     * 设置Hppts相关的证书
     */
    fun setHttpCA(httpsTls : HttpsTls) : OkNet {
        this.httpsTls = httpsTls
        return this
    }

    fun getHttpCA() : HttpsTls? {
        return httpsTls
    }

    /**
     * 是否打开debug模式
     */
    fun setDebug(debug : Boolean) : OkNet {
        this.debug = debug
        return this
    }

    fun isDebug() : Boolean {
        return debug
    }

    fun getContext() : Context? {
        return context
    }

    /**
     * 取消所有请求
     */
    fun cancelAll(context: Context) {
        okNetProxy.cancelAll(context)
    }

    private fun check() {
        if (context == null) {
            throw IllegalStateException("未初始化，请先调用init()方法")
        }
    }

    private fun <T> buildRequest(cls: Class<T>) : NetRequest<T> {
        check()
        val request = NetRequest<T>(okNetProxy)
        request.setParserClass(cls)
        return request
    }

    private fun <T> buildListRequest(cls: Class<T>) : NetRequest<List<T>> {
        check()
        val request = NetRequest<List<T>>(okNetProxy)
        val type = NetClzUtils.generateType(MutableList::class.java, cls)
        request.setParserClass(type)
        return request
    }

    private fun <T> buildRequest() : NetRequest<T> {
        check()
        val request = NetRequest<T>(okNetProxy)
        request.setParserClass(String::class.java)
        return request
    }
}