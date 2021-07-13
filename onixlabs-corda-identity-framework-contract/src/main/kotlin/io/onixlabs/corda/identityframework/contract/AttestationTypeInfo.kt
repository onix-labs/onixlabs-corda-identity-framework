package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.core.toClass
import io.onixlabs.corda.core.toTypedClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Represents the base class for implementing objects that obtain underlying attestation and attestation state types.
 * This is inspired by TypeReference<T>.
 *
 * @param T The underlying attestation type.
 * @property attestationType Obtains the most derived attestation type specified in the type hierarchy.
 * @property attestationStateType Obtains the attestation state type, or null if the value type was specified as a wildcard.
 */
abstract class AttestationTypeInfo<T : Attestation<*>> {

    val attestationType: Class<T>
        get() = getAttestationTypeInternal().toTypedClass()

    val attestationStateType: Class<*>?
        get() = getAttestationStateTypeInternal()?.toClass()

    /**
     * Obtains the most derived claim type specified in the type hierarchy.
     */
    private fun getAttestationTypeInternal(): Type {
        val superClass = javaClass.genericSuperclass
        check(superClass !is Class<*>) { "ClaimTypeInfo constructed without actual type information." }
        return (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Obtains the claim value type, or null if the value type was specified as a wildcard.
     */
    private tailrec fun getAttestationStateTypeInternal(attestationType: Type = getAttestationTypeInternal()): Type? {
        return if (attestationType is ParameterizedType) {
            val argument = attestationType.actualTypeArguments[0]
            if (argument is WildcardType) null else argument
        } else getAttestationStateTypeInternal(attestationType.toClass().genericSuperclass)
    }
}
