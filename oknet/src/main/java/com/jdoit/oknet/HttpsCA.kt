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

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/7 12:09 上午
 * @Description:
 */
class HttpsCA {
    //客户端要发给服务端的证书路径
    var clientCrtPath : String? = null
    //客户端要验证服务端的证书路径
    var serverCrtPath : String? = null
    //客户端证书的密码
    var clientCrtPwd : String? = null
    //证书验证类型，0--只验证服务端、1--双向验证
    var crtVerifyType : Int = 0
    //要验证的主机名称
    var verifyHostNames : MutableList<String>? = null
}