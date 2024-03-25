package com.jdoit.oknet.demo

import android.app.Application
import android.content.Context
import android.util.Log
import com.jdoit.oknet.*
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
//            .setBaseUrl("https://test.so-sounds.com/portal-api")
            .setHttpCA(ca)
            .setBaseParserModel(BaseModel::class.java)
            .setBaseUrl("https://127.0.0.1:8085")
            .setNetInterceptor(object : INetInterceptor.INetInterceptorAdapter() {
                override fun onInterceptHeader(url: String, header: MutableMap<String, String>) {
                    header["token"] = "abcdefg"
                    header["Authorization"] = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInppcCI6IkRFRiJ9.eNokytEKgzAMQNF_ybMBU22T-TepbcEhOhYLg7F_X8Ze77lvuF8bLKC8xlgyYUw04txyQ23zDaeiykl0zVxhgM3MZzvt7EcxD9azh33HbvX5G_SChZIkjiQcPBR3Gp3q6_EnmSRw-HwBAAD__w.ceB4Xe9YlpaF1SbwjF5OMnHHPdy56Wzpg35ylnoNWwc"
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