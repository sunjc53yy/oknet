package com.jdoit.oknet

import android.app.Application
import android.content.Context
import android.util.Log
import com.jdoit.oknet.converter.gson.NetGsonConverterFactory
import com.jdoit.oknet.converter.protobuf.NetProtobufConverterFactory
import com.jdoit.oknet.worker.okhttp3.OkHttpWorkerFactory

/**
 * @Description:
 * @Date: 2022/3/5 2:24 下午
 * @author : sunjichang
 */
class OkNetTestApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        val ca = HttpsTls()
        ca.rootCaCrtPath = "ca.crt"
        ca.clientCrtPath = "client.bks"
        ca.clientCrtPwd = "123456"
        ca.crtVerifyType = HttpsTls.CERTIFICATION_TWO_WAY
        OkNet.instance.init(this)
            .addConverterFactory(NetGsonConverterFactory.create())
            .addConverterFactory(NetProtobufConverterFactory.create())
            .addWorkerFactory(OkHttpWorkerFactory.create())
            .setBaseUrl("https://10.0.2.2:8085")
            .setHttpCA(ca)
            //.setBaseUrl("https://127.0.0.1:8089")
            .setNetInterceptor(object : INetInterceptor.INetInterceptorAdapter() {
                override fun onInterceptHeader(url: String, header: MutableMap<String, String>) {
                    header["token"] = "abcdefg"
                }

                override fun onInterceptParams(url: String, params: MutableMap<String, Any?>) {
                    params["id"] = "11111"
                }

                override fun <T> onRequestStart(request: NetRequest<T>) {
                    Log.d("TAG", "request start url=${request.getUrl()}")
                }

                override fun <T> onRequestSuccess(request: NetRequest<T>, response: RawResponse) {
                    Log.d("TAG", "request end url=${request.getUrl()}")
                }
            })

    }
}