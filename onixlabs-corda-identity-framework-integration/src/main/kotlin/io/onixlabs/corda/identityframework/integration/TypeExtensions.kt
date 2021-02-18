package io.onixlabs.corda.identityframework.integration

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Converts a type to a class.
 */
fun Type.toClass(): Class<*> = when (this) {
    is ParameterizedType -> this.rawType.toClass()
    is Class<*> -> this
    else -> Class.forName(typeName)
}
