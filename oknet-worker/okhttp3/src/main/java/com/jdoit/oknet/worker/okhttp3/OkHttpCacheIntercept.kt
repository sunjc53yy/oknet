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

import com.jdoit.oknet.Headers
import com.jdoit.oknet.NetRequest
import com.jdoit.oknet.cache.NetCacheManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/11 9:02 下午
 * @Description:
 */
class OkHttpCacheIntercept : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestTag = request.tag()
        var netReq = if (requestTag == NetRequest::class.java) requestTag as NetRequest<*> else null
        if (null != netReq && netReq.getCache().isPriorityModel()) {
            val cacheBody = NetCacheManager.read(netReq)
            if (cacheBody?.content != null) {
                val body = cacheBody.content!!.toResponseBody(cacheBody.mimeType?.toMediaType())
                return Response.Builder().code(Headers.ResponseCode.USE_CACHE)
                    .body(body)
                    .request(request)
                    .build()
            }
        }
        val response = chain.proceed(request)
        if (!response.isSuccessful && netReq?.getCache()?.isFailGetModel() == true) {
            val cacheBody = NetCacheManager.read(netReq)
            if (cacheBody?.content != null) {
                val body = cacheBody.content!!.toResponseBody(cacheBody.mimeType?.toMediaType())
                return Response.Builder().code(Headers.ResponseCode.USE_CACHE)
                    .body(body)
                    .request(request)
                    .build()
            }
        }
        if (null != netReq && netReq.getCache().allowCache()) {
            response.body?.let {
                NetCacheManager.cache(it.bytes(), it.contentType()?.toString(), netReq)
            }
        }
        return response
    }
}