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

import com.jdoit.oknet.*
import com.jdoit.oknet.utils.NetLogger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.observers.LambdaObserver
import io.reactivex.rxjava3.plugins.RxJavaPlugins

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/12 9:49 下午
 * @Description:
 */
internal class RxEnqueueObservable<T>(var worker : INetWorker<T>) : Observable<NetResponse<T>>(){

    override fun subscribeActual(observer: Observer<in NetResponse<T>>?) {
        val disposable = WorkerDisposable(worker, observer)
        observer?.let {
            it.onSubscribe(disposable)
            worker.enqueue(disposable)
        }
    }

    inner class WorkerDisposable(var worker : INetWorker<T>, var observer: Observer<in NetResponse<T>>?) :
        Disposable, INetCallback<T>{
        @Volatile
        var disposed = false
        @Volatile
        var completed = false

        override fun dispose() {
            disposed = true
            worker.cancel()
        }

        override fun isDisposed(): Boolean {
            return disposed
        }

        override fun onFailure(exception: NetFailResponse) {
            if (!disposed) {
                observer?.onError(exception)
            }
        }

        override fun onResponse(response: NetResponse<T>) {
            observer?.let {
                try {
                    if (!disposed) {
                        it.onNext(response)
                    }
                    if (!disposed) {
                        completed = true
                        it.onComplete()
                    }
                } catch (e: Throwable) {
                    if (completed) {
                        RxJavaPlugins.onError(e)
                    } else if (!disposed) {
                        error(NetFailResponse(e.message, e), it)
                    }
                }
            }
        }

        override fun onStart(request: NetRequest<T>) {
        }

        override fun onFinish(request: NetRequest<T>) {
        }

        override fun onDownloadProgress(total: Long, download: Long) {
            NetLogger.print("total=$total, download=$download")
        }
    }

    private fun error(error : NetFailResponse, observer: Observer<in NetResponse<T>>?) {
        if (observer is LambdaObserver) {
            if (observer.hasCustomOnError()) {
                observer.onError(error)
            }
        } else {
            try {
                observer?.onError(error)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}