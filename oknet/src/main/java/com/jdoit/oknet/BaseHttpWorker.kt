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

import com.jdoit.oknet.utils.NetClzUtils
import com.jdoit.oknet.utils.NetLogger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/21 11:22 下午
 * @Description:
 */
abstract class BaseHttpWorker<T>(var request: NetRequest<T>, var okNet: OkNet) : INetWorker<T> {
    open var isCancel : AtomicBoolean = AtomicBoolean(false)
    private var requestTime : Long = 0
    private var consumingTime : Long = 0
    abstract override fun execute(): NetResponse<T>

    abstract override fun enqueue(callback: INetCallback<T>?)

    override fun cancel() {
        isCancel = AtomicBoolean(true)
    }

    /**
     * 数据转换成model
     */
    fun convert(response: RawResponse) : ParserResult<T> {
        val parseResult : ParserResult<T> = ParserResult()
        try {
            val list = okNet.getConverterFactories()
            var converter : INetConverter<T>?
            if (!response.success) {
                parseResult.fail = NetFailResponse(
                    Headers.ResponseMsg.PARSER_FAIL, null,
                    Headers.ResponseCode.PARSER_FAIL
                )
                return parseResult
            }
            NetLogger.print("mediaType=${response.mimeType}")
            for (factory in list) {
                converter = factory.create(response.mimeType)
                converter?.let {
                    request.getParserClass()?.let { cls ->
                        var finalCls = cls
                        if (request.isUseBaseParser()) {
                            finalCls = NetClzUtils.generateType(okNet.getBaseParserModel(), cls)
                            val result = it.convert(finalCls, response)
                            result?.let {
                                val model = result as INetBaseModel<T>
                                if (OkNet.instance.getNetInterceptor()?.isSuccessBusinessCode(model.getCode()) == false) {
                                    parseResult.data = null
                                    parseResult.fail = NetFailResponse(model.getMessage(), null, model.getCode())
                                } else {
                                    parseResult.data = model.getData()
                                }
                                return parseResult
                            }
                        }
                        val result = it.convert(finalCls, response)
                        parseResult.data = result
                        return parseResult
                    }
                }
            }
            parseResult.fail = NetFailResponse(
                Headers.ResponseMsg.PARSER_FAIL, null,
                Headers.ResponseCode.PARSER_FAIL
            )
            return parseResult
        } catch (e: Exception) {
            parseResult.data = null
            parseResult.fail = NetFailResponse(e.message, e)
        }
        return parseResult
    }

    /**
     * 网络请求开始
     */
    protected fun onRequestStart() {
        requestTime = System.currentTimeMillis()
        okNet.getNetInterceptor()?.let {
            NetSync.instance.runOnMain {
                it.onRequestStart(request)
            }
        }
    }

    /**
     * 网络请求结束
     */
    protected fun onRequestEnd() {
        consumingTime = System.currentTimeMillis() - requestTime
    }

    protected fun onRequestSuccess(response: RawResponse) {
        response.consumingTime = consumingTime
        okNet.getNetInterceptor()?.let {
            NetSync.instance.runOnMain {
                it.onRequestSuccess(request, response)
            }
        }
    }

    protected fun onRequestFail(response: NetFailResponse?) {
        response?.consumingTime = consumingTime
        okNet.getNetInterceptor()?.let {
            NetSync.instance.runOnMain {
                it.onRequestFail(request, response)
            }
        }
    }

    class ParserResult<T>{
        var data : T? = null
        var fail : NetFailResponse? = null
    }
}