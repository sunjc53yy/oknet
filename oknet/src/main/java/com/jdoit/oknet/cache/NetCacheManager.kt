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

import android.content.Context
import android.util.Base64
import com.jdoit.oknet.Headers
import com.jdoit.oknet.utils.NetLogger
import com.jdoit.oknet.NetRequest
import com.jdoit.oknet.NetSync
import com.jdoit.oknet.OkNet
import java.io.File
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and


/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/3/11 12:23 下午
 * @Description:
 */
object NetCacheManager {
    private const val SPLIT = "@"
    private const val VERSION : Byte = 1
    private const val CACHE_PATH = "oknet"
    private const val sKey = "abcdef0123456789"
    private const val ivParameter = "0123456789abcdef"

    fun cache(data : ByteArray?, mimeType : String?, request: NetRequest<*>?){
        NetSync.instance.execute(object : NetSync.NetRunnable() {
            override fun execute() {
                cacheInner(data, mimeType, request)
            }
        })
    }

    private fun cacheInner(data : ByteArray?, mimeType : String?, request: NetRequest<*>?){
        try {
            if (request == null || !request.getCache().allowCache() || null == mimeType) {
                return
            }
            if (request.getMethod() != Headers.Method.GET &&
                request.getMethod() != Headers.Method.POST) {
                return
            }
            val context = OkNet.instance.getContext() ?: return
            val cache = request.getCache()
            data?.let {
                val key = generateCacheKey(request) ?: return
                val fileKey = encrypt(key)?.plus(".dat") ?: return
                NetLogger.print("写入缓存 $key $fileKey")
                val content = encodeData(it, mimeType, cache.getExpireTime(), key) ?: return
                val path = getCachePath(context)
                val saveFile = File(path, fileKey)
                val out = saveFile.outputStream()
                out.write(content)
                out.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun read(request: NetRequest<*>?) : NetCacheBody? {
        try {
            val context = OkNet.instance.getContext() ?: return null
            val path = getCachePath(context)
            val key = generateCacheKey(request) ?: return null
            val fileKey = encrypt(key)?.plus(".dat") ?: return null
            NetLogger.print("加载缓存开始 $key $fileKey")
            val saveFile = File(path, fileKey)
            if (!saveFile.exists()) {
                return null
            }
            val inStream = saveFile.inputStream()
            val content = decodeData(inStream.readBytes())
            inStream.close()
            NetLogger.print("加载缓存结束 $key")
            return content
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun decodeData(data : ByteArray) : NetCacheBody? {
        try {
            if (data.isEmpty()) {
                return null
            }
            //版本(1byte)+内容长度(4byte)+内容+内容类型+@+到期时间+@+url链接拼接参数组成的key
            var index = 0
            //版本
            var v = data[0]
            index ++
            //内容长度
            val contentLenBytes = ByteArray(4)
            System.arraycopy(data, index, contentLenBytes, 0, contentLenBytes.size)
            index += contentLenBytes.size
            val contentLen = ByteBuffer.wrap(contentLenBytes).int
            //内容
            val content = ByteArray(contentLen)
            System.arraycopy(data, index, content, 0, content.size)
            index += content.size
            //内容类型+@+到期时间+@+key
            val otherLen = data.size - contentLen - contentLenBytes.size - 1
            val otherBytes = ByteArray(otherLen)
            System.arraycopy(data, index, otherBytes, 0, otherBytes.size)
            val other = String(otherBytes)
            val arr = other.split(SPLIT)
            if (arr.size != 3) {
                return null
            }
            val mimeType = arr[0]
            val expireTime = arr[1].toLong()
            val key = arr[2]
            if (expireTime < System.currentTimeMillis()) {
                return null
            }
            return NetCacheBody(content, mimeType)
        } catch (e: Exception) {
        }
        return null
    }

    private fun encodeData(data : ByteArray, mimeType : String, expireTime : Long, key : String) : ByteArray? {
        try {
            val contentLen : Int = data.size
            val contentLenBytes = int2bytes(contentLen)

            val expireTimeBytes = expireTime.toString().toByteArray()
            val mimeTypeBytes = mimeType.toByteArray()
            val splitBytes = SPLIT.toByteArray()
            val keyBytes = key.toByteArray()

            //版本(1byte)+内容长度(4byte)+内容+内容类型+@+到期时间+@++url链接拼接参数组成的key
            val length = 1 + contentLenBytes.size + contentLen + mimeTypeBytes.size + splitBytes.size +
                    expireTimeBytes.size + splitBytes.size + keyBytes.size
            val result = ByteArray(length)
            var index = 0
            //版本
            result[index] = VERSION
            index ++
            //内容长度
            System.arraycopy(contentLenBytes, 0, result, index, contentLenBytes.size)
            index += contentLenBytes.size
            //内容
            System.arraycopy(data, 0, result, index, contentLen)
            index += contentLen
            //内容类型
            System.arraycopy(mimeTypeBytes, 0, result, index, mimeTypeBytes.size)
            index += mimeTypeBytes.size
            //分隔符号
            System.arraycopy(splitBytes, 0, result, index, splitBytes.size)
            index += splitBytes.size
            //到期时间
            System.arraycopy(expireTimeBytes, 0, result, index, expireTimeBytes.size)
            index += expireTimeBytes.size
            //分隔符号
            System.arraycopy(splitBytes, 0, result, index, splitBytes.size)
            index += splitBytes.size
            //key
            System.arraycopy(keyBytes, 0, result, index, keyBytes.size)
            return result
        } catch (e: Exception) {
        }
        return null
    }


    private fun int2bytes(value : Int) : ByteArray {
        val byteArray = ByteArray(4)
        val highH = ((value shr 24) and 0xff).toByte()
        val highL = ((value shr 16) and 0xff).toByte()
        val lowH = ((value shr 8) and 0xff).toByte()
        val lowL = (value and 0xff).toByte()
        byteArray[0] = highH
        byteArray[1] = highL
        byteArray[2] = lowH
        byteArray[3] = lowL
        return byteArray
    }

    private fun getCachePath(context : Context) : File {
        val path = File(context.cacheDir, CACHE_PATH)
        if (!path.exists()) {
            path.mkdirs()
        }
        return path
    }

    private fun generateCacheKey(request: NetRequest<*>?): String? {
        if (null == request) {
            return null
        }
        val body = request.getBody()
        val url = StringBuilder(request.getUrl())
        if (null != body) {
            val sb = StringBuilder()
            val paramMap = body.getParams()
            for ((index, key) in paramMap.keys.withIndex()) {
                sb.append(key).append("=").append(paramMap[key])
                if (index < paramMap.keys.size - 1) {
                    sb.append("&")
                }
            }
            if (sb.isNotEmpty()) {
                if (sb.toString().endsWith("?")) {
                    url.append("&")
                } else {
                    url.append("?")
                }
                url.append(sb.toString())
            }
        }
        url.append("##").append(request.getMethod())
        return url.toString()
    }

    private fun encrypt(text: String): String? {
        try {
            try {
                //获取md5加密对象
                val instance: MessageDigest = MessageDigest.getInstance("MD5")
                //对字符串加密，返回字节数组
                val digest:ByteArray = instance.digest(text.toByteArray())
                var sb : StringBuffer = StringBuffer()
                for (b in digest) {
                    //获取低八位有效值
                    var i :Int = b.toInt() and 0xff
                    //将整数转化为16进制
                    var hexString = Integer.toHexString(i)
                    if (hexString.length < 2) {
                        //如果是一位的话，补0
                        hexString = "0$hexString"
                    }
                    sb.append(hexString)
                }
                return sb.toString()

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
}