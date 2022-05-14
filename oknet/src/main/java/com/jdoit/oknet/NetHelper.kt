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
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.random.Random

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
        val httpsTls = OkNet.instance.getHttpCA() ?: return
        val context = OkNet.instance.getContext() ?: return
        if (null != httpsTls.rootCaCrtPath) {
            val rootCaCf: CertificateFactory = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = BufferedInputStream(context.assets.open(httpsTls.rootCaCrtPath!!))
            val rootCa: X509Certificate = caInput.use {
                rootCaCf.generateCertificate(it) as X509Certificate
            }
            NetLogger.print("ca=" + rootCa.subjectDN)

            // Create a KeyStore containing our trusted CAs
            val rootCaKeyStoreType = KeyStore.getDefaultType()
            val rootCaKeyStore = KeyStore.getInstance(rootCaKeyStoreType).apply {
                load(null, null)
                setCertificateEntry("ca", rootCa)
            }

            // Create a TrustManager that trusts the CAs inputStream our KeyStore
            val trustAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val caTrustFactory: TrustManagerFactory = TrustManagerFactory.getInstance(trustAlgorithm).apply {
                init(rootCaKeyStore)
            }
            trustManager = caTrustFactory.trustManagers
        }

        // Create a client KeyStore
        var clientKeyFactory : KeyManagerFactory? = null
        if (httpsTls.crtVerifyType == HttpsTls.CERTIFICATION_TWO_WAY && null != httpsTls.clientCrtPath) {
            val clientKeyStoreType = "BKS"   // "BKS" KeyStore.getDefaultType()
            val clientCaInput = BufferedInputStream(context.assets.open(httpsTls.clientCrtPath!!))
            val clientKeyStore = KeyStore.getInstance(clientKeyStoreType).apply {
                httpsTls.clientCrtPwd?.let {
                    load(clientCaInput, it.toCharArray())
                }
            }
            clientKeyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            httpsTls.clientCrtPwd?.let {
                clientKeyFactory!!.init(clientKeyStore, it.toCharArray())
            }
        }
        // Create an SSLContext that uses our TrustManager and KeyManager
        NetLogger.print("clientKeyFactory is null = "+ (clientKeyFactory == null))
        sslContext = SSLContext.getInstance("TLS").apply {
            init(clientKeyFactory?.keyManagers, trustManager, SecureRandom())
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

    private fun trustAllManager() : Array<TrustManager> {
        return arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {

            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
                if (chain == null) {
                    throw IllegalArgumentException("checkServerTrusted:x509Certificate array isnull")
                }
                if (chain.isEmpty()) {
                    throw IllegalArgumentException("checkServerTrusted: X509Certificate is empty")
                }
                if (!(null != authType && authType.equals("RSA", true))) {
                    throw CertificateException("checkServerTrusted: AuthType is not RSA")
                }
                try {
                    val tmf = TrustManagerFactory.getInstance("X509").apply {
                        val keyStore : KeyStore? = null
                        init(keyStore)
                    }
                    for (trustManager in tmf.trustManagers) {
                        (trustManager as X509TrustManager).checkServerTrusted(chain, authType);
                    }
                } catch (e : Exception) {
                    throw CertificateException(e);
                }
//                val pubkey = chain[0].publicKey as RSAPublicKey
//                val encoded = BigInteger(1 , pubkey.encoded).toString(16)
//                val expected = PUB_KEY.equalsIgnoreCase(encoded);
//                if (!expected) {
//                    throw CertificateException("checkServerTrusted: Expected public key: "
//                            + PUB_KEY + ", got public key:" + encoded);
//                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
    }
}