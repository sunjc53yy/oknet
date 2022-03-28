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
package com.jdoit.oknet.cache

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/11 12:21 下午
 * @Description:
 */
class NetCache private constructor(var model : Int, var maxAge : Long = 24 * 60 * 60) {

    companion object {

        fun cache(model : Int, maxAge : Long = 24 * 60 * 60) : NetCache {
            return NetCache(model, maxAge)
        }

        //无缓存
        val NONE_CACHE = NetCache(0)

        val PRIORITY_CACHE = NetCache(1, Long.MAX_VALUE)

        val FAIL_AND_GET_CACHE = NetCache(2, Long.MAX_VALUE)
    }

    fun getExpireTime() : Long {
        return if (maxAge == Long.MAX_VALUE) maxAge else System.currentTimeMillis() + maxAge * 1000
    }

    fun allowCache() : Boolean {
        return model == 1 || model == 2
    }

    fun isPriorityModel() : Boolean {
        return model == 1
    }

    fun isFailGetModel() : Boolean {
        return model == 2
    }
}