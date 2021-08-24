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

import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.core.contracts.StateAndRef

/**
 * Adds an attestation for issuance to a transaction builder, including the required command.
 *
 * @param state The attestation state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addIssuedAttestation(state: Attestation<*>): Builder = apply {
    addOutputState(state)
    addCommand(AttestationContract.Issue, state.attestor.owningKey)
}

/**
 * Adds an attestation for amendment to a transaction builder, including the required command.
 *
 * @param oldState The old attestation state to be consumed in the transaction.
 * @param newState The new attestation state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addAmendedAttestation(oldState: StateAndRef<Attestation<*>>, newState: Attestation<*>): Builder = apply {
    addInputState(oldState)
    addOutputState(newState)
    addCommand(AttestationContract.Amend, newState.attestor.owningKey)
}

/**
 * Adds an attestation for revocation to a transaction builder, including the required command.
 *
 * @param state The attestation state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addRevokedAttestation(state: StateAndRef<Attestation<*>>): Builder = apply {
    addInputState(state)
    addCommand(AttestationContract.Revoke, state.state.data.attestor.owningKey)
}
