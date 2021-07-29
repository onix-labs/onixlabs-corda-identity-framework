package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.claims.ClaimTypeInfo
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimSchema.CordaClaimEntity
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
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
 * Adds a vault query expression to filter by claim issuer account equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimIssuerAccount(value: Account) {
    claimIssuer(value.toAccountParty())
}

/**
 * Adds a vault query expression to filter by claim issuer account ID equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimIssuerAccountId(value: UniqueIdentifier) {
    expression(CordaClaimEntity::issuerAccountLinearId equalTo value.id)
    if (value.externalId == null) expression(CordaClaimEntity::issuerAccountExternalId.isNull())
    else expression(CordaClaimEntity::issuerAccountExternalId equalTo value.externalId)
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
 * Adds a vault query expression to filter by claim holder account equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimHolderAccount(value: Account) {
    claimHolder(value.toAccountParty())
}

/**
 * Adds a vault query expression to filter by claim holder account ID equal to the specified value.
 *
 * @param value The value to filter by in the vault query expression.
 */
@QueryDslContext
fun QueryDsl<out CordaClaim<*>>.claimHolderAccountId(value: UniqueIdentifier) {
    expression(CordaClaimEntity::holderAccountLinearId equalTo value.id)
    if (value.externalId == null) expression(CordaClaimEntity::holderAccountExternalId.isNull())
    else expression(CordaClaimEntity::holderAccountExternalId equalTo value.externalId)
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
