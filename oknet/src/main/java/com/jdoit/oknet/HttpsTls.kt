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

import androidx.annotation.IntDef

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/7 12:09 上午
 * @Description:
 */
class HttpsTls {
    companion object {
        const val CERTIFICATION_SINGLE = 0 //单项认证
        const val CERTIFICATION_TWO_WAY = 1 //双向认证
    }
    @IntDef(CERTIFICATION_SINGLE, CERTIFICATION_TWO_WAY)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.EXPRESSION)
    annotation class CertType

    //客户端要发给服务端的证书路径
    var clientCrtPath : String? = null
    //客户端证书的密码
    var clientCrtPwd : String? = null
    //根证书路径
    var rootCaCrtPath : String? = null
    //证书验证类型，0--只验证服务端、1--双向验证
    @CertType var crtVerifyType : Int = CERTIFICATION_SINGLE
    //要验证的主机名称
    var verifyHostNames : MutableList<String>? = null
}