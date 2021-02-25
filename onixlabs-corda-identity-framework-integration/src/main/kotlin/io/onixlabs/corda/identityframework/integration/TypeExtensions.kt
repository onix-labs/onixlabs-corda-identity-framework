package io.onixlabs.corda.identityframework.integration

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Converts a type to a class.
 */
fun Type.toClass(): Class<*> = when (this) {
    is ParameterizedType -> rawType.toClass()
    is Class<*> -> this
    else -> Class.forName(typeName)
}

/**
 * Converts a type to a class.
 */
fun Type.toClassOrNull(): Class<*>? = when (this) {
    is ParameterizedType -> rawType.toClass()
    is Class<*> -> this
    else -> null
}
