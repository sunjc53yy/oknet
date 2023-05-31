package com.jdoit.oknet.demo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import com.jdoit.oknet.Headers
import com.jdoit.oknet.OkNet
import com.jdoit.oknet.adapter.rxjava3.RxWorkerAdapterFactory
import com.jdoit.oknet.body.NetRequestBody
import com.jdoit.oknet.cache.NetCache
import com.jdoit.oknet.pb.PersonOuterClass

import kotlinx.android.synthetic.main.activity_builtin_http.*
import java.io.File

/**
 * @Description:
 * @Date: 2022/3/8 5:13 下午
 * @author : sunjichang
 */
class BuiltinHttpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_builtin_http)
        //表单请求
        btn_sys_form.setOnClickListener {
            val body = NetRequestBody.body()
                .param("name", "张安")
                .param("age", 1)
            val request = OkNet.newListRequest(People::class.java)
                .setUrl("go")
                .setBody(body)
                .setUseBaseParser(true)
                .setCache(NetCache.FAIL_AND_GET_CACHE)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                it.data?.let { list->
                    for (people in list) {
                        Log.d("TAG", "it=${people.name}")
                    }
                }
            }
        }
        //json请求
        btn_sys_json.setOnClickListener {
            val body = NetRequestBody.jsonBody()
                .param("name", "张安")
                .param("age", 1)
            val request = OkNet.newRequest(People::class.java)
                .setUrl("go")
                .setBody(body)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }
        //文件上传
        btn_sys_file.setOnClickListener {
            checkAndSelectPic()
        }
        //get请求
        btn_get_req.setOnClickListener {
            val body = NetRequestBody.body()
                .param("roomId", 1109779767340695552)
                .param("pageNum", 1)
                .param("pageSize", 100)
            val request = OkNet.newRequest(People::class.java)
                .setUrl("live/room/user/list")
                .setBody(body)
                .setMethod(Headers.Method.GET)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }
        //head请求
        btn_head_req.setOnClickListener {
            val request = OkNet.newRequest(People::class.java)
                .setUrl("gohead")
                .setMethod(Headers.Method.HEAD)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }
        //delete请求
        btn_delete_req.setOnClickListener {
            val body = NetRequestBody.jsonBody()
                .param("name", "张安")
                .param("age", 1)
            val request = OkNet.newRequest(People::class.java)
                .setUrl("godelete")
                .setBody(body)
                .setMethod(Headers.Method.DELETE)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }
        //put请求
        btn_put_req.setOnClickListener {
            val body = NetRequestBody.jsonBody()
                .param("name", "张安")
                .param("age", 1)
            val request = OkNet.newRequest(People::class.java)
                .setUrl("goput")
                .setMethod(Headers.Method.PUT)
                .setBody(body)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }
        //pb请求
        btn_pb.setOnClickListener {
            val body = NetRequestBody.pbBody()
            val p = PersonOuterClass.Person.newBuilder().setId(1).setName("张三").build()
            val bytes = p.toByteArray()
            body.setBytes(bytes)
            val request = OkNet.newRequest(PersonOuterClass.Person::class.java)
                .setUrl("gopb")
                .setMethod(Headers.Method.POST)
                .setBody(body)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data?.name}")
            }
        }

        btn_download.setOnClickListener {
            val request = OkNet.newDownloadRequest()
                .setUrl("godownload.jpg")
                .setMethod(Headers.Method.GET)
            request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
                Log.d("TAG", "it=${it.data}")
            }
        }
    }

    private fun upload(filePath : String) {
        val m = NetRequestBody.MultipleEntity.build {
            file = File(filePath)
            fileName = "test.jpg"
            mimeType = "image/jpeg"
            key = "file"
        }
        val body = NetRequestBody.fileBody()
            .param("name", "张安")
            .param("age", 1)
            .multiple(m)
        val request = OkNet.newRequest(People::class.java)
            .setUrl("gofile")
            .setBody(body)
            .setTarget(this)
        request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
            Log.d("TAG", "it=${it.data?.name}")
        }
    }

    private fun checkAndSelectPic() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
            return
        }
        selectPic();
    }

    private fun selectPic() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            // 得到图片的全路径
            val uri = data!!.data
            val filePathColumn = arrayOf(
                MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
            )
            val cursor = uri?.let {
                contentResolver.query(
                    it,
                    filePathColumn, null, null, null
                )
            } //从系统表中查询指定Uri对应的照片

            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            val path = columnIndex?.let { cursor.getString(it) } //获取照片路径
            Log.d("TAG", "$uri, path=$path")
            path?.let {
                upload(it)
            }
        } else if (requestCode == 1001) {
            selectPic()
        }
    }
}