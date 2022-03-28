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
package com.jdoit.oknet.utils

import android.util.Log
import com.jdoit.oknet.Headers
import com.jdoit.oknet.NetRequest
import com.jdoit.oknet.OkNet
import com.jdoit.oknet.RawResponse

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/6 11:46 下午
 * @Description:
 */
class NetLogger {
    companion object {
        fun print(msg : String) {
            // Throwable instance must be created before any methods
            val elements = Throwable().fillInStackTrace().stackTrace[2]
            var className = elements.fileName
            val methodName = elements.methodName
            val lineNumber = elements.lineNumber
            if (null == className) {
                className = "OkNet"
            }
            Log.d(className, formatLog(className, methodName, lineNumber, msg))
        }

        private fun formatLog(
            className: String?,
            methodName: String?,
            lineNumber: Int,
            msg: String
        ): String {
            val buffer = StringBuffer()
            .append(className).append(",")
            buffer.append("[").append(methodName).append(",").append(lineNumber)
                .append("] ")
            buffer.append(msg)
            return buffer.toString()
        }

        fun printHttpRequest(request : NetRequest<*>) {
            if (!OkNet.instance.isDebug()) {
                return
            }
            val method = "--> ".plus(request.getMethod()).plus(" ").plus(request.getUrl())
            Log.d(OkNet.TAG, method)
            for(key in request.getHeaders().keys) {
                Log.d(OkNet.TAG, "  ".plus(key).plus(": ").plus(request.getHeader(key)))
            }
            request.getBody()?.let {
                Log.d(OkNet.TAG, "  ".plus(Headers.Key.ContentType).plus(": ").plus(it.getMediaType()))
                Log.d(OkNet.TAG, " ")
                Log.d(OkNet.TAG, "  body: ".plus(it.getParams()))
            }
            Log.d(OkNet.TAG, "--> END ".plus(request.getMethod()))
        }

        fun printHttpResponse(url : String, response: RawResponse) {
            if (!OkNet.instance.isDebug()) {
                return
            }
            Log.d(OkNet.TAG, "<-- ".plus(response.code).plus(" ").plus(url))
            response.getHeaders()?.let {
                for(key in it.keys) {
                    Log.d(OkNet.TAG, "  ".plus(key).plus(": ").plus(it[key]))
                }
            }
            Log.d(OkNet.TAG, " ")
            if (response.content != null) {
                Log.d(OkNet.TAG, "  body: ".plus(String(response.content!!)))

            } else if (null != response.exception) {
                Log.d(OkNet.TAG, "  exception: ".plus(response.exception!!.message))
            }
            Log.d(OkNet.TAG, "<-- END HTTP")
            Log.d(OkNet.TAG, " ")
        }
    }
}