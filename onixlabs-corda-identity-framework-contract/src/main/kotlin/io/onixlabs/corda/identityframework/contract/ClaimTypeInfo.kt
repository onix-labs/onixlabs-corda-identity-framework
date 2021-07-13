package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.core.toClass
import io.onixlabs.corda.core.toTypedClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Represents the base class for implementing objects that obtain underlying claim and claim value types.
 * This is inspired by TypeReference<T>.
 *
 * @param T The underlying claim type.
 * @property claimType Obtains the most derived claim type specified in the type hierarchy.
 * @property claimValueType Obtains the claim value type, or null if the value type was specified as a wildcard.
 */
abstract class ClaimTypeInfo<T : AbstractClaim<*>> {

    val claimType: Class<T>
        get() = getClaimTypeInternal().toTypedClass()

    val claimValueType: Class<*>?
        get() = getClaimValueTypeInternal()?.toClass()

    /**
     * Obtains the most derived claim type specified in the type hierarchy.
     */
    private fun getClaimTypeInternal(): Type {
        val superClass = javaClass.genericSuperclass
        check(superClass !is Class<*>) { "ClaimTypeInfo constructed without actual type information." }
        return (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Obtains the claim value type, or null if the value type was specified as a wildcard.
     */
    private tailrec fun getClaimValueTypeInternal(claimType: Type = getClaimTypeInternal()): Type? {
        return if (claimType is ParameterizedType) {
            val argument = claimType.actualTypeArguments[0]
            if (argument is WildcardType) null else argument
        } else getClaimValueTypeInternal(claimType.toClass().genericSuperclass)
    }
}
