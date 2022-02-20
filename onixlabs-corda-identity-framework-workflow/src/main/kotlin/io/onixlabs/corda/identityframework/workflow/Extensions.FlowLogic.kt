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

package io.onixlabs.corda.identityframework.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.accounts.AccountParty
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.AbstractParty

/**
 * Checks whether the state for the specified attestation has been witnessed by this node.
 *
 * @param attestation The attestation to check.
 * @throws FlowException if the state for the specified attestation has not been witnessed.
 */
@Suspendable
fun FlowLogic<*>.checkHasAttestedStateBeenWitnessed(attestation: Attestation<*>) {
    if (attestation.pointer.resolve(serviceHub) == null) {
        val message = "A state with the pointer value has not been witnessed:"
        throw FlowException("$message ${attestation.pointer.statePointer}.")
    }
}

/**
 * Checks whether the specified claim already exists.
 *
 * @param claim The claim to check for existence.
 * @throws FlowException if the claim already exists.
 */
@Suspendable
fun FlowLogic<*>.checkClaimExists(claim: CordaClaim<*>) {
    val claimExists = serviceHub.vaultServiceFor(claim.javaClass).any {
        claimType(claim.javaClass)
        claimValueType(claim.value.javaClass)
        claimHash(claim.hash)
    }

    if (claimExists) {
        throw FlowException("A claim with the specified hash already exists: ${claim.hash}.")
    }
}

/**
 * Performs a pre-issuance check to determine whether the specified attestation has already been issued.
 *
 * @param attestation The attestation to check for existence.
 * @throws FlowException if the attestation already exists.
 */
@Suspendable
fun FlowLogic<*>.checkAttestationExistsForIssuance(attestation: Attestation<*>) {
    with(serviceHub.vaultServiceFor(attestation.javaClass)) {
        checkAttestationExistsWithIdenticalLinearId(attestation)
        checkAttestationWithIdenticalStatePointerExists(attestation)
        checkAttestationWithIdenticalPointerIdentifierExists(attestation)
    }
}

/**
 * Performs a pre-amendment check to determine whether the specified attestation has already been issued.
 *
 * @param attestation The attestation to check for existence.
 * @throws FlowException if the attestation already exists.
 */
@Suspendable
fun FlowLogic<*>.checkAttestationExistsForAmendment(attestation: Attestation<*>) {
    with(serviceHub.vaultServiceFor(attestation.javaClass)) {
        checkAttestationWithIdenticalHashExists(attestation)
    }
}

/**
 * Checks whether an account exists for the specified party, if the party is an [AccountParty].
 * If the specified party is not an [AccountParty] then this function will be ignored.
 *
 * @param party The party for which to find an account.
 * @throws FlowException if the account does not exist.
 */
@Suspendable
fun FlowLogic<*>.checkAccountExists(party: AbstractParty) {
    if (party is AccountParty && party.getAccountResolver(party.accountType).resolve(serviceHub) == null) {
        throw FlowException("An account with the specified linear ID does not exist: ${party.accountLinearId}.")
    }
}
