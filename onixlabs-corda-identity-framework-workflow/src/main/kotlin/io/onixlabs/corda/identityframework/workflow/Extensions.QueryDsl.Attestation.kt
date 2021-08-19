/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.attestations.AttestationTypeInfo
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

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
