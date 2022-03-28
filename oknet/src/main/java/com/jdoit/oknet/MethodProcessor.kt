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
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/13 3:21 下午
 * @Description:
 */
class MethodProcessor internal constructor(var okNet: OkNet) : IOkNet, NetRequest.OnFinishCallback{
    private val requestMap : ConcurrentHashMap<Context, MutableList<NetRequest<*>>> = ConcurrentHashMap()

    private fun <T> nextWorker(request: NetRequest<T>) : INetWorker<T> {
        val factory = okNet.getWorkerFactories()[0]
        val worker = factory.get(request, okNet)
        try {
            request.setWorker(worker)
            request.finishCallback = this
            request.getTarget()?.let {
                var list = requestMap[it]
                if (null == list) {
                    list = mutableListOf()
                }
                list.add(request)
                requestMap.put(it, list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return worker
    }

    override fun <T> execute(request: NetRequest<T>): NetResponse<T> {
        val worker = nextWorker(request)
        return worker.execute()
    }

    override fun <T, R> adapter(request: NetRequest<T>, adapter: INetWorkerAdapter<T, R>): R {
        val worker = nextWorker(request)
        return adapter.adapter(worker)
    }

    override fun <T> enqueue(request: NetRequest<T>, callback: INetCallback<T>?) {
        val worker = nextWorker(request)
        if (null != worker) {
            worker.enqueue(callback)
        }
    }

    override fun cancelAll(context: Context) {
        try {
            val list = requestMap[context]
            if (null != list) {
                for (request in list) {
                    request.cancel()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun <T> onFinish(request: NetRequest<T>) {
        try {
            request.getTarget()?.let {
                val list = requestMap[it]
                if (null != list && list.contains(request)) {
                    list.remove(request)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}