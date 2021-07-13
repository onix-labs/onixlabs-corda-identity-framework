package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.identityframework.contract.*
import io.onixlabs.corda.identityframework.contract.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema.CordaClaimEntity
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

/**
 * Adds a vault query expression to filter by claim issuer equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimIssuer(value: AbstractParty) {
    expression(CordaClaimEntity::issuer equalTo value)
}

/**
 * Adds a vault query expression to filter by claim holder equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimHolder(value: AbstractParty) {
    expression(CordaClaimEntity::holder equalTo value)
}

/**
 * Adds a vault query expression to filter by claim property equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimProperty(value: String) {
    expression(CordaClaimEntity::property equalTo value)
}

/**
 * Adds a vault query expression to filter by claim value equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimValue(value: Any) {
    expression(CordaClaimEntity::value equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by claim value type equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimValueType(value: Class<*>) {
    expression(CordaClaimEntity::valueType equalTo value.canonicalName)
}

/**
 * Adds a vault query expression to filter by claim value type equal to the specified value.
 */
@QueryDslContext
inline fun <reified T : Any> QueryDsl<out CordaClaim<*>>.claimValueType() {
    claimValueType(T::class.java)
}

/**
 * Adds a vault query expression to filter by claim previous state reference equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimPreviousStateRef(value: StateRef?) {
    if (value == null) expression(CordaClaimEntity::previousStateRef.isNull())
    else expression(CordaClaimEntity::previousStateRef equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by claim is self issued equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimIsSelfIssued(value: Boolean = true) {
    expression(CordaClaimEntity::isSelfIssued equalTo value)
}

/**
 * Adds a vault query expression to filter by claim hash equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimHash(value: SecureHash) {
    expression(CordaClaimEntity::hash equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by claim type equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : CordaClaim<*>> QueryDsl<in T>.claimType(value: Class<in T>) {
    expression(CordaClaimEntity::claimType equalTo value.canonicalName)
}

/**
 * Adds a vault query expression to filter by claim type equal to the specified value.
 * This will also filter by the claim value type unless it's specified as a wildcard.
 */
@QueryDslContext
inline fun <reified T : CordaClaim<*>> QueryDsl<in T>.claimType() {
    with(object : ClaimTypeInfo<T>() {}) {
        claimType(claimType)
        claimValueType?.let { expression(CordaClaimEntity::valueType equalTo it.canonicalName) }
    }
}

/**
 * Adds a vault query expression to filter by attestation attestor equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationAttestor(value: AbstractParty) {
    expression(AttestationEntity::attestor equalTo value)
}

/**
 * Adds a vault query expression to filter by attestation pointer equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationPointer(value: Any) {
    expression(AttestationEntity::pointer equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by attestation pointer type equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationPointerType(value: Class<out ContractState>) {
    expression(AttestationEntity::pointerStateType equalTo value.canonicalName)
}

/**
 * Adds a vault query expression to filter by attestation pointer hash equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationPointerHash(value: SecureHash) {
    expression(AttestationEntity::pointerHash equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by attestation status equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationStatus(value: AttestationStatus) {
    expression(AttestationEntity::status equalTo value)
}

/**
 * Adds a vault query expression to filter by attestation previous state reference equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationPreviousStateRef(value: StateRef?) {
    if (value == null) expression(AttestationEntity::previousStateRef.isNull())
    else expression(AttestationEntity::previousStateRef equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by attestation hash equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationHash(value: SecureHash) {
    expression(AttestationEntity::hash equalTo value.toString())
}

/**
 * Adds a vault query expression to filter by attestation type equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun <T : Attestation<*>> QueryDsl<in T>.attestationType(value: Class<in T>) {
    expression(AttestationEntity::attestationType equalTo value.canonicalName)
}

/**
 * Adds a vault query expression to filter by attestation type equal to the specified value.
 * This will also filter by the attestation pointer type unless it's specified as a wildcard.
 */
@QueryDslContext
inline fun <reified T : Attestation<*>> QueryDsl<in T>.attestationType() {
    with(object : AttestationTypeInfo<T>() {}) {
        attestationType(attestationType)
        attestationStateType?.let { expression(AttestationEntity::pointerStateType equalTo it.canonicalName) }
    }
}
