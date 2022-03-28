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

import com.jdoit.oknet.utils.NetLogger

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/25 11:45 下午
 * @Description:
 */
class BuiltinHttpWorkerFactory private constructor(): INetWorker.Factory() {
    companion object {
        fun create() : BuiltinHttpWorkerFactory {
            return BuiltinHttpWorkerFactory()
        }
    }

    override fun <T> get(request: NetRequest<T>, okNet: OkNet): INetWorker<T> {
        NetLogger.print("use BuiltinHttpWorker")
        return BuiltinHttpWorker(request, okNet)
    }
}