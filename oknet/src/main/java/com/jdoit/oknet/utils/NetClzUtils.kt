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
package com.jdoit.oknet.utils

import java.lang.IllegalArgumentException
import java.lang.reflect.*
import java.lang.reflect.Array

/**
 * @Author: sunjichang (https://github.com/sunjc53yy)
 * @Project: https://github.com/sunjc53yy/oknet.git
 * @Date: 2022/2/27 11:39 下午
 * @Description:
 */
class NetClzUtils {
    companion object {
        fun getRawType(type: Type?): Class<*>? {
            type?.let {
                return if (it is Class<*>) {
                    it
                } else if (it is ParameterizedType) {
                    val rawType = it.rawType
                    if (rawType !is Class<*>) {
                        throw IllegalArgumentException()
                    } else {
                        rawType
                    }
                } else if (it is GenericArrayType) {
                    val componentType = it.genericComponentType
                    Array.newInstance(getRawType(componentType), 0).javaClass
                } else if (it is TypeVariable<*>) {
                    Any::class.java
                } else if (it is WildcardType) {
                    getRawType(it.upperBounds[0])
                } else {
                    throw IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + type.javaClass.name)
                }
            }
            return null
        }

        fun getParameterUpperBound(index: Int, type: ParameterizedType?): Type? {
            type?.let {
                val types = it.actualTypeArguments
                return if (index >= 0 && index < types.size) {
                    val paramType = types[index]
                    if (paramType is WildcardType) paramType.upperBounds[0] else paramType
                } else {
                    throw IllegalArgumentException("Index " + index + " not in range [0," + types.size + ") for " + type)
                }
            }
            return null
        }

        fun generateType(cls : Class<*>, actualType : Type) : ParameterizedType {
            return object : ParameterizedType {
                override fun getActualTypeArguments(): kotlin.Array<Type> {
                    return arrayOf(actualType)
                }

                override fun getRawType(): Type {
                    return cls
                }

                override fun getOwnerType(): Type? {
                    return null
                }
            }
        }

        fun isBaseType(cls : Class<*>?) : Boolean{
            return cls == String::class.java ||
                    cls == Int::class.java ||
                    cls == IntArray::class.java ||
                    cls == Float::class.java ||
                    cls == FloatArray::class.java ||
                    cls == Long::class.java ||
                    cls == LongArray::class.java ||
                    cls == Char::class.java ||
                    cls == CharArray::class.java ||
                    cls == Boolean::class.java ||
                    cls == BooleanArray::class.java
        }
    }
}