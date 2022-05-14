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
package com.jdoit.oknet.converter.gson

import com.google.gson.Gson
import com.jdoit.oknet.INetConverter
import com.jdoit.oknet.RawResponse
import java.lang.reflect.Type

import com.jdoit.oknet.Headers
import com.jdoit.oknet.utils.NetLogger

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/2 11:48 下午
 * @Description:
 */
class NetGsonConverterFactory private constructor(): INetConverter.Factory(){
    private var gson : Gson = Gson()

    companion object {
        fun create() : INetConverter.Factory{
            return NetGsonConverterFactory()
        }
    }

    override fun <T> create(mimeType : String?): INetConverter<T>? {
        mimeType?.let {
            if (it.startsWith(Headers.MediaType.JSON)) {
                return NetGsonConverter(gson)
            }
        }
        return null
    }

    class NetGsonConverter<T>(var gson : Gson) : INetConverter<T> {
        override fun convert(type : Type, response: RawResponse): T? {
            response.content?.let {
                val content = String(it)
                NetLogger.print("use gson converter type=$type content=$content")
                return gson.fromJson<T>(content, type)
            }
            return null
        }

    }
}