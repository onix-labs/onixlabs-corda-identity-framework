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

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty

/**
 * Creates an attestation pointer from a [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @return Returns an attestation pointer for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.toAttestationPointer(): AttestationPointer<T> {
    return if (state.data is LinearState) {
        AttestationPointer(ref, state.data.javaClass, (state.data as LinearState).linearId)
    } else AttestationPointer(ref, state.data.javaClass)
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
fun <T : ContractState> StateAndRef<T>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = Attestation(
    attestor,
    state.data.participants.toSet(),
    toAttestationPointer(),
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
fun <T : ContractState> StateAndRef<T>.accept(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = attest(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

/**
 * Creates a rejected attestation from the specified [StateAndRef].
 *
 * @param T The underlying [ContractState] type.
 * @param attestor The attestor of the witnessed state.
 * @param metadata Additional information about the attestation.
 * @param linearId The unique identifier of the attestation.
 * @return Returns an rejected attestation for the specified [StateAndRef].
 */
fun <T : ContractState> StateAndRef<T>.reject(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
): Attestation<T> = attest(attestor, AttestationStatus.REJECTED, metadata, linearId)

/**
 * Casts a [StateAndRef] of an unknown [ContractState] to a [StateAndRef] of type [T].
 *
 * @param T The underlying [ContractState] type to cast to.
 * @return Returns a [StateAndRef] of type [T].
 * @throws ClassCastException if the unknown [ContractState] type cannot be cast to [T].
 */
inline fun <reified T : ContractState> StateAndRef<*>.cast(): StateAndRef<T> = with(state) {
    StateAndRef(TransactionState(T::class.java.cast(data), contract, notary, encumbrance, constraint), ref)
}
