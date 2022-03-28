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

import android.os.Handler
import android.os.Looper
import com.jdoit.oknet.utils.NetUtils
import java.util.concurrent.*

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/26 12:31 上午
 * @Description:
 */
class NetSync private constructor(){
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NetSync()
        }
    }
    @Volatile
    private var executor : Executor
    private var uiHandler: Handler
    private var keepAliveTime : Long = 60
    init {
        executor = ThreadPoolExecutor(OkNet.instance.getCoreThreadCount(),
            Int.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS,
            SynchronousQueue(), NetUtils.threadFactory("OkNet-Executor", false))
        uiHandler = Handler(Looper.getMainLooper()) {

            return@Handler false
        }
    }

    fun execute(runnable: NetRunnable) {
        executor.execute(runnable)
    }

    fun runOnMain(runnable : Runnable) {
        uiHandler.post(runnable)
    }

    abstract class NetRunnable : Runnable {
        override fun run() {
            execute()
        }

        abstract fun execute()
    }
}