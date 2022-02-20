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
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns a static attestation pointer for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.toStaticAttestationPointer(identifier: String? = null): StaticAttestationPointer<T> {
    return StaticAttestationPointer(this, identifier)
}

/**
 * Creates a linear attestation pointer from a [StateAndRef].
 *
 * @param T The underlying [LinearState] type.
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns a linear attestation pointer for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.toLinearAttestationPointer(identifier: String? = state.data.linearId.toString()): LinearAttestationPointer<T> {
    return LinearAttestationPointer(this, identifier)
}

/**
 * Creates an attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param status The status of the attestation.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createStaticAttestation(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = null
): Attestation<T> = Attestation(
    attestor,
    state.data.participants.toSet(),
    toStaticAttestationPointer(identifier),
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
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an accepted attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createAcceptedStaticAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = null
): Attestation<T> = createStaticAttestation(attestor, AttestationStatus.ACCEPTED, metadata, linearId, identifier)

/**
 * Creates a rejected attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an rejected attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.createRejectedStaticAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = null
): Attestation<T> = createStaticAttestation(attestor, AttestationStatus.REJECTED, metadata, linearId, identifier)

/**
 * Creates an attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param status The status of the attestation.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createLinearAttestation(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = state.data.linearId.toString()
): Attestation<T> = Attestation(
    attestor,
    state.data.participants.toSet(),
    toLinearAttestationPointer(identifier),
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
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an accepted attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createAcceptedLinearAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = state.data.linearId.toString()
): Attestation<T> = createLinearAttestation(attestor, AttestationStatus.ACCEPTED, metadata, linearId, identifier)

/**
 * Creates a rejected attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @param identifier Provides an additional, external identifier which can be used to track states across state transitions.
 * @return Returns an rejected attestation for the specified [StateAndRef].
 */
fun <T : LinearState> StateAndRef<T>.createRejectedLinearAttestation(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    identifier: String? = state.data.linearId.toString()
): Attestation<T> = createLinearAttestation(attestor, AttestationStatus.REJECTED, metadata, linearId, identifier)
