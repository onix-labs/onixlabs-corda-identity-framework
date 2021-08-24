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
import net.corda.core.transactions.TransactionBuilder

/**
 * Adds an attestation for issuance to a transaction builder, including the required command.
 *
 * @param attestation The attestation state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addIssuedAttestation(
    attestation: Attestation<*>
): TransactionBuilder = apply {
    addOutputState(attestation)
    addCommand(AttestationContract.Issue, attestation.attestor.owningKey)
}

/**
 * Adds an attestation for amendment to a transaction builder, including the required command.
 *
 * @param oldAttestation The old attestation state to be consumed in the transaction.
 * @param newAttestation The new attestation state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addAmendedAttestation(
    oldAttestation: StateAndRef<Attestation<*>>,
    newAttestation: Attestation<*>
): TransactionBuilder = apply {
    addInputState(oldAttestation)
    addOutputState(newAttestation)
    addCommand(AttestationContract.Amend, newAttestation.attestor.owningKey)
}

/**
 * Adds an attestation for revocation to a transaction builder, including the required command.
 *
 * @param attestation The attestation state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addRevokedAttestation(
    attestation: StateAndRef<Attestation<*>>
): TransactionBuilder = apply {
    addInputState(attestation)
    addCommand(AttestationContract.Revoke, attestation.state.data.attestor.owningKey)
}
