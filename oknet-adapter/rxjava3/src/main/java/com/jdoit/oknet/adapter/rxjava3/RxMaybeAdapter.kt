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

import com.jdoit.oknet.INetWorker
import com.jdoit.oknet.INetWorkerAdapter
import com.jdoit.oknet.NetResponse
import io.reactivex.rxjava3.core.*

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/28 11:31 下午
 * @Description:
 */
class RxMaybeAdapter<T>(var scheduler : Scheduler?, var isAsync : Boolean = false)
            : INetWorkerAdapter<T, Maybe<NetResponse<T>>> {
    override fun adapter(worker: INetWorker<T>): Maybe<NetResponse<T>> {
        val adapter = RxObservableAdapter<T>(scheduler, isAsync)
        val observable = adapter.adapter(worker)
        return observable.singleElement()
    }
}