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
 * @Date: 2022/3/20 9:31 下午
 * @Description: 内置解析base model
 */
class BuiltinBaseModel<T> : INetBaseModel<T> {
    private var code : Int = 0
    private var message : String? = null
    private var data : T? = null

    override fun getCode(): Int {
        return code
    }

    override fun getMessage(): String? {
        return message
    }

    override fun getData(): T? {
        return data
    }

    fun setCode(code : Int) {
        this.code = code
    }

    fun setMessage(message : String?) {
        this.message = message
    }

    fun setData(data : T?) {
        this.data = data
    }
}