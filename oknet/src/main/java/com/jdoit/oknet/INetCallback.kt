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

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/12 6:07 下午
 * @Description:
 */
interface INetCallback<T> {
    fun onStart(request: NetRequest<T>)
    fun onFailure(exception: NetFailResponse)
    fun onResponse(response: NetResponse<T>)
    fun onFinish(request: NetRequest<T>)
    fun onDownloadProgress(total : Long, download : Long)

    abstract class NetSimpleCallback<T> : INetCallback<T>{
        override fun onStart(request: NetRequest<T>) {
        }

        override fun onFailure(exception: NetFailResponse) {
        }

        override fun onResponse(response: NetResponse<T>) {
        }

        override fun onFinish(request: NetRequest<T>) {
        }

        override fun onDownloadProgress(total : Long, download : Long) {
        }
    }
}