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

import com.jdoit.oknet.utils.NetLogger
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/6 11:41 下午
 * @Description:
 */
class NetHelper private constructor(){
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NetHelper()
        }
    }

    private var sslContext : SSLContext? = null
    private var trustManager: Array<TrustManager>? = null

    /**
     * 获取https的证书
     */
    fun getHttpSSLSocketFactory(): SSLSocketFactory? {
        if (null == sslContext) {
            initHttpsCA()
        }
        return sslContext?.socketFactory
    }

    private fun initHttpsCA() {
        val httpsCA = OkNet.instance.getHttpCA() ?: return
        val context = OkNet.instance.getContext() ?: return
        if (null == httpsCA.serverCrtPath) {
            return
        }
        val serverCf: CertificateFactory = CertificateFactory.getInstance("X.509")
        var caInput: InputStream = BufferedInputStream(context.assets.open(httpsCA.serverCrtPath!!))
        val serverCa: X509Certificate = caInput.use {
            serverCf.generateCertificate(it) as X509Certificate
        }
        NetLogger.print("ca=" + serverCa.subjectDN)

        // Create a KeyStore containing our trusted CAs
        val serverKeyStoreType = KeyStore.getDefaultType()
        val serverKeyStore = KeyStore.getInstance(serverKeyStoreType).apply {
            load(null, null)
            setCertificateEntry("trust", serverCa)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val trustAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val serverTrustFactory: TrustManagerFactory = TrustManagerFactory.getInstance(trustAlgorithm).apply {
            init(serverKeyStore)
        }

        // Create a client KeyStore
        var clientKeyFactory : KeyManagerFactory? = null
        if (httpsCA.crtVerifyType == 1 && null != httpsCA.clientCrtPath) {
            val clientKeyStoreType = KeyStore.getDefaultType()
            caInput = BufferedInputStream(context.assets.open(httpsCA.clientCrtPath!!))
            val clientKeyStore = KeyStore.getInstance(clientKeyStoreType).apply {
                httpsCA.clientCrtPwd?.let {
                    load(caInput, it.toCharArray())
                }
            }
            clientKeyFactory = KeyManagerFactory.getInstance("X.509")
            httpsCA.clientCrtPwd?.let {
                clientKeyFactory!!.init(clientKeyStore, it.toCharArray())
            }
        }
        trustManager = serverTrustFactory.trustManagers
        // Create an SSLContext that uses our TrustManager and KeyManager
        sslContext = SSLContext.getInstance("TLS").apply {
            init(clientKeyFactory?.keyManagers, trustManager, null)
        }
    }

    fun getHttpsVerifierHostnameList() : MutableList<String>? {
        val httpsCA = OkNet.instance.getHttpCA() ?: return null
        return if (httpsCA.verifyHostNames != null) {
            httpsCA.verifyHostNames
        } else {
            null
        }
    }

    fun getTrustManager() : X509TrustManager? {
        if (null == trustManager) {
            initHttpsCA()
        }
        trustManager?.let {
            return it[0] as X509TrustManager
        }
        return null
    }
}