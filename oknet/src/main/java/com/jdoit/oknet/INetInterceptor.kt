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

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/13 9:20 下午
 * @Description:
 */
interface INetInterceptor {
    /**
     * 拦截header，主要用来插入一些公共头信息
     */
    fun onInterceptHeader(url : String, header: MutableMap<String, String>)

    /**
     * 拦截参数，主要用来插入一些公共参数
     */
    fun onInterceptParams(url : String, params: MutableMap<String, Any?>)

    /**
     * 把body由map转为byte数组，主要用来做一些加密参数操作
     */
    fun onConvertBody(url : String, @NetRequestBody.MediaType mediaType : String,
                      param: MutableMap<String, Any?>) : ByteArray?

    /**
     * 判断返回的业务code是否代表成功
     */
    fun isSuccessBusinessCode(code : Int) : Boolean

    fun onInterceptHttpCode(code : Int) : Boolean

    fun <T> onRequestStart(request: NetRequest<T>)

    fun <T> onRequestSuccess(request: NetRequest<T>, response: RawResponse)

    fun <T> onRequestFail(request: NetRequest<T>, response: NetFailResponse?)

    open class INetInterceptorAdapter : INetInterceptor {
        override fun onInterceptHeader(url: String, header: MutableMap<String, String>) {

        }

        override fun onInterceptParams(url: String, params: MutableMap<String, Any?>) {
        }


        override fun onConvertBody(
            url: String,
            mediaType: String,
            param: MutableMap<String, Any?>
        ): ByteArray? {
            return null
        }

        override fun isSuccessBusinessCode(code: Int): Boolean {
            return code == 200
        }

        override fun onInterceptHttpCode(code: Int): Boolean {
            return false
        }

        override fun <T> onRequestStart(request: NetRequest<T>) {
        }

        override fun <T> onRequestSuccess(request: NetRequest<T>, response: RawResponse) {
        }

        override fun <T> onRequestFail(request: NetRequest<T>, response: NetFailResponse?) {
        }
    }
}