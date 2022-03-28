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
package com.jdoit.oknet.converter.protobuf

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.jdoit.oknet.Headers
import com.jdoit.oknet.INetConverter
import com.jdoit.oknet.RawResponse
import com.jdoit.oknet.utils.NetLogger
import java.lang.reflect.Type

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/21 8:01 下午
 * @Description:
 */
class NetProtobufConverterFactory : INetConverter.Factory() {

    companion object {
        fun create() : INetConverter.Factory{
            return NetProtobufConverterFactory()
        }
    }

    override fun <T> create(mimeType: String?): INetConverter<T>? {
        mimeType?.let {
            if (it.startsWith(Headers.MediaType.STREAM)) {
                return NetProtobufConverter()
            }
        }
        return null
    }

    inner class NetProtobufConverter<T> : INetConverter<T> {
        override fun convert(type: Type, response: RawResponse): T? {
            NetLogger.print("use protobuf converter type=$type ")
            if (null == response.content) {
                return null
            }
            val c = type as Class<*>
            if (!MessageLite::class.java.isAssignableFrom(c)) {
                return null
            }
            var parser : Parser<MessageLite>? = null
            try {
                val method = c.getDeclaredMethod("parser")
                parser = method.invoke(null) as Parser<MessageLite>
            } catch (e: Exception) {
                try {
                    val field = c.getDeclaredField("PARSER")
                    field.isAccessible = true
                    parser = field.get(null) as Parser<MessageLite>
                } catch (e: Exception) {
                }
            }
            if (null == parser) {
                return null
            }
            try {
                return parser.parseFrom(response.content) as T
            } catch (e: Exception) {
            }
            return null
        }
    }
}