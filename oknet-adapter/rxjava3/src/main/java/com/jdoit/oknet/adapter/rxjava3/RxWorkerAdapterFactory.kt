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
package com.jdoit.oknet.adapter.rxjava3

import com.jdoit.oknet.INetWorkerAdapter
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/18 7:35 上午
 * @Description:
 */
class RxWorkerAdapterFactory constructor(var scheduler : Scheduler?, var isAsync : Boolean = true) : INetWorkerAdapter.Factory() {

    companion object {
        init {
            RxJavaPlugins.setErrorHandler {
                it.printStackTrace()
            }
        }

        fun create(scheduler : Scheduler, isAsync : Boolean = true) : RxWorkerAdapterFactory {
            return RxWorkerAdapterFactory(scheduler, isAsync)
        }

        fun create(isAsync : Boolean = true) : RxWorkerAdapterFactory {
            return RxWorkerAdapterFactory(null, isAsync)
        }

        fun <T> createObservableAdapter(scheduler : Scheduler? = null, isAsync : Boolean = true) : RxObservableAdapter<T> {
            return RxObservableAdapter(scheduler, isAsync)
        }

        fun <T> createMaybeAdapter(scheduler : Scheduler? = null, isAsync : Boolean = true) : RxMaybeAdapter<T> {
            return RxMaybeAdapter(scheduler, isAsync)
        }

        fun <T> createFlowableAdapter(scheduler : Scheduler? = null, isAsync : Boolean = true) : RxFlowableAdapter<T> {
            return RxFlowableAdapter(scheduler, isAsync)
        }

        fun <T> createSingleAdapter(scheduler : Scheduler? = null, isAsync : Boolean = true) : RxSingleAdapter<T> {
            return RxSingleAdapter(scheduler, isAsync)
        }

        fun <T> createCompletableAdapter(scheduler : Scheduler? = null, isAsync : Boolean = true) : RxCompletableAdapter<T> {
            return RxCompletableAdapter(scheduler, isAsync)
        }
    }

    override fun get(returnType: Type, annotations: Array<Annotation>): INetWorkerAdapter<Any, Any>? {
        val rawType = getRawType(returnType)
        if (rawType != Completable::class.java || rawType != Observable::class.java ||
            rawType != Single::class.java || rawType != Maybe::class.java || rawType != Flowable::class.java) {
            return null
        }
        if (rawType !is ParameterizedType) {
            return null
        }
        val responseType = getParameterUpperBound(0, rawType as ParameterizedType)
        responseType?.let {
            return RxWorkerAdapter<Any>(rawType, it, scheduler, isAsync)
        }
        return null
    }
}