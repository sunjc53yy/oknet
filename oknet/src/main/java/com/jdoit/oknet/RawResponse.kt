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
 * @Date: 2022/2/26 6:13 下午
 * @Description:
 */
class RawResponse {
    var code : Int = 0
    var content : ByteArray? = null
    var error : ByteArray? = null
    var contentLength : Long = 0
    var exception : java.lang.Exception? = null
    var success = false
    var mimeType : String? = null
    var cache : Boolean = false
    var consumingTime : Long = 0
    private var headers : MutableMap<String, String>? = null

    fun setHeaders(headers: Map<String, List<String>>) {
        val map = mutableMapOf<String, String>()
        var list : List<String>?
        for (key in headers.keys) {
            list = headers[key]
            list.let { l->
                map[key] = l.toString()
            }
        }
        this.headers = map
    }

    fun getHeaders() : MutableMap<String, String>? {
        return headers
    }
}