package com.jdoit.oknet.demo

import com.jdoit.oknet.INetBaseModel

class BaseModel<T>(var code : Int = 0, var msg : String? = null, var data : T? = null) : INetBaseModel<T> {
    override fun getBusinessCode(): Int {
        return code
    }

    override fun getBusinessMessage(): String? {
        return msg
    }

    override fun getBusinessData(): T? {
        return data
    }

    override fun getExt(): MutableMap<String, Any?>? {
        return null
    }

}