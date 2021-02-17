package io.onixlabs.corda.identityframework.integration

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Represents a reference to an underlying type.
 * This generic abstract class is derived from the TypeReference class is jackson fasterxml.
 *
 * @param T The underlying type.
 * @property type The underlying type.
 * @property typeName The underlying type name.
 * @property arguments The generic arguments of the underlying type.
 */
@PublishedApi
internal abstract class TypeReference<T> : Comparable<T> {

    val type: Type = getGenericType()
    val typeName: String = type.typeName
    val arguments: List<Type> = getGenericTypeArguments()

    /**
     * Gets an underlying type.
     */
    private fun getGenericType(): Type {
        val superClass = javaClass.genericSuperclass

        if (superClass is Class<*>) {
            throw IllegalArgumentException("TypeReference constructed without actual type information.")
        }

        return (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Gets a list of generic type arguments for the generic type.
     */
    private fun getGenericTypeArguments(): List<Type> {
        return if (type is ParameterizedType) {
            type.actualTypeArguments.toList()
        } else emptyList()
    }

    /**
     * The only reason we define this method (and require implementation of [Comparable])
     * is to prevent constructing a reference without type information.
     */
    override fun compareTo(other: T): Int = 0
}
