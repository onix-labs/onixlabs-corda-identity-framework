package io.onixlabs.corda.identityframework.workflow

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal abstract class TypeReference<T> : Comparable<T> {

    val type: Type = getGenericType()
    val typeName: String = type.typeName
    val arguments: List<Type> = getGenericTypeArguments()

    fun toClass(): Class<T> {
        return Class.forName(typeName) as Class<T>
    }

    private fun getGenericType(): Type {
        val superClass = javaClass.genericSuperclass

        if (superClass is Class<*>) {
            throw IllegalArgumentException("TypeReference constructed without actual type information.")
        }

        return (superClass as ParameterizedType).actualTypeArguments[0]
    }

    private fun getGenericTypeArguments(): List<Type> {
        return if (type is ParameterizedType) {
            type.actualTypeArguments.toList()
        } else emptyList()
    }

    override fun compareTo(other: T): Int = 0
}
