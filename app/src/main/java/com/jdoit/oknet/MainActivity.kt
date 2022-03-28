package com.jdoit.oknet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.adapter.rxjava3.RxWorkerAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @Description:
 * @Date: 2022/2/27 4:20 下午
 * @author : sunjichang
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_sys_http.setOnClickListener {
            startActivity(Intent(this, BuiltinHttpActivity::class.java))
        }
    }
}