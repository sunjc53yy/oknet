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

import com.jdoit.oknet.utils.NetClzUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 2:21 下午
 * @Description:
 */
interface INetWorkerAdapter<T, R> {
    fun adapter(worker : INetWorker<T>) : R

    abstract class Factory{
        abstract fun get(returnType : Type, annotations : Array<Annotation>) : INetWorkerAdapter<Any, Any>?

        protected open fun getParameterUpperBound(index: Int, type: ParameterizedType?): Type? {
            return NetClzUtils.getParameterUpperBound(index, type)
        }

        protected open fun getRawType(type: Type?): Class<*>? {
            return NetClzUtils.getRawType(type)
        }
    }
}