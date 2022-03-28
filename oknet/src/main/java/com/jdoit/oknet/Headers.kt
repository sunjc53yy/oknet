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
 * @Date: 2022/2/6 9:39 下午
 * @Description:
 */
class Headers {
    object C {
        const val UPLOAD_BOUNDARY = "***android***oknet***"
        const val UPLOAD_LINE_END = "\r\n"
        const val UPLOAD_PREFIX = "--"
    }
    object Key {
        const val ContentType : String = "Content-Type"
    }
    object Method {
        const val GET : String = "GET"
        const val POST : String = "POST"
        const val PUT : String = "PUT"
        const val DELETE : String = "DELETE"
        const val HEAD : String = "HEAD"
        const val PATCH : String = "PATCH"
    }
    object MediaType {
        const val JSON = "application/json"
        const val FORM = "application/x-www-form-urlencoded"
        const val STREAM = "application/octet-stream"
        const val XML = "application/xml"
        const val FILE = "multipart/form-data"
    }
    object ResponseCode {
        const val USE_CACHE = 99999
        const val PARSER_FAIL = 99998
        const val UNKNOWN = -9999
    }

    object ResponseMsg {
        const val PARSER_FAIL = "parser fail"
    }
}