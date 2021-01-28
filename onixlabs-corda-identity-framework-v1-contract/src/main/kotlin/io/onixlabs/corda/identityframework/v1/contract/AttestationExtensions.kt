/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.v1.contract

import net.corda.core.contracts.ContractState
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
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.amend(
    status: AttestationStatus,
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = U::class.java.cast(state.data.amend(ref, status, pointer, metadata))

/**
 * Amends an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param status The amended status of the attestation.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an amended attestation.
 */
inline fun <reified T : ContractState, reified U : Attestation<T>> StateAndRef<U>.amend(
    status: AttestationStatus,
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amend(status, stateAndRef.toAttestationPointer(), metadata)

/**
 * Accepts an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param pointer The amended pointer of the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an accepted attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.accept(
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = amend(AttestationStatus.ACCEPTED, pointer, metadata)

/**
 * Accepts an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an accepted attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.accept(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amend(AttestationStatus.ACCEPTED, stateAndRef.toAttestationPointer(), metadata)

/**
 * Rejects an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param pointer The amended pointer of the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an rejected attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.reject(
    pointer: AttestationPointer<T> = this.state.data.pointer,
    metadata: Map<String, String> = emptyMap()
): U = amend(AttestationStatus.REJECTED, pointer, metadata)

/**
 * Rejects an attestation.
 *
 * @param T The underlying [ContractState] type.
 * @param U The underlying [Attestation] type.
 * @param stateAndRef The [StateAndRef] from which to amend the attestation.
 * @param metadata Additional information about the attestation.
 * @return Returns an rejected attestation.
 */
inline fun <T : ContractState, reified U : Attestation<T>> StateAndRef<U>.reject(
    stateAndRef: StateAndRef<T>,
    metadata: Map<String, String> = emptyMap()
): U = amend(AttestationStatus.REJECTED, stateAndRef.toAttestationPointer(), metadata)
