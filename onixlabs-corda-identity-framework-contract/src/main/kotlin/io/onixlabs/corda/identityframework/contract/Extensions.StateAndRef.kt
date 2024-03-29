/*
 * Copyright 2020-2022 ONIXLabs
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

import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.attestations.LinearAttestationPointer
import io.onixlabs.corda.identityframework.contract.attestations.StaticAttestationPointer
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

/**
 * Creates a static attestation pointer from a [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @return Returns a static attestation pointer for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.toStaticAttestationPointer(): StaticAttestationPointer<T> {
    return StaticAttestationPointer(this)
}

/**
 * Creates a linear attestation pointer from a [StateAndRef].
 *
 * @param T The underlying [LinearState] type.
 * @return Returns a linear attestation pointer for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.toLinearAttestationPointer(): LinearAttestationPointer<T> {
    return LinearAttestationPointer(this)
}

/**
 * Creates an attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param status The status of the attestation.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createStaticAttestation(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = Attestation(
    attestor,
    state.data.participants.toSet(),
    toStaticAttestationPointer(),
    status,
    metadata,
    linearId,
    null
)

/**
 * Creates an accepted attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an accepted attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createAcceptedStaticAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = createStaticAttestation(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

/**
 * Creates a rejected attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an rejected attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createRejectedStaticAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = createStaticAttestation(attestor, AttestationStatus.REJECTED, metadata, linearId)

/**
 * Creates an attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param status The status of the attestation.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createLinearAttestation(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = Attestation(
    attestor,
    state.data.participants.toSet(),
    toLinearAttestationPointer(),
    status,
    metadata,
    linearId,
    null
)

/**
 * Creates an accepted attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an accepted attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createAcceptedLinearAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = createLinearAttestation(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

/**
 * Creates a rejected attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an rejected attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createRejectedLinearAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = createLinearAttestation(attestor, AttestationStatus.REJECTED, metadata, linearId)
