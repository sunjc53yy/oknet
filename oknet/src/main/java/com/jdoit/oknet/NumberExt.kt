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
 * @Date: 2022/3/11 12:38 下午
 * @Description:
 */
class NumberExt {
    val Int.bytes : ByteArray
        get() {
            val byteArray = ByteArray(4)
            val highH = ((this shr 24) and 0xff).toByte()
            val highL = ((this shr 16) and 0xff).toByte()
            val lowH = ((this shr 8) and 0xff).toByte()
            val lowL = (this and 0xff).toByte()
            byteArray[0] = highH
            byteArray[1] = highL
            byteArray[2] = lowH
            byteArray[3] = lowL
            return byteArray
        }
}