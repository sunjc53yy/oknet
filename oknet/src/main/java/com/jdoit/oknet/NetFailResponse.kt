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

import java.lang.Exception

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/1/22 1:57 下午
 * @Description:
 */
class NetFailResponse(var msg : String?, cause : Throwable?, var code : Int = 0) : Exception(msg, cause) {
    var consumingTime : Long = 0
    constructor(msg : String?) : this(msg, null)

    companion object {
        fun translate(throwable : Throwable) : NetFailResponse {
            return if (throwable is NetFailResponse) {
                throwable
            } else NetFailResponse("unknown", null, Headers.ResponseCode.UNKNOWN)
        }
    }

    override fun getLocalizedMessage(): String? {
        return super.getLocalizedMessage()
    }
}