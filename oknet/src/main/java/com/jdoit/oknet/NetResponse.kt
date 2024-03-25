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
 * @Date: 2022/2/12 3:48 下午
 * @Description:
 */
class NetResponse <T>{
    var rawResponse : RawResponse? = null
    var exception : NetFailResponse? = null
    var request: NetRequest<T>? = null
    var data : T? = null
    var cache : Boolean = false
    private var extMap: MutableMap<String, Any?>? = null

    fun putExtData(ext: MutableMap<String, Any?>) {
        if (null == extMap) {
            extMap = mutableMapOf()
        }
        extMap?.putAll(ext)
    }
    fun putExtData(key: String, value: Any?) {
        if (null == extMap) {
            extMap = mutableMapOf()
        }
        extMap?.put(key, value)
    }

    fun getExtData(key: String): Any? {
        return extMap?.get(key)
    }

    fun setExtData(model: INetBaseModel<T>) {
        putExtData("businessCode", model.getBusinessCode())
        putExtData("businessMessage", model.getBusinessMessage())
        model.getExt()?.let { et->
            putExtData(et)
        }
    }
}