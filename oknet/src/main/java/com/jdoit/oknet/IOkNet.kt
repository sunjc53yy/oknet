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

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/13 3:05 下午
 * @Description:
 */
interface IOkNet {
    fun <T> execute(request: NetRequest<T>) : NetResponse<T>
    fun <T, R> adapter(request: NetRequest<T>, adapter: INetWorkerAdapter<T, R>) : R
    fun <T> enqueue(request: NetRequest<T>, callback: INetCallback<T>?)
    fun cancelAll(context: Context)
}
