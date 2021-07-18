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

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic

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
 * Checks whether the specified attestation already exists.
 *
 * @param attestation The attestation to check for existence.
 * @throws FlowException if the claim already exists.
 */
@Suspendable
fun FlowLogic<*>.checkAttestationExists(attestation: Attestation<*>) {
    val attestationExists = serviceHub.vaultServiceFor(attestation.javaClass).any {
        attestationType(attestation.javaClass)
        attestationPointerType(attestation.pointer.stateType)
        attestationHash(attestation.hash)
    }

    if (attestationExists) {
        throw FlowException("An attestation with the specified hash already exists: ${attestation.hash}.")
    }
}
