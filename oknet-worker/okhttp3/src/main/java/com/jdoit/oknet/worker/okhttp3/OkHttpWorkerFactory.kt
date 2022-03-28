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

import com.jdoit.oknet.INetWorker
import com.jdoit.oknet.NetHelper
import com.jdoit.oknet.utils.NetLogger
import com.jdoit.oknet.NetRequest
import com.jdoit.oknet.OkNet
import okhttp3.OkHttpClient
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/8 9:26 下午
 * @Description:
 */
class OkHttpWorkerFactory private constructor(): INetWorker.Factory() {
    private var okHttpClient : OkHttpClient? = null

    companion object {
        fun create() : INetWorker.Factory {
            NetLogger.print("use OkHttpWorker")
            return OkHttpWorkerFactory()
        }
    }

    override fun <T> get(request: NetRequest<T>, okNet: OkNet): INetWorker<T> {
        val worker = OkHttpWorker(request, okNet)
        worker.setClient(getClient())
        return worker
    }

    @Synchronized
    private fun getClient() : OkHttpClient{
        if (null != okHttpClient) {
            return okHttpClient!!
        }
        val builder = OkHttpClient.Builder()
            .addInterceptor(OkHttpCacheIntercept())
        val sslSocketFactory = NetHelper.instance.getHttpSSLSocketFactory()
        sslSocketFactory?.let {
            builder.sslSocketFactory(it, NetHelper.instance.getTrustManager()!!)
        }
        builder.hostnameVerifier(HostnameVerifier { hostname, session ->
            NetLogger.print("hostname=$hostname, session=$session")
            val hostnameList = NetHelper.instance.getHttpsVerifierHostnameList()
            return@HostnameVerifier hostnameList?.contains(hostname) ?: true
        })
        okHttpClient = builder.build()
        return okHttpClient!!
    }
}