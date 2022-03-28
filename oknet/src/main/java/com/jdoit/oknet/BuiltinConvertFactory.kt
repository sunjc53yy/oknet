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
import com.jdoit.oknet.utils.NetLogger
import java.lang.reflect.Type

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/3 8:52 上午
 * @Description:
 */
class BuiltinConvertFactory : INetConverter.Factory() {
    companion object {
        fun create() : INetConverter.Factory{
            return BuiltinConvertFactory()
        }
    }
    override fun <T> create(mimeType: String?): INetConverter<T>? {
        return BuiltinConvert()
    }

    class BuiltinConvert<T> : INetConverter<T> {
        override fun convert(type: Type, response: RawResponse): T? {
            try {
                NetLogger.print("use BuiltinConvert type=$type")
                val cls = NetClzUtils.getRawType(type)
                val baseType = NetClzUtils.isBaseType(cls)
                response.content?.let {
                    val content = String(it)
                    return if (baseType) content as T else null
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }

    }
}