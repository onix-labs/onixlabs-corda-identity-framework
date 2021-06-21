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

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef

/**
 * Amends an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param status The amended status of the attestation.
 * @param pointer The amended pointer of the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an amended attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.amendAttestation(
    status: AttestationStatus,
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = U::class.java.cast(state.data.amend(ref, status, pointer, metadata))

/**
 * Amends an attestation of a [ContractState].
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param status The amended status of the attestation.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an amended attestation.
 */
inline fun <reified T : ContractState, reified U : Attestation<T>> StateAndRef<U>.amendStaticAttestation(
    status: AttestationStatus,
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(status, stateAndRef.toStaticAttestationPointer(), metadata)

/**
 * Amends an attestation of a [LinearState].
 *
 * @param T The underlying [LinearState] type.
 * @param U The underlying [Attestation] type.
 * @param status The amended status of the attestation.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an amended attestation.
 */
inline fun <reified T : LinearState, reified U : Attestation<T>> StateAndRef<U>.amendLinearAttestation(
    status: AttestationStatus,
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(status, stateAndRef.toLinearAttestationPointer(), metadata)

/**
 * Accepts an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param pointer The amended pointer of the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an accepted attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.acceptAttestation(
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.ACCEPTED, pointer, metadata)

/**
 * Accepts an attestation of a [ContractState].
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an accepted attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.acceptStaticAttestation(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.ACCEPTED, stateAndRef.toStaticAttestationPointer(), metadata)

/**
 * Accepts an attestation of a [LinearState].
 *
 * @param T The underlying [LinearState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an accepted attestation.
 */
inline fun <T : LinearState, reified U : Attestation<T>> StateAndRef<U>.acceptLinearAttestation(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.ACCEPTED, stateAndRef.toLinearAttestationPointer(), metadata)

/**
 * Rejects an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param pointer The amended pointer of the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an rejected attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.rejectAttestation(
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.REJECTED, pointer, metadata)

/**
 * Rejects an attestation of a [ContractState].
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an rejected attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.rejectStaticAttestation(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.REJECTED, stateAndRef.toStaticAttestationPointer(), metadata)

/**
 * Rejects an attestation of a [LinearState].
 *
 * @param T The underlying [LinearState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an rejected attestation.
 */
inline fun <T : LinearState, reified U : Attestation<T>> StateAndRef<U>.rejectLinearAttestation(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amendAttestation(AttestationStatus.REJECTED, stateAndRef.toLinearAttestationPointer(), metadata)
